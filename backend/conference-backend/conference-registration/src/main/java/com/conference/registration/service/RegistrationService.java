package com.conference.registration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.conference.registration.dto.RegistrationAuditRequest;
import com.conference.registration.dto.RegistrationFieldConfigRequest;
import com.conference.registration.dto.RegistrationRequest;
import com.conference.registration.entity.Registration;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报名服务接口
 */
public interface RegistrationService extends IService<Registration> {
    
    /**
     * 提交报名
     */
    Registration submitRegistration(RegistrationRequest request);
    
    /**
     * 获取报名详情
     */
    Registration getRegistrationDetail(Long registrationId);
    
    /**
     * 获取会议报名列表（分页）
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<Registration> listRegistrations(
        Long conferenceId, int page, int pageSize
    );
    
    /**
     * 审核报名
     */
    void auditRegistration(RegistrationAuditRequest request, Long auditorId);
    
    /**
     * 批量审核
     */
    void batchAuditRegistration(List<RegistrationAuditRequest> requests, Long auditorId);
    
    /**
     * 导出报名名册 (Excel)
     */
    byte[] exportRegistrationList(Long conferenceId);
    
    /**
     * 获取报名统计
     */
    java.util.Map<String, Object> getRegistrationStats(Long conferenceId);
    
    /**
     * OCR 识别身份证
     */
    java.util.Map<String, String> ocrIdCard(String photoUrl);

    /**
     * 上传报名附件（身份证/学历证明）
     */
    Map<String, String> uploadRegistrationFile(Long conferenceId, String fileType, MultipartFile file, String requestBaseUrl) throws IOException;

    /**
     * 获取会议报名动态字段配置
     */
    List<RegistrationFieldConfigRequest.FieldConfig> getRegistrationFormFields(Long conferenceId);

    /**
     * 保存会议报名动态字段配置
     */
    void saveRegistrationFormFields(RegistrationFieldConfigRequest request);

    /**
     * 根据手机号查询报名状态
     */
    Registration queryByConferenceAndPhone(Long conferenceId, String phone);

    /**
     * 状态文本映射
     */
    String mapStatusText(Integer status);

    /**
     * OCR结果规则校验
     */
    LinkedHashMap<String, Object> validateOcrResult(Registration registration, Map<String, String> ocrResult);
    
    /**
     * 生成 PDF 名册
     */
    byte[] generatePdfRoster(Long conferenceId, String coverImage, String remarks);

        /**
         * 生成二维码 (PNG字节数组)
         */
        byte[] generateQrCode(String text, int width, int height) throws IOException;

        /**
         * 获取会议注册二维码 (如果不存在则生成并存储)
         */
        String getOrGenerateQrCode(Long conferenceId, String registrationUrl) throws IOException;
}
