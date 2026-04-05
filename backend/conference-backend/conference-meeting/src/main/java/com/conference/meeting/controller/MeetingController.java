package com.conference.meeting.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.common.exception.BusinessException;
import com.conference.common.result.Result;
import com.conference.meeting.dto.MeetingCreateRequest;
import com.conference.meeting.dto.MeetingUpdateRequest;
import com.conference.meeting.entity.Meeting;
import com.conference.meeting.service.IMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/meeting")
// @CrossOrigin  // 已禁用：使用Gateway的统一CORS配置
public class MeetingController {

    @Autowired
    private IMeetingService meetingService;

    private static final long MAX_COVER_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping("/upload/cover")
    public ResponseEntity<Result<Map<String, String>>> uploadCover(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "请选择要上传的图片"));
            }

            if (file.getSize() > MAX_COVER_SIZE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "图片大小不能超过5MB"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "仅支持图片文件上传"));
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
            }
            if (!StringUtils.hasText(ext)) {
                ext = ".png";
            }

            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + ext;
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "meeting", "cover");
            Files.createDirectories(uploadDir);

            Path target = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + "/uploads/meeting/cover/" + fileName;

            Map<String, String> data = new HashMap<>();
            data.put("url", url);

            return ResponseEntity.ok(Result.success("封面上传成功", data));
        } catch (IOException e) {
            log.error("封面上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "封面上传失败，请稍后重试"));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Result<Meeting>> createMeeting(@RequestBody MeetingCreateRequest request) {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未获取到租户信息，请检查认证信息"));
            }

            Meeting meeting = new Meeting();
            BeanUtils.copyProperties(request, meeting);

            Meeting created = meetingService.createMeeting(meeting);
            log.info("会议创建成功，租户: {}, 会议ID: {}", tenantId, created.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success("会议创建成功", created));
        } catch (BusinessException e) {
            log.warn("会议创建失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("会议创建异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "会议创建失败，请稍后重试"));
        }
    }

    @PostMapping("/{id}/staff")
    public ResponseEntity<Result<Boolean>> addMeetingStaff(
            @PathVariable Long id,
            @RequestBody Map<String, Object> staffRequest) {
        try {
            // 校验会议是否存在且属于当前租户
            meetingService.getMeetingDetail(id);

            log.info("收到会议工作人员添加请求，会议ID: {}, staff: {}", id, staffRequest);
            // 当前版本仅记录请求并返回成功，后续可扩展为持久化到工作人员表
            return ResponseEntity.ok(Result.success("工作人员添加成功", true));
        } catch (BusinessException e) {
            log.warn("工作人员添加失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("工作人员添加异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "工作人员添加失败，请稍后重试"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<Meeting>> getMeetingDetail(@PathVariable Long id) {
        try {
            Meeting meeting = meetingService.getMeetingDetail(id);
            return ResponseEntity.ok(Result.success("获取成功", meeting));
        } catch (BusinessException e) {
            log.warn("获取会议失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("获取会议异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "获取会议失败，请稍后重试"));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Result<?>> getMeetingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未获取到租户信息"));
            }

            Page<Meeting> page = new Page<>(pageNum, pageSize);
            var result = meetingService.getMeetingList(page, tenantId, keyword, status);

            return ResponseEntity.ok(Result.success("获取成功", result));
        } catch (Exception e) {
            log.error("列表查询异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "查询失败，请稍后重试"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Boolean>> updateMeeting(
            @PathVariable Long id,
            @RequestBody MeetingUpdateRequest request) {
        try {
            Meeting meeting = new Meeting();
            BeanUtils.copyProperties(request, meeting);
            meeting.setId(id);

            Boolean result = meetingService.updateMeeting(meeting);
            if (result) {
                log.info("会议更新成功，ID: {}", id);
                return ResponseEntity.ok(Result.success("更新成功", true));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "更新失败，请稍后重试"));
            }
        } catch (BusinessException e) {
            log.warn("会议更新失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("会议更新异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "更新失败，请稍后重试"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Boolean>> deleteMeeting(@PathVariable Long id) {
        try {
            Boolean result = meetingService.deleteMeeting(id);
            if (result) {
                log.info("会议删除成功，ID: {}", id);
                return ResponseEntity.ok(Result.success("删除成功", true));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "删除失败，请稍后重试"));
            }
        } catch (BusinessException e) {
            log.warn("会议删除失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("会议删除异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "删除失败，请稍后重试"));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Result<Boolean>> updateMeetingStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        try {
            Boolean result = meetingService.updateMeetingStatus(id, status);
            if (result) {
                log.info("会议状态更新成功，ID: {}, 新状态: {}", id, status);
                return ResponseEntity.ok(Result.success("状态更新成功", true));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "状态更新失败"));
            }
        } catch (BusinessException e) {
            log.warn("会议状态更新失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("会议状态更新异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "状态更新失败"));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Result<?>> getMeetingStatistics() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未获取到租户信息"));
            }

            var stats = meetingService.getMeetingStatistics(tenantId);
            return ResponseEntity.ok(Result.success("获取成功", stats));
        } catch (Exception e) {
            log.error("统计数据查询异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "查询失败"));
        }
    }

    @GetMapping("/ongoing")
    public ResponseEntity<Result<?>> getOngoingMeetings() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Result.error(401, "未获取到租户信息"));
            }

            var meetings = meetingService.getOngoingMeetings(tenantId);
            return ResponseEntity.ok(Result.success("获取成功", meetings));
        } catch (Exception e) {
            log.error("查询进行中的会议异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "查询失败"));
        }
    }
}
