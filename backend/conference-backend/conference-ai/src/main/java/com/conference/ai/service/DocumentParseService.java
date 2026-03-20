package com.conference.ai.service;

import com.conference.ai.entity.AiKnowledge;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 文档解析服务
 * 支持: PDF, Word(.docx), Excel(.xlsx/.xls), TXT/Markdown 文档的内容提取
 * 解析后自动拆分为知识库条目存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParseService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;
    private static final int MAX_CHUNK_SIZE = 2000;  // 每个知识块最大字符数
    private static final int OVERLAP_SIZE = 200;      // 知识块之间的重叠字符数

    private final AiKnowledgeService knowledgeService;

    @Value("${ai.upload.dir:./ai-uploads}")
    private String uploadDir;

    /**
     * 支持的文件类型
     */
    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "pdf", "docx", "doc", "xlsx", "xls", "txt", "md", "csv"
    );

    /**
     * 上传并解析文档，存入知识库
     * @return 解析结果：包含解析出的知识条目数量等信息
     */
    public Map<String, Object> uploadAndParse(MultipartFile file, String category, Long conferenceId, String tags) {
        Map<String, Object> result = new LinkedHashMap<>();
        String originalName = file.getOriginalFilename();
        String ext = getFileExtension(originalName).toLowerCase();

        if (!SUPPORTED_TYPES.contains(ext)) {
            result.put("success", false);
            result.put("message", "不支持的文件类型: " + ext + "。支持: PDF, Word, Excel, TXT, Markdown, CSV");
            return result;
        }

        try {
            // 1. 保存上传文件
            String savedPath = saveFile(file);
            log.info("文档已保存: {} -> {}", originalName, savedPath);

            // 2. 提取文本内容
            String textContent = extractText(file, ext);
            if (textContent == null || textContent.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文档内容为空或无法解析");
                return result;
            }
            log.info("文档已解析: {} , 提取到 {} 字符", originalName, textContent.length());

            // 3. 智能分块
            List<TextChunk> chunks = splitIntoChunks(textContent, originalName);
            log.info("文档已分块: {} 个知识块", chunks.size());

            // 4. 存入知识库
            int savedCount = 0;
            Long tenantId = getTenantId();
            String effectiveCategory = (category != null && !category.isEmpty()) ? category : guessCategory(originalName, textContent);

            for (TextChunk chunk : chunks) {
                try {
                    AiKnowledge knowledge = new AiKnowledge();
                    knowledge.setTenantId(tenantId);
                    knowledge.setConferenceId(conferenceId);
                    knowledge.setTitle(chunk.title);
                    knowledge.setSummary(chunk.summary);
                    knowledge.setContent(chunk.content);
                    knowledge.setCategory(effectiveCategory);
                    knowledge.setTags(buildTags(tags, originalName, ext));
                    knowledge.setIcon(getIconByType(ext));
                    knowledge.setViews(0);
                    knowledge.setStatus("active");
                    knowledge.setSortOrder(chunk.order);
                    knowledge.setDeleted(0);
                    knowledge.setCreateTime(LocalDateTime.now());
                    knowledge.setUpdateTime(LocalDateTime.now());
                    knowledgeService.save(knowledge);
                    savedCount++;
                } catch (Exception e) {
                    log.warn("保存知识块失败: {}", chunk.title, e);
                }
            }

            result.put("success", true);
            result.put("message", "文档解析成功");
            result.put("fileName", originalName);
            result.put("fileSize", formatFileSize(file.getSize()));
            result.put("textLength", textContent.length());
            result.put("chunksTotal", chunks.size());
            result.put("chunksSaved", savedCount);
            result.put("category", effectiveCategory);
            result.put("savedPath", savedPath);

        } catch (Exception e) {
            log.error("文档解析失败: {}", originalName, e);
            result.put("success", false);
            result.put("message", "文档解析失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 提取文本内容 - 根据文件类型分发
     */
    private String extractText(MultipartFile file, String ext) throws Exception {
        switch (ext) {
            case "pdf":
                return extractFromPdf(file);
            case "docx":
                return extractFromDocx(file);
            case "doc":
                return extractFromDoc(file);
            case "xlsx":
                return extractFromXlsx(file);
            case "xls":
                return extractFromXls(file);
            case "txt":
            case "md":
            case "csv":
                return extractFromText(file);
            default:
                throw new IllegalArgumentException("不支持的文件类型: " + ext);
        }
    }

    /**
     * PDF 文本提取
     */
    private String extractFromPdf(MultipartFile file) throws Exception {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            int pages = document.getNumberOfPages();
            log.info("PDF解析完成: {} 页", pages);
            return text;
        }
    }

    /**
     * Word (.docx) 文本提取
     */
    private String extractFromDocx(MultipartFile file) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();

            // 提取正文段落
            for (XWPFParagraph para : doc.getParagraphs()) {
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    // 检测标题样式
                    String style = para.getStyle();
                    if (style != null && style.toLowerCase().contains("heading")) {
                        sb.append("\n## ").append(text.trim()).append("\n\n");
                    } else {
                        sb.append(text.trim()).append("\n");
                    }
                }
            }

            // 提取表格内容
            for (XWPFTable table : doc.getTables()) {
                sb.append("\n");
                for (XWPFTableRow row : table.getRows()) {
                    sb.append("| ");
                    for (XWPFTableCell cell : row.getTableCells()) {
                        sb.append(cell.getText().trim()).append(" | ");
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }

            return sb.toString();
        }
    }

    /**
     * Word (.doc) 旧格式 - 简易提取
     */
    private String extractFromDoc(MultipartFile file) throws Exception {
        // .doc 格式使用 POI HWPF，但依赖较重，这里使用简易处理
        try (InputStream is = file.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            // 尝试作为文本读取
            String text = new String(bytes, StandardCharsets.UTF_8);
            // 如果包含大量乱码字符，可能是二进制格式
            long printableRatio = text.chars().filter(c -> c >= 32 && c < 127 || c > 0x4e00).count();
            if (printableRatio * 100 / Math.max(text.length(), 1) < 30) {
                return "（.doc旧格式文件，建议转换为.docx格式后重新上传以获得更好的解析效果）";
            }
            return text;
        }
    }

    /**
     * Excel (.xlsx) 文本提取 - 智能格式化日期和数字
     */
    private String extractFromXlsx(MultipartFile file) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                sb.append("## 工作表: ").append(sheet.getSheetName()).append("\n\n");
                sb.append(extractSheetContent(sheet));
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Excel (.xls) 旧格式文本提取 - 复用通用Sheet解析
     */
    private String extractFromXls(MultipartFile file) throws Exception {
        try (HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                sb.append("## 工作表: ").append(sheet.getSheetName()).append("\n\n");
                sb.append(extractSheetContent(sheet));
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * 纯文本文件提取 (TXT / Markdown / CSV)
     */
    private String extractFromText(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        // 尝试UTF-8解码
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 提取工作表内容（通用方法，支持XSSF和HSSF）
     * 跳过全空行，保持表格结构清晰
     */
    private String extractSheetContent(Sheet sheet) {
        StringBuilder sb = new StringBuilder();
        int emptyRowCount = 0;
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null || isRowEmpty(row)) {
                emptyRowCount++;
                if (emptyRowCount > 2) continue; // 连续超过2个空行就跳过
                continue;
            }
            emptyRowCount = 0;
            StringBuilder rowStr = new StringBuilder();
            boolean hasContent = false;
            for (int c = 0; c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                String val = getSmartCellValue(cell);
                if (!val.isEmpty()) hasContent = true;
                rowStr.append(val).append(" | ");
            }
            if (hasContent) {
                sb.append("| ").append(rowStr).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 判断行是否为空
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getSmartCellValue(cell);
                if (!val.isEmpty()) return false;
            }
        }
        return true;
    }

    /**
     * 智能获取单元格值
     * - 日期单元格：格式化为 yyyy-MM-dd
     * - 整数数值：去掉 .0（如 1.0 → 1，35.0 → 35）
     * - 小数数值：保留实际精度
     * - 公式单元格：取缓存结果值
     */
    private String getSmartCellValue(Cell cell) {
        if (cell == null) return "";
        try {
            CellType type = cell.getCellType();
            // 公式单元格取缓存结果
            if (type == CellType.FORMULA) {
                type = cell.getCachedFormulaResultType();
            }
            return switch (type) {
                case NUMERIC -> {
                    // 检测是否为日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        java.util.Date date = cell.getDateCellValue();
                        if (date != null) {
                            yield new SimpleDateFormat("yyyy-MM-dd").format(date);
                        }
                        yield "";
                    }
                    // 数值：整数去掉.0
                    double val = cell.getNumericCellValue();
                    if (val == Math.floor(val) && !Double.isInfinite(val)) {
                        yield String.valueOf((long) val);
                    }
                    // 保留2位小数
                    yield String.format("%.2f", val);
                }
                case STRING -> cell.getStringCellValue().trim();
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                case BLANK -> "";
                default -> cell.toString().trim();
            };
        } catch (Exception e) {
            try { return cell.toString().trim(); } catch (Exception ex) { return ""; }
        }
    }

    /**
     * 智能分块 - 将长文本拆分为多个知识块
     * 策略：按段落/标题分割，保持语义完整性
     * Excel特殊处理：相邻的小Sheet合并为一个知识块
     */
    private List<TextChunk> splitIntoChunks(String text, String fileName) {
        List<TextChunk> chunks = new ArrayList<>();
        String baseName = getFileBaseName(fileName);

        // 先按标题（## 开头的行）分段
        String[] sections = text.split("(?m)^##\\s+");

        if (sections.length > 1) {
            // 有标题结构，尝试合并相邻的小段落
            int order = 0;
            StringBuilder mergeBuffer = new StringBuilder();
            String mergeTitle = null;
            List<String> mergeTitles = new ArrayList<>();

            for (String section : sections) {
                if (section.trim().isEmpty()) continue;
                String[] lines = section.split("\n", 2);
                String title = lines[0].trim();
                String body = lines.length > 1 ? lines[1].trim() : title;

                // 如果当前段落加入缓冲区后不超限，就合并
                if (mergeBuffer.length() + body.length() + title.length() + 20 <= MAX_CHUNK_SIZE) {
                    mergeBuffer.append("### ").append(title).append("\n").append(body).append("\n\n");
                    mergeTitles.add(title);
                    if (mergeTitle == null) mergeTitle = title;
                } else {
                    // 先保存缓冲区
                    if (mergeBuffer.length() > 0) {
                        String combinedTitle = mergeTitles.size() <= 2
                                ? baseName + " - " + String.join("、", mergeTitles)
                                : baseName + " - " + mergeTitles.get(0) + "等" + mergeTitles.size() + "项";
                        chunks.add(new TextChunk(
                                combinedTitle,
                                truncate(mergeBuffer.toString(), 150),
                                mergeBuffer.toString().trim(),
                                order++
                        ));
                        mergeBuffer.setLength(0);
                        mergeTitles.clear();
                    }
                    // 处理当前段落
                    if (body.length() <= MAX_CHUNK_SIZE) {
                        mergeBuffer.append("### ").append(title).append("\n").append(body).append("\n\n");
                        mergeTitles.add(title);
                        mergeTitle = title;
                    } else {
                        // 大段落拆分
                        List<String> subChunks = splitBySize(body, MAX_CHUNK_SIZE, OVERLAP_SIZE);
                        for (int i = 0; i < subChunks.size(); i++) {
                            chunks.add(new TextChunk(
                                    baseName + " - " + title + (subChunks.size() > 1 ? " (第" + (i + 1) + "部分)" : ""),
                                    truncate(subChunks.get(i), 150),
                                    subChunks.get(i),
                                    order++
                            ));
                        }
                    }
                }
            }
            // 保存最后的缓冲区
            if (mergeBuffer.length() > 0) {
                String combinedTitle = mergeTitles.size() <= 2
                        ? baseName + " - " + String.join("、", mergeTitles)
                        : baseName + " - " + mergeTitles.get(0) + "等" + mergeTitles.size() + "项";
                chunks.add(new TextChunk(
                        combinedTitle,
                        truncate(mergeBuffer.toString(), 150),
                        mergeBuffer.toString().trim(),
                        order++
                ));
            }
        } else {
            // 无标题结构，按固定大小分块
            List<String> subChunks = splitBySize(text.trim(), MAX_CHUNK_SIZE, OVERLAP_SIZE);
            for (int i = 0; i < subChunks.size(); i++) {
                chunks.add(new TextChunk(
                        baseName + (subChunks.size() > 1 ? " (第" + (i + 1) + "/" + subChunks.size() + "部分)" : ""),
                        truncate(subChunks.get(i), 150),
                        subChunks.get(i),
                        i
                ));
            }
        }

        // 如果整个文档很短，就作为一个知识块
        if (chunks.isEmpty() && !text.trim().isEmpty()) {
            chunks.add(new TextChunk(baseName, truncate(text, 150), text.trim(), 0));
        }

        return chunks;
    }

    /**
     * 按大小分割文本，在句子边界处切分
     */
    private List<String> splitBySize(String text, int maxSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxSize, text.length());
            if (end < text.length()) {
                // 尝试在句子边界处切分
                int breakPoint = findBreakPoint(text, start + maxSize - 200, end);
                if (breakPoint > start) end = breakPoint;
            }
            chunks.add(text.substring(start, end).trim());
            start = end - overlap;
            if (start < 0) start = 0;
            if (end >= text.length()) break;
        }
        return chunks;
    }

    /**
     * 找到句子边界
     */
    private int findBreakPoint(String text, int from, int to) {
        // 优先在段落换行处切分
        for (int i = to; i >= from; i--) {
            if (text.charAt(i) == '\n') return i + 1;
        }
        // 其次在句号处切分
        String[] delimiters = {"。", ".", "！", "!", "？", "?", "；", ";", "\n"};
        for (String d : delimiters) {
            int idx = text.lastIndexOf(d, to);
            if (idx >= from) return idx + d.length();
        }
        return to;
    }

    /**
     * 保存上传文件到本地
     */
    private String saveFile(MultipartFile file) throws IOException {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        String ext = getFileExtension(file.getOriginalFilename());
        String savedName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
        Path target = dir.resolve(savedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    /**
     * 根据文件名和内容猜测分类
     */
    private String guessCategory(String fileName, String content) {
        String lower = (fileName + " " + content.substring(0, Math.min(content.length(), 500))).toLowerCase();
        if (containsAny(lower, "日程", "议程", "安排", "schedule", "agenda")) return "会议信息";
        if (containsAny(lower, "交通", "路线", "班车", "出行")) return "出行指南";
        if (containsAny(lower, "住宿", "酒店", "宾馆", "房间")) return "住宿信息";
        if (containsAny(lower, "餐饮", "用餐", "饮食")) return "餐饮服务";
        if (containsAny(lower, "签到", "报名", "注册")) return "报名签到";
        if (containsAny(lower, "讲师", "嘉宾", "专家", "speaker")) return "讲师信息";
        if (containsAny(lower, "须知", "注意", "规则", "制度")) return "会务须知";
        if (containsAny(lower, "资料", "课件", "讲义", "教材")) return "学习资料";
        return "会议资料";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) { if (text.contains(k)) return true; }
        return false;
    }

    private String buildTags(String userTags, String fileName, String ext) {
        List<String> tagList = new ArrayList<>();
        if (userTags != null && !userTags.isEmpty()) tagList.add(userTags);
        tagList.add("文档导入");
        tagList.add(ext.toUpperCase());
        tagList.add(getFileBaseName(fileName));
        return String.join(",", tagList);
    }

    private String getIconByType(String ext) {
        return switch (ext) {
            case "pdf" -> "fas fa-file-pdf";
            case "docx", "doc" -> "fas fa-file-word";
            case "xlsx", "xls", "csv" -> "fas fa-file-excel";
            case "md" -> "fas fa-file-code";
            default -> "fas fa-file-alt";
        };
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }

    private String getFileBaseName(String filename) {
        if (filename == null) return "未命名文档";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        text = text.replaceAll("\\s+", " ").trim();
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    private Long getTenantId() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    /**
     * 文本块数据结构
     */
    static class TextChunk {
        String title;
        String summary;
        String content;
        int order;

        TextChunk(String title, String summary, String content, int order) {
            this.title = title;
            this.summary = summary;
            this.content = content;
            this.order = order;
        }
    }
}
