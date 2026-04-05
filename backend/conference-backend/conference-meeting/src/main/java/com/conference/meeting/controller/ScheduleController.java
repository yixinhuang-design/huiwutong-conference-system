package com.conference.meeting.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.common.exception.BusinessException;
import com.conference.common.result.Result;
import com.conference.common.tenant.TenantContextHolder;
import com.conference.meeting.dto.ScheduleCreateRequest;
import com.conference.meeting.dto.ScheduleResponse;
import com.conference.meeting.dto.ScheduleUpdateRequest;
import com.conference.meeting.entity.Schedule;
import com.conference.meeting.service.IScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日程管理API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule")
// @CrossOrigin  // 已禁用：使用Gateway的统一CORS配置
public class ScheduleController {

    @Autowired
    private IScheduleService scheduleService;

    /**
     * 创建日程
     * POST /api/schedule/create?meetingId=123
     */
    @PostMapping("/create")
    public ResponseEntity<Result<ScheduleResponse>> createSchedule(
            @RequestParam Long meetingId,
            @RequestBody ScheduleCreateRequest request) {
        try {
            log.info("创建日程 - meetingId: {}, title: {}", meetingId, request.getTitle());
            
            if (meetingId == null || meetingId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "会议ID无效"));
            }

            ScheduleResponse response = scheduleService.createSchedule(meetingId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success(response));
        } catch (BusinessException e) {
            log.warn("创建日程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("创建日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 更新日程
     * PUT /api/schedule/123
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<ScheduleResponse>> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleUpdateRequest request) {
        try {
            log.info("更新日程 - id: {}", id);
            
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "日程ID无效"));
            }

            ScheduleResponse response = scheduleService.updateSchedule(id, request);
            return ResponseEntity.ok(Result.success(response));
        } catch (BusinessException e) {
            log.warn("更新日程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("更新日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 删除日程
     * DELETE /api/schedule/123
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteSchedule(@PathVariable Long id) {
        try {
            log.info("删除日程 - id: {}", id);
            
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "日程ID无效"));
            }

            scheduleService.deleteSchedule(id);
            return ResponseEntity.ok(Result.success(null));
        } catch (BusinessException e) {
            log.warn("删除日程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("删除日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 获取日程详情
     * GET /api/schedule/123
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<ScheduleResponse>> getSchedule(@PathVariable Long id) {
        try {
            log.info("获取日程详情 - id: {}", id);
            
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "日程ID无效"));
            }

            ScheduleResponse response = scheduleService.getScheduleDetail(id);
            return ResponseEntity.ok(Result.success(response));
        } catch (BusinessException e) {
            log.warn("获取日程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("获取日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 分页查询日程列表
     * GET /api/schedule/list?meetingId=123&pageNo=1&pageSize=10
     */
    @GetMapping("/list")
    public ResponseEntity<Result<com.baomidou.mybatisplus.core.metadata.IPage<ScheduleResponse>>> listSchedules(
            @RequestParam Long meetingId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            log.info("查询日程列表 - meetingId: {}, pageNo: {}, pageSize: {}", meetingId, pageNo, pageSize);
            
            if (meetingId == null || meetingId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "会议ID无效"));
            }

            Page<Schedule> page = new Page<>(pageNo, pageSize);
            var result = scheduleService.listSchedules(meetingId, page);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("查询日程列表异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 获取全部日程（不分页）
     * GET /api/schedule/all?meetingId=123
     */
    @GetMapping("/all")
    public ResponseEntity<Result<List<ScheduleResponse>>> allSchedules(@RequestParam Long meetingId) {
        try {
            log.info("查询全部日程 - meetingId: {}", meetingId);
            
            if (meetingId == null || meetingId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Result.error(400, "会议ID无效"));
            }

            List<ScheduleResponse> schedules = scheduleService.listAllSchedules(meetingId);
            return ResponseEntity.ok(Result.success(schedules));
        } catch (Exception e) {
            log.error("查询全部日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 查询需要签到的日程
     * GET /api/schedule/need-checkin?meetingId=123
     */
    @GetMapping("/need-checkin")
    public ResponseEntity<Result<List<ScheduleResponse>>> needCheckinSchedules(@RequestParam Long meetingId) {
        try {
            log.info("查询需要签到的日程 - meetingId: {}", meetingId);
            
            List<ScheduleResponse> schedules = scheduleService.listNeedCheckinSchedules(meetingId);
            return ResponseEntity.ok(Result.success(schedules));
        } catch (Exception e) {
            log.error("查询需要签到的日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 查询需要提醒的日程
     * GET /api/schedule/need-reminder?meetingId=123
     */
    @GetMapping("/need-reminder")
    public ResponseEntity<Result<List<ScheduleResponse>>> needReminderSchedules(@RequestParam Long meetingId) {
        try {
            log.info("查询需要提醒的日程 - meetingId: {}", meetingId);
            
            List<ScheduleResponse> schedules = scheduleService.listNeedReminderSchedules(meetingId);
            return ResponseEntity.ok(Result.success(schedules));
        } catch (Exception e) {
            log.error("查询需要提醒的日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 查询进行中的日程
     * GET /api/schedule/ongoing?meetingId=123
     */
    @GetMapping("/ongoing")
    public ResponseEntity<Result<List<ScheduleResponse>>> ongoingSchedules(@RequestParam Long meetingId) {
        try {
            log.info("查询进行中的日程 - meetingId: {}", meetingId);
            
            List<ScheduleResponse> schedules = scheduleService.listOngoingSchedules(meetingId);
            return ResponseEntity.ok(Result.success(schedules));
        } catch (Exception e) {
            log.error("查询进行中的日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 查询即将开始的日程（30分钟内）
     * GET /api/schedule/upcoming?meetingId=123
     */
    @GetMapping("/upcoming")
    public ResponseEntity<Result<List<ScheduleResponse>>> upcomingSchedules(@RequestParam Long meetingId) {
        try {
            log.info("查询即将开始的日程 - meetingId: {}", meetingId);
            
            List<ScheduleResponse> schedules = scheduleService.listUpcomingSchedules(meetingId);
            return ResponseEntity.ok(Result.success(schedules));
        } catch (Exception e) {
            log.error("查询即将开始的日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 查询时间范围内的日程
     * GET /api/schedule/by-time-range?meetingId=123&startTime=2024-06-15 09:00:00&endTime=2024-06-15 18:00:00
     */
    @GetMapping("/by-time-range")
    public ResponseEntity<Result<List<ScheduleResponse>>> schedulesByTimeRange(
            @RequestParam Long meetingId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            log.info("查询时间范围内的日程 - meetingId: {}, startTime: {}, endTime: {}", meetingId, startTime, endTime);
            
            List<ScheduleResponse> schedules = scheduleService.listSchedulesByTimeRange(meetingId, startTime, endTime);
            return ResponseEntity.ok(Result.success(schedules));
        } catch (Exception e) {
            log.error("查询时间范围内的日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 获取下一个日程
     * GET /api/schedule/next?meetingId=123
     */
    @GetMapping("/next")
    public ResponseEntity<Result<ScheduleResponse>> nextSchedule(@RequestParam Long meetingId) {
        try {
            log.info("获取下一个日程 - meetingId: {}", meetingId);
            
            ScheduleResponse schedule = scheduleService.getNextSchedule(meetingId);
            return ResponseEntity.ok(Result.success(schedule));
        } catch (Exception e) {
            log.error("获取下一个日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 获取当前日程
     * GET /api/schedule/current?meetingId=123
     */
    @GetMapping("/current")
    public ResponseEntity<Result<ScheduleResponse>> currentSchedule(@RequestParam Long meetingId) {
        try {
            log.info("获取当前日程 - meetingId: {}", meetingId);
            
            ScheduleResponse schedule = scheduleService.getCurrentSchedule(meetingId);
            return ResponseEntity.ok(Result.success(schedule));
        } catch (Exception e) {
            log.error("获取当前日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 发布日程
     * PUT /api/schedule/123/publish
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<Result<Void>> publishSchedule(@PathVariable Long id) {
        try {
            log.info("发布日程 - id: {}", id);
            
            scheduleService.publishSchedule(id);
            return ResponseEntity.ok(Result.success(null));
        } catch (BusinessException e) {
            log.warn("发布日程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("发布日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 取消日程
     * PUT /api/schedule/123/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Result<Void>> cancelSchedule(@PathVariable Long id) {
        try {
            log.info("取消日程 - id: {}", id);
            
            scheduleService.cancelSchedule(id);
            return ResponseEntity.ok(Result.success(null));
        } catch (BusinessException e) {
            log.warn("取消日程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("取消日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 复制日程
     * POST /api/schedule/123/duplicate
     */
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<Result<ScheduleResponse>> duplicateSchedule(@PathVariable Long id) {
        try {
            log.info("复制日程 - id: {}", id);
            
            ScheduleResponse response = scheduleService.duplicateSchedule(id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success(response));
        } catch (BusinessException e) {
            log.warn("复制日程失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("复制日程异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    /**
     * 统计日程数量
     * GET /api/schedule/count?meetingId=123
     */
    @GetMapping("/count")
    public ResponseEntity<Result<Integer>> countSchedules(@RequestParam Long meetingId) {
        try {
            log.info("统计日程数量 - meetingId: {}", meetingId);
            
            Integer count = scheduleService.countSchedules(meetingId);
            return ResponseEntity.ok(Result.success(count));
        } catch (Exception e) {
            log.error("统计日程数量异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }
}
