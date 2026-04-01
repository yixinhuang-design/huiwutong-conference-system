package com.conference.registration.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.common.result.Result;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.registration.dto.RegistrationAuditRequest;
import com.conference.registration.dto.RegistrationFieldConfigRequest;
import com.conference.registration.dto.RegistrationRequest;
import com.conference.registration.entity.Registration;
import com.conference.registration.service.RegistrationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报名管理 Controller
 */
@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {
    
    private final RegistrationService registrationService;
    
    /**
     * 提交报名
     */
    @PostMapping("/submit")
    public Result<Registration> submitRegistration(@Valid @RequestBody RegistrationRequest request) {
        Registration registration = registrationService.submitRegistration(request);
        return Result.ok(registration);
    }
    
    /**
     * 获取报名详情
     */
    @GetMapping("/{id}")
    public Result<Registration> getRegistration(@PathVariable Long id) {
        Registration registration = registrationService.getRegistrationDetail(id);
        return Result.ok(registration);
    }
    
    /**
     * 分页查询报名列表
     */
    @GetMapping("/list")
    public Result<Page<Registration>> listRegistrations(
            @RequestParam Long conferenceId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<Registration> result = registrationService.listRegistrations(conferenceId, page, pageSize);
        return Result.ok(result);
    }

    /**
     * 根据手机号查询报名状态
     */
    @GetMapping("/query")
    public Result<Registration> queryByPhone(
            @RequestParam Long conferenceId,
            @RequestParam @NotBlank String phone) {
        Registration registration = registrationService.queryByConferenceAndPhone(conferenceId, phone);
        return Result.ok(registration);
    }
    
    /**
     * 审核报名
     */
    @PostMapping("/audit")
    public Result<String> auditRegistration(
            @Valid @RequestBody RegistrationAuditRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long auditorId) {
        if (auditorId == null) {
            auditorId = TenantContextHolder.getUserId();
        }
        registrationService.auditRegistration(request, auditorId != null ? auditorId : 0L);
        return Result.ok("审核成功");
    }
    
    /**
     * 批量审核报名
     */
    @PostMapping("/batchAudit")
    public Result<String> batchAuditRegistration(
            @Valid @RequestBody List<RegistrationAuditRequest> requests,
            @RequestHeader(value = "X-User-Id", required = false) Long auditorId) {
        if (auditorId == null) {
            auditorId = TenantContextHolder.getUserId();
        }
        registrationService.batchAuditRegistration(requests, auditorId != null ? auditorId : 0L);
        return Result.ok("批量审核成功");
    }
    
    /**
     * 导出报名名册（Excel）
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportRegistrationList(@RequestParam Long conferenceId) {
        byte[] excelBytes = registrationService.exportRegistrationList(conferenceId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=registration_" + conferenceId + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
    
    /**
     * 获取报名统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(@RequestParam Long conferenceId) {
        Map<String, Object> stats = registrationService.getRegistrationStats(conferenceId);
        return Result.ok(stats);
    }
    
    /**
     * OCR 识别身份证
     */
    @PostMapping("/ocr/idCard")
    public Result<Map<String, String>> ocrIdCard(@RequestParam String photoUrl) {
        Map<String, String> result = registrationService.ocrIdCard(photoUrl);
        return Result.ok(result);
    }

    /**
     * 上传报名附件（身份证/学历证明）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadRegistrationFile(
            @RequestParam Long conferenceId,
            @RequestParam(defaultValue = "idCard") String fileType,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        String base = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        Map<String, String> data = registrationService.uploadRegistrationFile(conferenceId, fileType, file, base);
        return Result.ok("上传成功", data);
    }

    /**
     * 获取动态报名字段配置
     */
    @GetMapping("/form/fields")
    public Result<List<RegistrationFieldConfigRequest.FieldConfig>> getFormFields(@RequestParam Long conferenceId) {
        return Result.ok(registrationService.getRegistrationFormFields(conferenceId));
    }

    /**
     * 保存动态报名字段配置
     */
    @PostMapping("/form/fields/save")
    public Result<String> saveFormFields(@Valid @RequestBody RegistrationFieldConfigRequest request) {
        registrationService.saveRegistrationFormFields(request);
        return Result.ok("保存成功");
    }

    /**
     * 获取OCR规则校验结果（根据报名ID）
     */
    @GetMapping("/ocr/result")
    public Result<LinkedHashMap<String, Object>> getOcrResult(@RequestParam Long registrationId) {
        Registration registration = registrationService.getRegistrationDetail(registrationId);
        if (registration == null) {
            return Result.error(404, "报名记录不存在");
        }

        String photoUrl = registration.getIdCardPhotoUrl();
        if ((photoUrl == null || photoUrl.isBlank()) && registration.getRegistrationData() != null) {
            try {
                JSONObject data = JSON.parseObject(registration.getRegistrationData());
                JSONObject files = data.getJSONObject("files");
                if (files != null) {
                    photoUrl = files.getString("idCardPhotoUrl");
                }
            } catch (Exception ignored) {
            }
        }

        Map<String, String> ocr = registrationService.ocrIdCard(photoUrl);
        LinkedHashMap<String, Object> validated = registrationService.validateOcrResult(registration, ocr);
        validated.put("ocr", ocr);
        validated.put("statusText", registrationService.mapStatusText(registration.getStatus()));
        return Result.ok(validated);
    }
    
    /**
     * 生成名册（HTML格式，支持浏览器打印为PDF）
     */
    @PostMapping("/pdf/roster")
    public ResponseEntity<byte[]> generatePdfRoster(
            @RequestParam Long conferenceId,
            @RequestParam(required = false) String coverImage,
            @RequestParam(required = false) String remarks) {
        
        byte[] pdfBytes = registrationService.generatePdfRoster(conferenceId, coverImage, remarks);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"roster_" + conferenceId + ".html\"")
                .contentType(MediaType.TEXT_HTML)
                .body(pdfBytes);
    }

    /**
     * 生成会议注册二维码 (base64 data URL)
     */
    @GetMapping("/qr/generate")
    public Result<String> generateQrCode(
            @RequestParam Long conferenceId,
            @RequestParam(required = false) String registrationUrl,
            jakarta.servlet.http.HttpServletRequest request) throws IOException {
        
        if (!StringUtils.hasText(registrationUrl)) {
            // 动态获取请求来源的域名/IP
            String scheme = request.getHeader("X-Forwarded-Proto");
            if (!StringUtils.hasText(scheme)) scheme = request.getScheme();
            String host = request.getHeader("X-Forwarded-Host");
            if (!StringUtils.hasText(host)) host = request.getHeader("Host");
            if (!StringUtils.hasText(host)) host = request.getServerName() + ":" + request.getServerPort();
            registrationUrl = scheme + "://" + host + "/app/learner/scan-register.html?meetingId=" + conferenceId;
        }
        
        String qrCodeDataUrl = registrationService.getOrGenerateQrCode(conferenceId, registrationUrl);
        return Result.ok("QR码生成成功", qrCodeDataUrl);
    }
}
