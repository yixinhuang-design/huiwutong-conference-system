package com.conference.collaboration.service;

import com.conference.collaboration.entity.TaskInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 任务导出服务
 * 支持Excel、PDF等格式导出
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskExportService {

    private final TaskService taskService;

    /**
     * 导出征集表单数据为Excel
     * @param taskId 任务ID
     * @return Excel文件字节数组
     */
    public byte[] exportCollectionToExcel(Long taskId) throws IOException {
        // 获取任务详情
        Map<String, Object> taskDetail = taskService.getTaskDetail(taskId);
        TaskInfo task = (TaskInfo) taskDetail.get("task");

        // 获取征集数据
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> collectionData = 
            (List<Map<String, Object>>) taskDetail.get("collectionData");

        // 解析字段配置
        String configJson = task.getConfig();
        List<Map<String, Object>> fields = parseFieldsFromConfig(configJson);

        // 创建Excel工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("征集数据");

        // 创建标题样式
        CellStyle headerStyle = createHeaderStyle(workbook);

        // 创建表头
        Row headerRow = sheet.createRow(0);
        int colIndex = 0;
        headerRow.createCell(colIndex++).setCellValue("学员姓名");
        for (Map<String, Object> field : fields) {
            headerRow.createCell(colIndex++).setCellValue((String) field.get("label"));
        }
        headerRow.createCell(colIndex++).setCellValue("提交时间");

        // 应用表头样式
        for (Cell cell : headerRow) {
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        int rowIndex = 1;
        if (collectionData != null) {
            for (Map<String, Object> submission : collectionData) {
                Row dataRow = sheet.createRow(rowIndex++);
                colIndex = 0;

                // 学员姓名
                dataRow.createCell(colIndex++).setCellValue((String) submission.get("userName"));

                // 字段数据
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) submission.get("data");
                if (data != null) {
                    for (Map<String, Object> field : fields) {
                        String label = (String) field.get("label");
                        Object value = data.get(label);
                        String cellValue = value != null ? value.toString() : "";
                        dataRow.createCell(colIndex++).setCellValue(cellValue);
                    }
                }

                // 提交时间
                dataRow.createCell(colIndex++).setCellValue((String) submission.get("time"));
            }
        }

        // 自动调整列宽
        for (int i = 0; i < colIndex; i++) {
            sheet.autoSizeColumn(i);
            // 设置最小列宽
            if (sheet.getColumnWidth(i) < 2000) {
                sheet.setColumnWidth(i, 2000);
            }
            // 设置最大列宽
            if (sheet.getColumnWidth(i) > 6000) {
                sheet.setColumnWidth(i, 6000);
            }
        }

        // 写入字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        log.info("征集表单导出成功: taskId={}, 数据行数={}", taskId, rowIndex - 1);
        return outputStream.toByteArray();
    }

    /**
     * 导出任务完成报告为Excel
     * @param taskId 任务ID
     * @return Excel文件字节数组
     */
    public byte[] exportTaskReportToExcel(Long taskId) throws IOException {
        Map<String, Object> taskDetail = taskService.getTaskDetail(taskId);
        TaskInfo task = (TaskInfo) taskDetail.get("task");

        Workbook workbook = new XSSFWorkbook();

        // 创建任务概况Sheet
        Sheet overviewSheet = workbook.createSheet("任务概况");
        createTaskOverviewSheet(overviewSheet, task, taskDetail);

        // 创建执行人明细Sheet
        Sheet assigneesSheet = workbook.createSheet("执行人明细");
        createAssigneesSheet(assigneesSheet, taskDetail);

        // 根据完成方式创建结果Sheet
        String completionMethod = task.getCompletionMethod();
        if ("collection".equals(completionMethod)) {
            Sheet collectionSheet = workbook.createSheet("征集数据");
            createCollectionSheet(collectionSheet, taskDetail);
        } else if ("questionnaire".equals(completionMethod)) {
            Sheet questionnaireSheet = workbook.createSheet("问卷统计");
            createQuestionnaireSheet(questionnaireSheet, taskDetail);
        } else if ("text_image".equals(completionMethod)) {
            Sheet feedbackSheet = workbook.createSheet("反馈内容");
            createFeedbackSheet(feedbackSheet, taskDetail);
        } else if ("location".equals(completionMethod)) {
            Sheet checkinSheet = workbook.createSheet("签到记录");
            createCheckinSheet(checkinSheet, taskDetail);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        log.info("任务报告导出成功: taskId={}", taskId);
        return outputStream.toByteArray();
    }

    /**
     * 创建任务概况Sheet
     */
    private void createTaskOverviewSheet(Sheet sheet, TaskInfo task, Map<String, Object> taskDetail) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        CellStyle labelStyle = createLabelStyle(sheet.getWorkbook());

        int row = 0;
        // 标题
        Row titleRow = sheet.createRow(row++);
        titleRow.createCell(0).setCellValue("任务完成报告");
        titleRow.getCell(0).setCellStyle(headerStyle);

        row++; // 空行

        // 任务信息
        createOverviewRow(sheet, row++, "任务名称", task.getTaskName(), labelStyle);
        createOverviewRow(sheet, row++, "任务类型", getTaskTypeName(task.getTaskType()), labelStyle);
        createOverviewRow(sheet, row++, "任务类别", getCategoryName(task.getCategory()), labelStyle);
        createOverviewRow(sheet, row++, "完成方式", getCompletionMethodName(task.getCompletionMethod()), labelStyle);
        createOverviewRow(sheet, row++, "优先级", getPriorityName(task.getPriority()), labelStyle);
        createOverviewRow(sheet, row++, "任务状态", getStatusName(task.getStatus()), labelStyle);
        createOverviewRow(sheet, row++, "完成进度", task.getProgress() + "%", labelStyle);
        createOverviewRow(sheet, row++, "创建时间", task.getCreateTime().toString(), labelStyle);
        createOverviewRow(sheet, row++, "截止时间", 
            task.getDeadline() != null ? task.getDeadline().toString() : "无", labelStyle);

        row++; // 空行

        // 统计信息
        int totalAssignees = (int) taskDetail.get("totalAssignees");
        int completedAssignees = (int) taskDetail.get("completedAssignees");
        int progress = (int) taskDetail.get("progress");

        createOverviewRow(sheet, row++, "总执行人数", String.valueOf(totalAssignees), labelStyle);
        createOverviewRow(sheet, row++, "已完成人数", String.valueOf(completedAssignees), labelStyle);
        createOverviewRow(sheet, row++, "完成率", progress + "%", labelStyle);

        // 自动调整列宽
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.setColumnWidth(0, 5000);
    }

    /**
     * 创建执行人明细Sheet
     */
    @SuppressWarnings("unchecked")
    private void createAssigneesSheet(Sheet sheet, Map<String, Object> taskDetail) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        List<Map<String, Object>> assignees = 
            (List<Map<String, Object>>) taskDetail.get("assignees");

        // 表头
        Row headerRow = sheet.createRow(0);
        int col = 0;
        headerRow.createCell(col++).setCellValue("执行人姓名");
        headerRow.createCell(col++).setCellValue("角色");
        headerRow.createCell(col++).setCellValue("状态");
        headerRow.createCell(col++).setCellValue("提交内容");
        headerRow.createCell(col++).setCellValue("提交时间");

        for (Cell cell : headerRow) {
            cell.setCellStyle(headerStyle);
        }

        // 数据
        if (assignees != null) {
            int row = 1;
            for (Map<String, Object> assignee : assignees) {
                Row dataRow = sheet.createRow(row++);
                col = 0;
                dataRow.createCell(col++).setCellValue((String) assignee.get("userName"));
                dataRow.createCell(col++).setCellValue((String) assignee.get("role"));
                dataRow.createCell(col++).setCellValue(getStatusName((String) assignee.get("status")));
                dataRow.createCell(col++).setCellValue((String) assignee.get("submitContent"));
                dataRow.createCell(col++).setCellValue(
                    assignee.get("submitTime") != null ? assignee.get("submitTime").toString() : "");
            }
        }

        // 自动调整列宽
        for (int i = 0; i < col; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建征集数据Sheet
     */
    @SuppressWarnings("unchecked")
    private void createCollectionSheet(Sheet sheet, Map<String, Object> taskDetail) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        List<Map<String, Object>> collectionData = 
            (List<Map<String, Object>>) taskDetail.get("collectionData");

        if (collectionData == null || collectionData.isEmpty()) {
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("暂无征集数据");
            return;
        }

        // 获取字段列表
        Map<String, Object> firstSubmission = collectionData.get(0);
        Map<String, Object> firstData = (Map<String, Object>) firstSubmission.get("data");
        List<String> fields = new ArrayList<>(firstData.keySet());

        // 表头
        Row headerRow = sheet.createRow(0);
        int col = 0;
        headerRow.createCell(col++).setCellValue("学员姓名");
        for (String field : fields) {
            headerRow.createCell(col++).setCellValue(field);
        }
        headerRow.createCell(col++).setCellValue("提交时间");

        for (Cell cell : headerRow) {
            cell.setCellStyle(headerStyle);
        }

        // 数据
        int row = 1;
        for (Map<String, Object> submission : collectionData) {
            Row dataRow = sheet.createRow(row++);
            col = 0;
            dataRow.createCell(col++).setCellValue((String) submission.get("userName"));

            Map<String, Object> data = (Map<String, Object>) submission.get("data");
            if (data != null) {
                for (String field : fields) {
                    Object value = data.get(field);
                    dataRow.createCell(col++).setCellValue(value != null ? value.toString() : "");
                }
            }

            dataRow.createCell(col++).setCellValue((String) submission.get("time"));
        }

        // 自动调整列宽
        for (int i = 0; i < col; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建问卷统计Sheet
     */
    @SuppressWarnings("unchecked")
    private void createQuestionnaireSheet(Sheet sheet, Map<String, Object> taskDetail) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        TaskInfo task = (TaskInfo) taskDetail.get("task");

        // 解析问卷配置中的问题列表
        List<Map<String, Object>> questions = new ArrayList<>();
        if (task.getConfig() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> config = objectMapper.readValue(task.getConfig(), Map.class);
                Object questionsObj = config.get("questions");
                if (questionsObj instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> map) {
                            questions.add((Map<String, Object>) map);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析问卷配置失败: {}", e.getMessage());
            }
        }

        if (questions.isEmpty()) {
            sheet.createRow(0).createCell(0).setCellValue("暂无问卷配置或问卷数据");
            return;
        }

        // 创建表头
        Row headerRow = sheet.createRow(0);
        int col = 0;
        headerRow.createCell(col++).setCellValue("学员姓名");
        for (Map<String, Object> q : questions) {
            headerRow.createCell(col++).setCellValue((String) q.get("title"));
        }
        headerRow.createCell(col++).setCellValue("提交时间");
        for (Cell cell : headerRow) {
            cell.setCellStyle(headerStyle);
        }

        // 填充数据
        Object assigneesObj = taskDetail.get("assignees");
        int rowIndex = 1;
        if (assigneesObj instanceof List<?> assigneeList) {
            for (Object assigneeObj : assigneeList) {
                String userName = "";
                String submitContent = "";
                String submitTime = "";

                if (assigneeObj instanceof com.conference.collaboration.entity.TaskAssignee ta) {
                    if (!"completed".equals(ta.getStatus())) continue;
                    userName = ta.getUserName() != null ? ta.getUserName() : "";
                    submitContent = ta.getSubmitContent() != null ? ta.getSubmitContent() : "";
                    submitTime = ta.getSubmitTime() != null ? ta.getSubmitTime().toString() : "";
                } else if (assigneeObj instanceof Map<?, ?> map) {
                    if (!"completed".equals(map.get("status"))) continue;
                    userName = map.get("userName") != null ? map.get("userName").toString() : "";
                    submitContent = map.get("submitContent") != null ? map.get("submitContent").toString() : "";
                    submitTime = map.get("submitTime") != null ? map.get("submitTime").toString() : "";
                }

                Row dataRow = sheet.createRow(rowIndex++);
                col = 0;
                dataRow.createCell(col++).setCellValue(userName);

                Map<String, Object> answers = new java.util.LinkedHashMap<>();
                if (!submitContent.isEmpty()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                        answers = om.readValue(submitContent, Map.class);
                    } catch (Exception e) {
                        answers.put("_raw", submitContent);
                    }
                }

                for (Map<String, Object> q : questions) {
                    String questionTitle = (String) q.get("title");
                    Object answer = answers.get(questionTitle);
                    dataRow.createCell(col++).setCellValue(answer != null ? answer.toString() : "");
                }
                dataRow.createCell(col++).setCellValue(submitTime);
            }
        }

        for (int i = 0; i <= col; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建反馈内容Sheet
     */
    @SuppressWarnings("unchecked")
    private void createFeedbackSheet(Sheet sheet, Map<String, Object> taskDetail) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        List<Map<String, Object>> textResponses = 
            (List<Map<String, Object>>) taskDetail.get("textResponses");
        List<Map<String, Object>> imageUploads = 
            (List<Map<String, Object>>) taskDetail.get("imageUploads");

        // 文字反馈
        if (textResponses != null && !textResponses.isEmpty()) {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("学员姓名");
            headerRow.createCell(1).setCellValue("反馈内容");
            headerRow.createCell(2).setCellValue("提交时间");
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }

            int row = 1;
            for (Map<String, Object> response : textResponses) {
                Row dataRow = sheet.createRow(row++);
                dataRow.createCell(0).setCellValue((String) response.get("userName"));
                dataRow.createCell(1).setCellValue((String) response.get("text"));
                dataRow.createCell(2).setCellValue((String) response.get("time"));
            }
        }

        // 图片上传
        if (imageUploads != null && !imageUploads.isEmpty()) {
            int startRow = textResponses != null ? textResponses.size() + 2 : 0;
            Row titleRow = sheet.createRow(startRow);
            titleRow.createCell(0).setCellValue("图片上传列表");

            int row = startRow + 1;
            for (Map<String, Object> image : imageUploads) {
                Row dataRow = sheet.createRow(row++);
                dataRow.createCell(0).setCellValue((String) image.get("userName"));
                dataRow.createCell(1).setCellValue((String) image.get("url"));
                dataRow.createCell(2).setCellValue((String) image.get("time"));
            }
        }
    }

    /**
     * 创建签到记录Sheet
     */
    @SuppressWarnings("unchecked")
    private void createCheckinSheet(Sheet sheet, Map<String, Object> taskDetail) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        List<Map<String, Object>> locationCheckins = 
            (List<Map<String, Object>>) taskDetail.get("locationCheckins");

        if (locationCheckins == null || locationCheckins.isEmpty()) {
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("暂无签到记录");
            return;
        }

        // 表头
        Row headerRow = sheet.createRow(0);
        int col = 0;
        headerRow.createCell(col++).setCellValue("学员姓名");
        headerRow.createCell(col++).setCellValue("签到位置");
        headerRow.createCell(col++).setCellValue("距离(米)");
        headerRow.createCell(col++).setCellValue("签到时间");
        headerRow.createCell(col++).setCellValue("照片URL");

        for (Cell cell : headerRow) {
            cell.setCellStyle(headerStyle);
        }

        // 数据
        int row = 1;
        for (Map<String, Object> checkin : locationCheckins) {
            Row dataRow = sheet.createRow(row++);
            col = 0;
            dataRow.createCell(col++).setCellValue((String) checkin.get("userName"));
            dataRow.createCell(col++).setCellValue((String) checkin.get("location"));
            dataRow.createCell(col++).setCellValue(checkin.get("distance") != null ? 
                checkin.get("distance").toString() : "");
            dataRow.createCell(col++).setCellValue((String) checkin.get("time"));
            dataRow.createCell(col++).setCellValue((String) checkin.get("photo"));
        }

        // 自动调整列宽
        for (int i = 0; i < col; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // ==================== 辅助方法 ====================

    private void createOverviewRow(Sheet sheet, int row, String label, String value, CellStyle labelStyle) {
        Row dataRow = sheet.createRow(row);
        dataRow.createCell(0).setCellValue(label);
        dataRow.createCell(1).setCellValue(value);
        dataRow.getCell(0).setCellStyle(labelStyle);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseFieldsFromConfig(String configJson) {
        List<Map<String, Object>> fields = new ArrayList<>();
        if (configJson == null || configJson.isEmpty()) {
            return fields;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
            // 支持 {"fields": [...]} 格式
            Object fieldsObj = config.get("fields");
            if (fieldsObj instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map) {
                        fields.add((Map<String, Object>) map);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析config JSON失败: {}", e.getMessage());
        }
        return fields;
    }

    private String getTaskTypeName(String taskType) {
        if (taskType == null) return "其他";
        return switch (taskType) {
            case "preparation" -> "筹备任务";
            case "venue_check" -> "会场检查";
            case "reception" -> "学员接待";
            case "registration" -> "报到任务";
            case "checkin" -> "签到任务";
            case "room_check" -> "查寝任务";
            case "evaluation" -> "评价任务";
            case "collection" -> "征集任务";
            default -> "其他";
        };
    }

    private String getCategoryName(String category) {
        if (category == null) return "其他";
        return switch (category) {
            case "venue" -> "会场任务";
            case "student" -> "学员任务";
            case "custom" -> "其他任务";
            default -> "其他";
        };
    }

    private String getCompletionMethodName(String method) {
        if (method == null) return "其他";
        return switch (method) {
            case "text_image" -> "文字/图片";
            case "location" -> "位置签到";
            case "questionnaire" -> "评价问卷";
            case "collection" -> "征集表单";
            default -> "其他";
        };
    }

    private String getPriorityName(String priority) {
        if (priority == null) return "中";
        return switch (priority) {
            case "low" -> "低";
            case "medium" -> "中";
            case "high" -> "高";
            case "urgent" -> "紧急";
            default -> "中";
        };
    }

    private String getStatusName(String status) {
        if (status == null) return "未知";
        return switch (status) {
            case "pending" -> "待开始";
            case "in_progress" -> "进行中";
            case "completed" -> "已完成";
            case "overdue" -> "已超时";
            case "cancelled" -> "已取消";
            default -> "未知";
        };
    }
}
