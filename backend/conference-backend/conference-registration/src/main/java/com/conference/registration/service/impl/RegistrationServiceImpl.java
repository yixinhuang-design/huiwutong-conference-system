package com.conference.registration.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.common.exception.BusinessException;
import com.conference.common.result.ResultCode;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.registration.dto.RegistrationAuditRequest;
import com.conference.registration.dto.RegistrationFieldConfigRequest;
import com.conference.registration.dto.RegistrationRequest;
import com.conference.registration.entity.Registration;
import com.conference.registration.entity.RegistrationAudit;
import com.conference.registration.mapper.RegistrationAuditMapper;
import com.conference.registration.mapper.RegistrationMapper;
import com.conference.registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 报名服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration>
        implements RegistrationService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;
    private static final int STATUS_CANCELLED = 3;
    private static final int STATUS_CHECKED_IN = 4;

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]");
    private static final Pattern NAME_PATTERN = Pattern.compile("姓名[：:]?\\s*([\\u4e00-\\u9fa5·]{2,20})");

    private final RegistrationMapper registrationMapper;
    private final RegistrationAuditMapper auditMapper;

    @Value("${registration.ocr.api-url:https://api.ocr.space/parse/imageurl}")
    private String ocrApiUrl;

    @Value("${registration.ocr.api-key:helloworld}")
    private String ocrApiKey;

    @Override
    @Transactional
    public Registration submitRegistration(RegistrationRequest request) {
        Long tenantId = currentTenantId();
        Long userId = TenantContextHolder.getUserId();

        log.info("=== 报名提交开始 ===");
        log.info("会议ID: {}, 租户ID: {}, 用户ID: {}", request.getConferenceId(), tenantId, userId);
        log.info("报名人: {}, 手机号: {}", request.getRealName(), request.getPhone());

        // 处理idCard的特殊值：如果是"未识别"或空字符串，转换为null
        if (!StringUtils.hasText(request.getIdCard()) || "未识别".equals(request.getIdCard())) {
            log.debug("idCard为特殊值[{}]，已转换为null", request.getIdCard());
            request.setIdCard(null);
        }

        Registration existing = registrationMapper.selectOne(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getConferenceId, request.getConferenceId())
                        .eq(Registration::getPhone, request.getPhone())
                        .eq(Registration::getTenantId, tenantId)
                        .eq(Registration::getDeleted, 0)
                        .last("LIMIT 1")
        );
        if (existing != null) {
            log.error("报名已存在，phone: {}, conferenceId: {}", request.getPhone(), request.getConferenceId());
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST);
        }

        List<RegistrationFieldConfigRequest.FieldConfig> formFields = getRegistrationFormFields(request.getConferenceId());
        log.debug("会议[{}]的动态字段配置: {}", request.getConferenceId(), formFields);
        validateDynamicFields(request.getRegistrationData(), formFields);

        Registration registration = new Registration();
        registration.setConferenceId(request.getConferenceId());
        registration.setTenantId(tenantId);
        registration.setUserId(userId);
        registration.setRealName(request.getRealName());
        registration.setPhone(request.getPhone());
        registration.setEmail(request.getEmail());
        registration.setDepartment(request.getDepartment());
        registration.setPosition(request.getPosition());
        registration.setIdCard(request.getIdCard());
        registration.setIdCardPhotoUrl(request.getIdCardPhotoUrl());
        registration.setDiplomaPhotoUrl(request.getDiplomaPhotoUrl());
        registration.setStatus(STATUS_PENDING);
        registration.setDeleted(0);

        String mergedData = mergeRegistrationDataWithFiles(request.getRegistrationData(), request.getIdCardPhotoUrl(), request.getDiplomaPhotoUrl());

        // 只在有idCardPhotoUrl且registrationData中没有OCR结果时才进行OCR识别
        if (StringUtils.hasText(request.getIdCardPhotoUrl()) && !hasOcrResult(mergedData)) {
            log.debug("对会议[{}]的用户[{}]进行OCR识别", request.getConferenceId(), request.getPhone());
            Map<String, String> ocrResult = ocrIdCard(request.getIdCardPhotoUrl());
            LinkedHashMap<String, Object> validation = validateOcrResult(registration, ocrResult);

            JSONObject dataJson = safeObject(mergedData);
            dataJson.put("ocr", ocrResult);
            dataJson.put("ocrValidation", validation);
            dataJson.put("ocrTime", LocalDateTime.now().toString());
            mergedData = dataJson.toJSONString();
        } else if (StringUtils.hasText(request.getIdCardPhotoUrl())) {
            log.debug("registrationData中已存在OCR结果，跳过重复识别");
        }

        registration.setRegistrationData(mergedData);
        
        log.info("正在保存报名记录...");
        registrationMapper.insert(registration);
        log.info("报名记录保存成功，ID: {}, meeting_id: {}, tenant_id: {}", registration.getId(), registration.getConferenceId(), registration.getTenantId());

        // 数据一致性检查：插入后立即查询验证
        Registration saved = registrationMapper.selectById(registration.getId());
        if (saved == null) {
            log.error("【严重错误】数据插入后查询失败！无法找到刚插入的记录，ID: {}", registration.getId());
            throw new BusinessException(ResultCode.OPERATION_FAILED, "报名保存失败：数据验证错误");
        }
        
        if (!Objects.equals(saved.getConferenceId(), request.getConferenceId())) {
            log.error("【严重错误】保存的conferenceId不匹配！期望: {}, 实际: {}", request.getConferenceId(), saved.getConferenceId());
            throw new BusinessException(ResultCode.OPERATION_FAILED, "报名保存失败：会议ID保存错误");
        }
        
        if (!Objects.equals(saved.getTenantId(), tenantId)) {
            log.error("【严重错误】保存的tenantId不匹配！期望: {}, 实际: {}", tenantId, saved.getTenantId());
            throw new BusinessException(ResultCode.OPERATION_FAILED, "报名保存失败：租户ID保存错误");
        }

        log.info("=== 报名提交成功 ===");
        log.info("最终保存的数据 - ID: {}, phone: {}, conferenceId: {}, tenantId: {}", 
            saved.getId(), saved.getPhone(), saved.getConferenceId(), saved.getTenantId());
        
        return saved;
    }

    @Override
    public Registration getRegistrationDetail(Long registrationId) {
        return registrationMapper.selectById(registrationId);
    }

    @Override
    public Page<Registration> listRegistrations(Long conferenceId, int page, int pageSize) {
        Long tenantId = currentTenantId();

        return registrationMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getConferenceId, conferenceId)
                        .eq(Registration::getTenantId, tenantId)
                        .eq(Registration::getDeleted, 0)
                        .orderByDesc(Registration::getRegistrationTime)
        );
    }

    @Override
    @Transactional
    public void auditRegistration(RegistrationAuditRequest request, Long auditorId) {
        Registration registration = registrationMapper.selectById(request.getRegistrationId());
        if (registration == null || Integer.valueOf(1).equals(registration.getDeleted())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }

        registration.setStatus(mapStatusCode(request.getAuditResult()));
        registration.setAuditRemark(request.getRemark());
        registration.setAuditorId(auditorId);
        registration.setAuditTime(LocalDateTime.now());
        registrationMapper.updateById(registration);

        RegistrationAudit audit = new RegistrationAudit();
        audit.setRegistrationId(request.getRegistrationId());
        audit.setTenantId(registration.getTenantId());
        audit.setAuditorId(auditorId);
        audit.setAuditResult(request.getAuditResult());
        audit.setRemark(request.getRemark());
        audit.setAuditTime(LocalDateTime.now());
        audit.setAuditMethod("manual");
        audit.setDeleted(0);
        auditMapper.insert(audit);
    }

    @Override
    @Transactional
    public void batchAuditRegistration(List<RegistrationAuditRequest> requests, Long auditorId) {
        for (RegistrationAuditRequest request : requests) {
            auditRegistration(request, auditorId);
        }
    }

    @Override
    public byte[] exportRegistrationList(Long conferenceId) {
        Long tenantId = currentTenantId();

        List<Registration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getConferenceId, conferenceId)
                        .eq(Registration::getTenantId, tenantId)
                        .eq(Registration::getStatus, STATUS_APPROVED)
                        .eq(Registration::getDeleted, 0)
                        .orderByAsc(Registration::getDepartment)
        );

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("报名名册");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "姓名", "单位", "职位", "手机号", "邮箱", "身份证", "报名时间", "状态", "OCR结果"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Registration reg : registrations) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(nullable(reg.getRealName()));
                row.createCell(2).setCellValue(nullable(reg.getDepartment()));
                row.createCell(3).setCellValue(nullable(reg.getPosition()));
                row.createCell(4).setCellValue(nullable(reg.getPhone()));
                row.createCell(5).setCellValue(nullable(reg.getEmail()));
                row.createCell(6).setCellValue(nullable(reg.getIdCard()));
                row.createCell(7).setCellValue(reg.getRegistrationTime() == null ? "" : reg.getRegistrationTime().toString());
                row.createCell(8).setCellValue(mapStatusText(reg.getStatus()));
                row.createCell(9).setCellValue(extractOcrStatus(reg.getRegistrationData()));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出报名名册失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        }
    }

    @Override
    public Map<String, Object> getRegistrationStats(Long conferenceId) {
        Long tenantId = currentTenantId();
        List<Map<String, Object>> stats = registrationMapper.getRegistrationStats(conferenceId, tenantId);

        Map<String, Object> result = new HashMap<>();
        result.put("pending", 0);
        result.put("approved", 0);
        result.put("rejected", 0);
        result.put("cancelled", 0);
        result.put("checkedin", 0);

        for (Map<String, Object> stat : stats) {
            Integer status = Integer.parseInt(String.valueOf(stat.get("status")));
            Integer count = Integer.parseInt(String.valueOf(stat.get("count")));
            result.put(mapStatusKey(status), count);
        }
        return result;
    }

    @Override
    public Map<String, String> ocrIdCard(String photoUrl) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("photoUrl", photoUrl);

        if (!StringUtils.hasText(photoUrl)) {
            result.put("ocrStatus", "FAIL");
            result.put("reason", "身份证照片地址为空");
            return result;
        }

        try {
            Map<String, Object> req = new HashMap<>();
            req.put("apikey", ocrApiKey);
            req.put("url", photoUrl);
            req.put("language", "chs");
            req.put("isOverlayRequired", false);

            String response = cn.hutool.http.HttpUtil.post(ocrApiUrl, req);
            JSONObject body = JSON.parseObject(response);
            JSONArray parsedResults = body.getJSONArray("ParsedResults");

            if (parsedResults == null || parsedResults.isEmpty()) {
                result.put("ocrStatus", "FAIL");
                result.put("reason", "OCR未识别到有效文本");
                result.put("raw", response);
                return result;
            }

            String text = parsedResults.getJSONObject(0).getString("ParsedText");
            result.put("rawText", nullable(text));

            String idCard = extractIdCard(text);
            String name = extractName(text);

            result.put("idCard", nullable(idCard));
            result.put("name", nullable(name));
            result.put("confidence", (StringUtils.hasText(idCard) && StringUtils.hasText(name)) ? "0.95" : "0.70");

            if (StringUtils.hasText(idCard) && StringUtils.hasText(name)) {
                result.put("ocrStatus", "PASS");
                result.put("reason", "证件信息识别成功");
            } else if (StringUtils.hasText(idCard) || StringUtils.hasText(name)) {
                result.put("ocrStatus", "REVIEW");
                result.put("reason", "识别部分成功，需人工复核");
            } else {
                result.put("ocrStatus", "FAIL");
                result.put("reason", "未识别到姓名和身份证号");
            }
            result.put("raw", response);
            return result;
        } catch (Exception e) {
            log.error("OCR识别失败, photoUrl={}", photoUrl, e);
            result.put("ocrStatus", "FAIL");
            result.put("reason", "OCR识别异常:" + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, String> uploadRegistrationFile(Long conferenceId, String fileType, MultipartFile file, String requestBaseUrl) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "上传文件不能为空");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "单个文件不能超过10MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = ".jpg";
        if (StringUtils.hasText(originalName) && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
        }

        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM/dd"));
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "registration", String.valueOf(conferenceId), datePath);
        Files.createDirectories(uploadDir);

        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        byte[] bytes = Files.readAllBytes(target);
        String sha256 = sha256(bytes);
        String relativePath = String.join("/", "registration", String.valueOf(conferenceId), datePath.replace("\\", "/"), fileName);

        Map<String, String> data = new HashMap<>();
        data.put("fileType", fileType);
        data.put("fileName", fileName);
        data.put("fileUrl", requestBaseUrl + "/uploads/" + relativePath);
        data.put("storagePath", target.toString());
        data.put("sha256", sha256);
        data.put("size", String.valueOf(file.getSize()));
        return data;
    }

    @Override
    public List<RegistrationFieldConfigRequest.FieldConfig> getRegistrationFormFields(Long conferenceId) {
        Long tenantId = currentTenantId();
        String configText = registrationMapper.getMeetingRegistrationConfig(conferenceId, tenantId);

        if (!StringUtils.hasText(configText)) {
            log.warn("会议[{}]未在数据库中配置动态字段，使用系统默认字段配置", conferenceId);
            return defaultFormFields();
        }

        JSONObject config = safeObject(configText);
        JSONArray fields = config.getJSONArray("formFields");
        if (fields == null || fields.isEmpty()) {
            log.warn("会议[{}]的字段配置为空，使用系统默认字段配置", conferenceId);
            return defaultFormFields();
        }

        List<RegistrationFieldConfigRequest.FieldConfig> list = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            JSONObject obj = fields.getJSONObject(i);
            RegistrationFieldConfigRequest.FieldConfig item = new RegistrationFieldConfigRequest.FieldConfig();
            item.setKey(obj.getString("key"));
            item.setLabel(obj.getString("label"));
            item.setType(obj.getString("type"));
            item.setRequired(obj.getBooleanValue("required"));
            item.setPlaceholder(obj.getString("placeholder"));
            item.setSort(obj.getInteger("sort"));
            item.setEnabled(!obj.containsKey("enabled") || obj.getBooleanValue("enabled"));
            item.setValidationPattern(obj.getString("validationPattern"));
            item.setValidationMessage(obj.getString("validationMessage"));
            JSONArray options = obj.getJSONArray("options");
            if (options != null) {
                item.setOptions(options.toJavaList(String.class));
            }
            list.add(item);
        }
        
        log.info("会议[{}]成功从数据库加载{}个字段配置: {}", conferenceId, list.size(), 
            list.stream().map(RegistrationFieldConfigRequest.FieldConfig::getKey).collect(java.util.stream.Collectors.toList()));
        return list;
    }

    @Override
    public void saveRegistrationFormFields(RegistrationFieldConfigRequest request) {
        Long tenantId = currentTenantId();
        String configText = registrationMapper.getMeetingRegistrationConfig(request.getConferenceId(), tenantId);
        JSONObject config = safeObject(configText);

        JSONArray fields = new JSONArray();
        if (request.getFields() != null) {
            for (RegistrationFieldConfigRequest.FieldConfig item : request.getFields()) {
                JSONObject obj = new JSONObject();
                obj.put("key", item.getKey());
                obj.put("label", item.getLabel());
                obj.put("type", item.getType());
                obj.put("required", Boolean.TRUE.equals(item.getRequired()));
                obj.put("placeholder", item.getPlaceholder());
                obj.put("sort", item.getSort() == null ? 0 : item.getSort());
                obj.put("enabled", item.getEnabled() == null || item.getEnabled());
                obj.put("validationPattern", item.getValidationPattern());
                obj.put("validationMessage", item.getValidationMessage());
                obj.put("options", item.getOptions() == null ? new JSONArray() : item.getOptions());
                fields.add(obj);
            }
        }
        config.put("formFields", fields);
        if (!config.containsKey("mode")) {
            config.put("mode", "open");
        }

        int rows = registrationMapper.updateMeetingRegistrationConfig(request.getConferenceId(), tenantId, config.toJSONString());
        if (rows <= 0) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "会议不存在或无权限更新报名配置");
        }
    }

    @Override
    public Registration queryByConferenceAndPhone(Long conferenceId, String phone) {
        Long tenantId = currentTenantId();
        
        log.info("=== 查询报名信息开始 ===");
        log.info("查询条件 - 会议ID: {}, 手机号: {}, 租户ID: {}", conferenceId, phone, tenantId);

        Registration result = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getConferenceId, conferenceId)
                .eq(Registration::getPhone, phone)
                .eq(Registration::getTenantId, tenantId)
                .eq(Registration::getDeleted, 0)
                .orderByDesc(Registration::getRegistrationTime)
                .last("LIMIT 1"));
        
        if (result != null) {
            log.info("查询成功 - 找到报名记录，ID: {}, 报名人: {}", result.getId(), result.getRealName());
        } else {
            log.warn("查询无结果 - 未找到对应报名记录，会议ID: {}, 手机号: {}, 租户ID: {}", conferenceId, phone, tenantId);
        }
        
        return result;
    }

    @Override
    public String mapStatusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case STATUS_PENDING -> "pending";
            case STATUS_APPROVED -> "approved";
            case STATUS_REJECTED -> "rejected";
            case STATUS_CANCELLED -> "cancelled";
            case STATUS_CHECKED_IN -> "checkedin";
            default -> "unknown";
        };
    }

    @Override
    public LinkedHashMap<String, Object> validateOcrResult(Registration registration, Map<String, String> ocrResult) {
        LinkedHashMap<String, Object> rule = new LinkedHashMap<>();
        String ocrName = ocrResult.getOrDefault("name", "");
        String ocrIdCard = ocrResult.getOrDefault("idCard", "");

        boolean idFormat = ID_CARD_PATTERN.matcher(ocrIdCard).find();
        boolean nameMatch = StringUtils.hasText(registration.getRealName()) && StringUtils.hasText(ocrName) && registration.getRealName().trim().equals(ocrName.trim());
        boolean idMatch = StringUtils.hasText(registration.getIdCard()) && StringUtils.hasText(ocrIdCard)
                && registration.getIdCard().equalsIgnoreCase(ocrIdCard);

        rule.put("idFormat", idFormat);
        rule.put("nameMatch", nameMatch);
        rule.put("idMatch", idMatch);
        rule.put("score", (idFormat ? 40 : 0) + (nameMatch ? 30 : 0) + (idMatch ? 30 : 0));
        rule.put("finalStatus", idFormat && nameMatch && idMatch ? "PASS" : (idFormat ? "REVIEW" : "FAIL"));
        return rule;
    }

    @Override
    public byte[] generatePdfRoster(Long conferenceId, String coverImage, String remarks) {
        return new byte[0];
    }

    private Long currentTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId == null ? DEFAULT_TENANT_ID : tenantId;
    }

    private Integer mapStatusCode(String status) {
        if (!StringUtils.hasText(status)) {
            return STATUS_PENDING;
        }
        return switch (status.toLowerCase()) {
            case "approved", "pass", "passed" -> STATUS_APPROVED;
            case "rejected", "fail", "failed" -> STATUS_REJECTED;
            case "cancelled", "canceled" -> STATUS_CANCELLED;
            case "checkedin", "checkin" -> STATUS_CHECKED_IN;
            default -> STATUS_PENDING;
        };
    }

    private String mapStatusKey(Integer status) {
        return mapStatusText(status);
    }

    private String nullable(String value) {
        return value == null ? "" : value;
    }

    private String extractOcrStatus(String registrationData) {
        if (!StringUtils.hasText(registrationData)) {
            return "-";
        }
        JSONObject json = safeObject(registrationData);
        JSONObject ocr = json.getJSONObject("ocr");
        if (ocr == null) {
            return "-";
        }
        return ocr.getString("ocrStatus");
    }

    private JSONObject safeObject(String jsonText) {
        if (!StringUtils.hasText(jsonText)) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(jsonText);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private String mergeRegistrationDataWithFiles(String registrationData, String idCardPhotoUrl, String diplomaPhotoUrl) {
        JSONObject data = safeObject(registrationData);
        JSONObject files = data.getJSONObject("files");
        if (files == null) {
            files = new JSONObject();
            data.put("files", files);
        }
        if (StringUtils.hasText(idCardPhotoUrl)) {
            files.put("idCardPhotoUrl", idCardPhotoUrl);
        }
        if (StringUtils.hasText(diplomaPhotoUrl)) {
            files.put("diplomaPhotoUrl", diplomaPhotoUrl);
        }
        return data.toJSONString();
    }

    private void validateDynamicFields(String registrationData, List<RegistrationFieldConfigRequest.FieldConfig> fields) {
        if (fields == null || fields.isEmpty()) {
            log.debug("没有需要验证的动态字段");
            return;
        }
        JSONObject data = safeObject(registrationData);
        log.debug("开始验证动态字段，registrationData中包含的字段: {}", data.keySet());
        
        for (RegistrationFieldConfigRequest.FieldConfig field : fields) {
            if (!Boolean.TRUE.equals(field.getEnabled())) {
                log.trace("字段[{}]未启用，跳过验证", field.getKey());
                continue;
            }
            String key = field.getKey();
            if (!StringUtils.hasText(key)) {
                log.warn("字段配置的key为空");
                continue;
            }
            String value = data.getString(key);
            if (Boolean.TRUE.equals(field.getRequired()) && !StringUtils.hasText(value)) {
                log.error("必填字段[{}]{}为空", key, field.getLabel());
                throw new BusinessException(ResultCode.PARAM_ERROR, "动态字段必填: " + (StringUtils.hasText(field.getLabel()) ? field.getLabel() : key));
            }
            if (StringUtils.hasText(value) && StringUtils.hasText(field.getValidationPattern())) {
                if (!value.matches(field.getValidationPattern())) {
                    log.error("字段[{}]{}的值[{}]不匹配正则[{}]", key, field.getLabel(), value, field.getValidationPattern());
                    throw new BusinessException(ResultCode.PARAM_ERROR,
                            StringUtils.hasText(field.getValidationMessage()) ? field.getValidationMessage() : "字段校验失败: " + key);
                }
            }
            log.trace("字段[{}]{}验证通过，值: {}", key, field.getLabel(), value);
        }
        log.debug("所有动态字段验证通过");
    }

    private List<RegistrationFieldConfigRequest.FieldConfig> defaultFormFields() {
        List<RegistrationFieldConfigRequest.FieldConfig> defaults = new ArrayList<>();
        defaults.add(systemField("name", "姓名", "text", true, 1));
        defaults.add(systemField("phone", "手机号", "text", true, 2));
        defaults.add(systemField("unit", "单位", "text", true, 3));
        defaults.add(systemField("position", "职位", "text", true, 4));
        return defaults;
    }

    private RegistrationFieldConfigRequest.FieldConfig systemField(String key, String label, String type, boolean required, int sort) {
        RegistrationFieldConfigRequest.FieldConfig cfg = new RegistrationFieldConfigRequest.FieldConfig();
        cfg.setKey(key);
        cfg.setLabel(label);
        cfg.setType(type);
        cfg.setRequired(required);
        cfg.setEnabled(true);
        cfg.setSort(sort);
        return cfg;
    }

    private String extractIdCard(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        Matcher matcher = ID_CARD_PATTERN.matcher(text.replaceAll("\\s+", ""));
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private String extractName(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        Matcher matcher = NAME_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String candidate = line.trim().replace("姓 名", "").replace("姓名", "").replace("：", "").replace(":", "").trim();
            if (candidate.matches("[\\u4e00-\\u9fa5·]{2,20}")) {
                return candidate;
            }
        }
        return "";
    }

    private String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return UUID.nameUUIDFromBytes(data).toString().replace("-", "");
        }
    }

    @Override
    public byte[] generateQrCode(String text, int width, int height) throws IOException {
        try {
            com.google.zxing.BarcodeFormat format = com.google.zxing.BarcodeFormat.QR_CODE;
            com.google.zxing.EncodeHintType hintType = com.google.zxing.EncodeHintType.ERROR_CORRECTION;
            com.google.zxing.qrcode.decoder.ErrorCorrectionLevel level = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L;
            java.util.Map<com.google.zxing.EncodeHintType, Object> hints = new java.util.HashMap<>();
            hints.put(hintType, level);
            hints.put(com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(com.google.zxing.EncodeHintType.MARGIN, 2);
        
            com.google.zxing.MultiFormatWriter writer = new com.google.zxing.MultiFormatWriter();
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(text, format, width, height, hints);
            com.google.zxing.client.j2se.BufferedImageLuminanceSource source = 
                    new com.google.zxing.client.j2se.BufferedImageLuminanceSource(
                        com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix));
            java.awt.image.BufferedImage image = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);
        
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);
            baos.close();
            return baos.toByteArray();
        } catch (com.google.zxing.WriterException e) {
            throw new IOException("QR码生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查registrationData中是否已存在OCR识别结果
     */
    private boolean hasOcrResult(String registrationData) {
        if (!StringUtils.hasText(registrationData)) {
            return false;
        }
        JSONObject data = safeObject(registrationData);
        JSONObject ocr = data.getJSONObject("ocr");
        return ocr != null && !ocr.isEmpty();
    }

    @Override
    public String getOrGenerateQrCode(Long conferenceId, String registrationUrl) throws IOException {
        // 生成QR码PNG字节数组
        byte[] qrCodeBytes = generateQrCode(registrationUrl, 220, 220);
    
        // Base64编码为Data URL
        String base64Encoded = java.util.Base64.getEncoder().encodeToString(qrCodeBytes);
        return "data:image/png;base64," + base64Encoded;
    }
}
