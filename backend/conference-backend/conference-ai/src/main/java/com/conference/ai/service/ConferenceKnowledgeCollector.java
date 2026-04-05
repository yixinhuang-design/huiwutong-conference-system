package com.conference.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * 会议知识库采集器 - 跨模块聚合会议全维度信息
 * 
 * 当用户在某个会议上下文中提问时，自动从各业务微服务采集该会议的全面信息，
 * 组装为结构化的知识上下文，供AI生成高质量回答。
 * 
 * 采集维度：
 * 1. 会议基本信息（会议服务 8084）
 * 2. 日程安排（会议服务 8084）
 * 3. 报名信息与统计（报名服务 8082）
 * 4. 分组信息（报名服务 8082）
 * 5. 工作人员（报名服务 8082）
 * 6. 会场与座位（排座服务 8086）
 * 7. 住宿安排（排座服务 8086）
 * 8. 餐饮安排（排座服务 8086）
 * 9. 交通安排（排座服务 8086）
 * 10. 讨论室（排座服务 8086）
 * 11. 课件资料（会议服务 8084）
 * 12. 会议手册（报名服务 8082）
 * 
 * @author AI Executive
 * @date 2026-04-04
 */
@Slf4j
@Service
public class ConferenceKnowledgeCollector {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(6);

    /** 知识缓存：conferenceId -> (知识文本, 过期时间) */
    private final ConcurrentHashMap<Long, CachedKnowledge> knowledgeCache = new ConcurrentHashMap<>();

    /** 缓存有效期（分钟） */
    private static final int CACHE_TTL_MINUTES = 5;

    @Value("${service.meeting.url:http://localhost:8084}")
    private String meetingService;

    @Value("${service.registration.url:http://localhost:8082}")
    private String registrationService;

    @Value("${service.seating.url:http://localhost:8086}")
    private String seatingService;

    @Value("${service.collaboration.url:http://localhost:8089}")
    private String collaborationService;

    @Value("${service.default-tenant-id:2027317834622709762}")
    private String defaultTenantId;

    public ConferenceKnowledgeCollector(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 采集指定会议的全维度知识（带缓存）
     * 
     * @param conferenceId 会议ID
     * @return 结构化的知识文本，可直接嵌入AI系统提示词
     */
    public String collectKnowledge(Long conferenceId) {
        if (conferenceId == null) {
            return collectGlobalKnowledge();
        }

        // 检查缓存
        CachedKnowledge cached = knowledgeCache.get(conferenceId);
        if (cached != null && !cached.isExpired()) {
            log.debug("使用缓存知识: conferenceId={}", conferenceId);
            return cached.content;
        }

        log.info("开始采集会议知识: conferenceId={}", conferenceId);
        long startTime = System.currentTimeMillis();

        // 并发采集各维度数据
        Map<String, Future<String>> futures = new LinkedHashMap<>();
        futures.put("meeting", executorService.submit(() -> collectMeetingInfo(conferenceId)));
        futures.put("schedules", executorService.submit(() -> collectSchedules(conferenceId)));
        futures.put("registration", executorService.submit(() -> collectRegistrationInfo(conferenceId)));
        futures.put("groups", executorService.submit(() -> collectGroupInfo(conferenceId)));
        futures.put("staff", executorService.submit(() -> collectStaffInfo(conferenceId)));
        futures.put("venues", executorService.submit(() -> collectVenueAndSeatInfo(conferenceId)));
        futures.put("accommodation", executorService.submit(() -> collectAccommodationInfo(conferenceId)));
        futures.put("dining", executorService.submit(() -> collectDiningInfo(conferenceId)));
        futures.put("transport", executorService.submit(() -> collectTransportInfo(conferenceId)));
        futures.put("discussions", executorService.submit(() -> collectDiscussionInfo(conferenceId)));
        futures.put("attachments", executorService.submit(() -> collectAttachmentInfo(conferenceId)));

        // 组装知识文本
        StringBuilder knowledge = new StringBuilder();
        knowledge.append("=== 会议知识库（实时数据） ===\n");
        knowledge.append("数据采集时间：").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        for (Map.Entry<String, Future<String>> entry : futures.entrySet()) {
            try {
                String result = entry.getValue().get(8, TimeUnit.SECONDS);
                if (result != null && !result.isBlank()) {
                    knowledge.append(result).append("\n\n");
                }
            } catch (TimeoutException e) {
                log.warn("采集超时: {}", entry.getKey());
            } catch (Exception e) {
                log.warn("采集异常: {} - {}", entry.getKey(), e.getMessage());
            }
        }

        String knowledgeText = knowledge.toString().trim();

        // 缓存结果
        knowledgeCache.put(conferenceId, new CachedKnowledge(knowledgeText));

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("会议知识采集完成: conferenceId={}, 耗时={}ms, 知识量={}字符", conferenceId, elapsed, knowledgeText.length());

        return knowledgeText;
    }

    /**
     * 全局模式 - 采集概览性知识
     */
    private String collectGlobalKnowledge() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 系统概览知识 ===\n\n");

        // 会议列表
        String meetingList = collectMeetingList();
        if (meetingList != null) sb.append(meetingList).append("\n\n");

        // 会议统计
        String stats = collectMeetingStats();
        if (stats != null) sb.append(stats).append("\n\n");

        return sb.toString().trim();
    }

    // ==================== 各维度采集方法 ====================

    /**
     * 1. 会议基本信息
     */
    private String collectMeetingInfo(Long conferenceId) {
        JsonNode data = safeCall(meetingService + "/api/meeting/" + conferenceId);
        if (data == null) return null;

        StringBuilder sb = new StringBuilder("【会议基本信息】\n");
        appendField(sb, "会议名称", data, "name", "title", "meetingName");
        appendField(sb, "会议状态", data, "status");
        appendField(sb, "开始时间", data, "startDate", "startTime");
        appendField(sb, "结束时间", data, "endDate", "endTime");
        appendField(sb, "会议地点", data, "location", "address", "venue");
        appendField(sb, "会议描述", data, "description", "desc");
        appendField(sb, "主办方", data, "organizer", "host");
        appendField(sb, "联系人", data, "contactPerson", "contact");
        appendField(sb, "联系电话", data, "contactPhone", "phone");
        appendField(sb, "参会人数上限", data, "maxParticipants", "capacity");
        appendField(sb, "会议类型", data, "type", "meetingType");
        appendField(sb, "备注", data, "remark", "notes");

        return sb.toString().trim();
    }

    /**
     * 2. 日程安排
     */
    private String collectSchedules(Long conferenceId) {
        JsonNode data = safeCall(meetingService + "/api/schedule/all?meetingId=" + conferenceId);
        if (data == null || !data.isArray() || data.isEmpty()) return "【日程安排】\n暂未设置日程。";

        StringBuilder sb = new StringBuilder("【日程安排】\n");
        sb.append("共").append(data.size()).append("个日程项：\n");

        String lastDate = "";
        for (JsonNode s : data) {
            String startTime = getField(s, "startTime");
            String endTime = getField(s, "endTime");
            String title = getField(s, "title");
            String speaker = getField(s, "speaker");
            String location = getField(s, "location");
            String description = getField(s, "description", "desc");

            // 按日期分组显示
            String date = startTime != null && startTime.length() >= 10 ? startTime.substring(0, 10) : "";
            if (!date.equals(lastDate)) {
                sb.append("\n[").append(date.isEmpty() ? "日期未定" : date).append("]\n");
                lastDate = date;
            }

            String time = formatTimeRange(startTime, endTime);
            sb.append("- ").append(time).append(" ").append(title != null ? title : "未命名");
            if (speaker != null) sb.append(" | 主讲：").append(speaker);
            if (location != null) sb.append(" | 地点：").append(location);
            if (description != null && description.length() <= 100) sb.append(" | 说明：").append(description);
            sb.append("\n");
        }

        // 标注当前进行中的日程
        JsonNode current = safeCall(meetingService + "/api/schedule/current?meetingId=" + conferenceId);
        if (current != null && !current.isNull()) {
            String currentTitle = getField(current, "title");
            if (currentTitle != null) {
                sb.append("\n** 当前正在进行：").append(currentTitle).append(" **\n");
            }
        }

        // 标注下一个日程
        JsonNode next = safeCall(meetingService + "/api/schedule/next?meetingId=" + conferenceId);
        if (next != null && !next.isNull()) {
            String nextTitle = getField(next, "title");
            String nextStart = getField(next, "startTime");
            if (nextTitle != null) {
                sb.append("** 下一个日程：").append(nextTitle);
                if (nextStart != null) sb.append("（").append(formatTime(nextStart)).append("开始）");
                sb.append(" **\n");
            }
        }

        return sb.toString().trim();
    }

    /**
     * 3. 报名信息与统计
     */
    private String collectRegistrationInfo(Long conferenceId) {
        StringBuilder sb = new StringBuilder("【报名信息】\n");

        // 报名统计
        JsonNode stats = safeCall(registrationService + "/api/registration/stats?conferenceId=" + conferenceId);
        if (stats != null) {
            appendField(sb, "报名总数", stats, "total", "totalCount", "registrationCount");
            appendField(sb, "已审核通过", stats, "approved", "approvedCount");
            appendField(sb, "待审核", stats, "pending", "pendingCount");
            appendField(sb, "已拒绝", stats, "rejected", "rejectedCount");
        }

        // 报名人员列表（前20人概要）
        JsonNode regList = safeCall(registrationService + "/api/registration/list?conferenceId=" + conferenceId + "&pageNum=1&pageSize=20");
        if (regList != null) {
            JsonNode records = regList.has("records") ? regList.get("records") : regList;
            if (records.isArray() && !records.isEmpty()) {
                sb.append("报名人员（部分）：\n");
                for (JsonNode r : records) {
                    String name = getField(r, "name", "realName", "userName");
                    String org = getField(r, "organization", "company", "orgName");
                    String phone = getField(r, "phone", "mobile");
                    String regStatus = getField(r, "status", "auditStatus");
                    sb.append("- ").append(name != null ? name : "未知");
                    if (org != null) sb.append(" | ").append(org);
                    if (phone != null) sb.append(" | ").append(phone);
                    if (regStatus != null) sb.append(" | 状态：").append(formatRegStatus(regStatus));
                    sb.append("\n");
                }
            }
        }

        return sb.toString().trim();
    }

    /**
     * 4. 分组信息
     */
    private String collectGroupInfo(Long conferenceId) {
        JsonNode data = safeCall(registrationService + "/api/grouping/list?conferenceId=" + conferenceId);
        if (data == null || !data.isArray() || data.isEmpty()) return "【分组信息】\n暂无分组。";

        StringBuilder sb = new StringBuilder("【分组信息】\n");
        sb.append("共").append(data.size()).append("个小组：\n");

        for (JsonNode g : data) {
            String groupName = getField(g, "groupName", "name");
            String leader = getField(g, "leaderName", "leader");
            sb.append("- ").append(groupName != null ? groupName : "未命名组");
            if (leader != null) sb.append(" | 组长：").append(leader);

            // 组员列表
            JsonNode members = g.has("members") ? g.get("members") : null;
            if (members != null && members.isArray() && !members.isEmpty()) {
                sb.append(" | 成员(").append(members.size()).append("人)：");
                List<String> memberNames = new ArrayList<>();
                for (JsonNode m : members) {
                    String mName = getField(m, "name", "memberName", "realName");
                    if (mName != null) memberNames.add(mName);
                }
                sb.append(String.join("、", memberNames));
            }
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * 5. 工作人员
     */
    private String collectStaffInfo(Long conferenceId) {
        JsonNode data = safeCall(registrationService + "/api/registration/staff/list?conferenceId=" + conferenceId);
        if (data == null || !data.isArray() || data.isEmpty()) return null;

        StringBuilder sb = new StringBuilder("【工作人员】\n");
        for (JsonNode s : data) {
            String name = getField(s, "name", "staffName", "realName");
            String role = getField(s, "role", "staffRole");
            String phone = getField(s, "phone", "mobile");
            sb.append("- ").append(name != null ? name : "未知");
            if (role != null) sb.append(" | 角色：").append(role);
            if (phone != null) sb.append(" | 电话：").append(phone);
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * 6. 会场与座位信息
     */
    private String collectVenueAndSeatInfo(Long conferenceId) {
        JsonNode venues = safeCall(seatingService + "/api/seating/venues/" + conferenceId);
        if (venues == null) return "【会场座位】\n暂未设置会场。";

        StringBuilder sb = new StringBuilder("【会场与座位信息】\n");

        JsonNode venueList = venues.isArray() ? venues : null;
        if (venueList == null) {
            // 可能是单个对象
            appendVenueDetail(sb, venues, conferenceId);
        } else {
            sb.append("共").append(venueList.size()).append("个会场：\n");
            for (JsonNode v : venueList) {
                appendVenueDetail(sb, v, conferenceId);
            }
        }

        return sb.toString().trim();
    }

    private void appendVenueDetail(StringBuilder sb, JsonNode venue, Long conferenceId) {
        String name = getField(venue, "name", "venueName");
        String capacity = getField(venue, "capacity", "totalSeats");
        String addr = getField(venue, "address", "location");
        String venueId = getField(venue, "id", "venueId");

        sb.append("会场：").append(name != null ? name : "未命名");
        if (capacity != null) sb.append(" | 容量：").append(capacity);
        if (addr != null) sb.append(" | 地址：").append(addr);
        sb.append("\n");

        // 座位统计
        if (venueId != null) {
            JsonNode seatStats = safeCall(seatingService + "/api/seating/seats/stats/" + venueId);
            if (seatStats != null) {
                String total = getField(seatStats, "totalSeats", "total");
                String assigned = getField(seatStats, "assignedSeats", "assigned");
                String available = getField(seatStats, "availableSeats", "available");
                if (total != null) sb.append("  座位总数：").append(total);
                if (assigned != null) sb.append(" | 已分配：").append(assigned);
                if (available != null) sb.append(" | 空闲：").append(available);
                sb.append("\n");
            }
        }
    }

    /**
     * 7. 住宿安排
     */
    private String collectAccommodationInfo(Long conferenceId) {
        JsonNode data = safeCall(seatingService + "/api/seating/accommodations/" + conferenceId);
        if (data == null || (data.isArray() && data.isEmpty())) return "【住宿安排】\n暂无住宿安排。";

        StringBuilder sb = new StringBuilder("【住宿安排】\n");
        if (data.isArray()) {
            for (JsonNode a : data) {
                String hotelName = getField(a, "hotelName", "name");
                String address = getField(a, "address", "location");
                String checkIn = getField(a, "checkInTime", "checkIn");
                String checkOut = getField(a, "checkOutTime", "checkOut");
                String roomType = getField(a, "roomType", "type");
                String contact = getField(a, "contactPhone", "phone");

                sb.append("- ").append(hotelName != null ? hotelName : "住宿");
                if (address != null) sb.append(" | 地址：").append(address);
                if (roomType != null) sb.append(" | 房型：").append(roomType);
                if (checkIn != null) sb.append(" | 入住：").append(checkIn);
                if (checkOut != null) sb.append(" | 退房：").append(checkOut);
                if (contact != null) sb.append(" | 联系：").append(contact);
                sb.append("\n");
            }
        }

        return sb.toString().trim();
    }

    /**
     * 8. 餐饮安排
     */
    private String collectDiningInfo(Long conferenceId) {
        JsonNode data = safeCall(seatingService + "/api/seating/dinings/" + conferenceId);
        if (data == null || (data.isArray() && data.isEmpty())) return "【餐饮安排】\n暂无用餐安排。";

        StringBuilder sb = new StringBuilder("【餐饮安排】\n");
        if (data.isArray()) {
            for (JsonNode d : data) {
                String mealType = getField(d, "mealType", "type", "name");
                String time = getField(d, "mealTime", "time");
                String location = getField(d, "location", "place");
                String menu = getField(d, "menu", "description");

                sb.append("- ").append(mealType != null ? mealType : "用餐");
                if (time != null) sb.append(" | 时间：").append(time);
                if (location != null) sb.append(" | 地点：").append(location);
                if (menu != null) sb.append(" | 菜单/说明：").append(menu);
                sb.append("\n");
            }
        }

        return sb.toString().trim();
    }

    /**
     * 9. 交通安排
     */
    private String collectTransportInfo(Long conferenceId) {
        JsonNode data = safeCall(seatingService + "/api/seating/transports/" + conferenceId);
        if (data == null || (data.isArray() && data.isEmpty())) return "【交通安排】\n暂无交通安排。";

        StringBuilder sb = new StringBuilder("【交通安排】\n");
        if (data.isArray()) {
            for (JsonNode t : data) {
                String vehicleType = getField(t, "vehicleType", "type");
                String plateNumber = getField(t, "plateNumber", "plate");
                String driverName = getField(t, "driverName", "driver");
                String driverPhone = getField(t, "driverPhone", "phone");
                String route = getField(t, "route", "routeDescription");
                String departTime = getField(t, "departureTime", "departTime");

                sb.append("- ").append(vehicleType != null ? vehicleType : "车辆");
                if (plateNumber != null) sb.append(" ").append(plateNumber);
                if (driverName != null) sb.append(" | 司机：").append(driverName);
                if (driverPhone != null) sb.append(" | 电话：").append(driverPhone);
                if (route != null) sb.append(" | 路线：").append(route);
                if (departTime != null) sb.append(" | 发车：").append(departTime);
                sb.append("\n");
            }
        }

        return sb.toString().trim();
    }

    /**
     * 10. 讨论室
     */
    private String collectDiscussionInfo(Long conferenceId) {
        JsonNode data = safeCall(seatingService + "/api/seating/discussions/" + conferenceId);
        if (data == null || (data.isArray() && data.isEmpty())) return null;

        StringBuilder sb = new StringBuilder("【讨论室信息】\n");
        if (data.isArray()) {
            for (JsonNode d : data) {
                String name = getField(d, "name", "roomName");
                String capacity = getField(d, "capacity");
                String location = getField(d, "location");
                String time = getField(d, "time", "useTime");

                sb.append("- ").append(name != null ? name : "讨论室");
                if (capacity != null) sb.append(" | 容量：").append(capacity).append("人");
                if (location != null) sb.append(" | 位置：").append(location);
                if (time != null) sb.append(" | 时间：").append(time);
                sb.append("\n");
            }
        }

        return sb.toString().trim();
    }

    /**
     * 11. 课件附件资料
     */
    private String collectAttachmentInfo(Long conferenceId) {
        // 先获取日程列表，再获取每个日程的附件
        JsonNode schedules = safeCall(meetingService + "/api/schedule/all?meetingId=" + conferenceId);
        if (schedules == null || !schedules.isArray() || schedules.isEmpty()) return null;

        StringBuilder sb = new StringBuilder("【课件资料】\n");
        boolean hasAttachments = false;

        for (JsonNode s : schedules) {
            String scheduleId = getField(s, "id");
            String scheduleTitle = getField(s, "title");
            if (scheduleId == null) continue;

            JsonNode attachments = safeCall(meetingService + "/api/schedule/attachments/" + scheduleId);
            if (attachments != null && attachments.isArray() && !attachments.isEmpty()) {
                hasAttachments = true;
                sb.append("日程「").append(scheduleTitle != null ? scheduleTitle : "未命名").append("」的资料：\n");
                for (JsonNode a : attachments) {
                    String fileName = getField(a, "fileName", "name", "originalName");
                    String fileType = getField(a, "fileType", "type");
                    String fileSize = getField(a, "fileSize", "size");
                    sb.append("  - ").append(fileName != null ? fileName : "文件");
                    if (fileType != null) sb.append(" (").append(fileType).append(")");
                    if (fileSize != null) sb.append(" ").append(fileSize);
                    sb.append("\n");
                }
            }
        }

        if (!hasAttachments) return null;
        return sb.toString().trim();
    }

    /**
     * 会议列表概览
     */
    private String collectMeetingList() {
        JsonNode data = safeCall(meetingService + "/api/meeting/list?pageNum=1&pageSize=10");
        if (data == null) return null;

        JsonNode records = data.has("records") ? data.get("records") : data;
        if (!records.isArray() || records.isEmpty()) return "当前暂无会议。";

        StringBuilder sb = new StringBuilder("【会议列表】\n");
        for (JsonNode m : records) {
            String name = getField(m, "name", "title", "meetingName");
            String id = getField(m, "id");
            String status = getField(m, "status");
            String startDate = getField(m, "startDate", "startTime");
            sb.append("- ").append(name != null ? name : "会议");
            if (id != null) sb.append(" (ID:").append(id).append(")");
            if (status != null) sb.append(" | 状态：").append(formatMeetingStatus(status));
            if (startDate != null) sb.append(" | 时间：").append(startDate);
            sb.append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * 会议统计概览
     */
    private String collectMeetingStats() {
        JsonNode data = safeCall(meetingService + "/api/meeting/statistics");
        if (data == null) return null;

        StringBuilder sb = new StringBuilder("【会议统计】\n");
        appendField(sb, "总会议数", data, "total", "totalCount");
        appendField(sb, "进行中", data, "ongoing", "ongoingCount");
        appendField(sb, "已结束", data, "finished", "finishedCount");
        appendField(sb, "待开始", data, "pending", "pendingCount");

        return sb.toString().trim();
    }

    /**
     * 采集特定用户在会议中的个人信息（座位、分组等）
     */
    public String collectUserPersonalInfo(Long conferenceId, Long userId, String userName) {
        if (conferenceId == null || (userId == null && userName == null)) return null;

        StringBuilder sb = new StringBuilder("【用户个人相关信息】\n");
        sb.append("用户：").append(userName != null ? userName : "ID:" + userId).append("\n");

        // 查询用户报名状态
        if (userName != null) {
            // 通过报名列表中查找该用户
            JsonNode regList = safeCall(registrationService + "/api/registration/list?conferenceId=" + conferenceId + "&pageNum=1&pageSize=100");
            if (regList != null) {
                JsonNode records = regList.has("records") ? regList.get("records") : regList;
                if (records.isArray()) {
                    for (JsonNode r : records) {
                        String name = getField(r, "name", "realName", "userName");
                        if (userName.equals(name)) {
                            appendField(sb, "报名状态", r, "status", "auditStatus");
                            appendField(sb, "所属单位", r, "organization", "company");
                            appendField(sb, "职务", r, "position", "title");
                            break;
                        }
                    }
                }
            }
        }

        // 查询用户座位（从参会人员列表中查找）
        JsonNode attendees = safeCall(seatingService + "/api/seating/attendees/" + conferenceId);
        if (attendees != null && attendees.isArray()) {
            for (JsonNode a : attendees) {
                String aName = getField(a, "name", "attendeeName", "realName");
                if (userName != null && userName.equals(aName)) {
                    String seatNo = getField(a, "seatNumber", "seatNo", "seat");
                    String row = getField(a, "rowNumber", "row");
                    String col = getField(a, "colNumber", "col");
                    String venueName = getField(a, "venueName");
                    if (seatNo != null) sb.append("座位号：").append(seatNo);
                    if (row != null) sb.append(" | 排：").append(row);
                    if (col != null) sb.append(" | 列：").append(col);
                    if (venueName != null) sb.append(" | 会场：").append(venueName);
                    sb.append("\n");
                    break;
                }
            }
        }

        return sb.length() > 30 ? sb.toString().trim() : null;
    }

    /**
     * 清除指定会议的缓存
     */
    public void invalidateCache(Long conferenceId) {
        if (conferenceId != null) {
            knowledgeCache.remove(conferenceId);
        }
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        knowledgeCache.clear();
    }

    // ==================== 工具方法 ====================

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Tenant-Id", defaultTenantId);
        return headers;
    }

    private JsonNode safeCall(String url) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode root = objectMapper.readTree(resp.getBody());
                if (root.has("code") && root.get("code").asInt() == 200 && root.has("data")) {
                    return root.get("data");
                }
                return root;
            }
        } catch (Exception e) {
            log.debug("远程调用: {} - {}", url, e.getMessage());
        }
        return null;
    }

    private String getField(JsonNode node, String... fieldNames) {
        for (String field : fieldNames) {
            if (node.has(field) && !node.get(field).isNull()) {
                String val = node.get(field).asText();
                if (!val.isEmpty() && !"null".equals(val)) return val;
            }
        }
        return null;
    }

    private void appendField(StringBuilder sb, String label, JsonNode node, String... fieldNames) {
        String val = getField(node, fieldNames);
        if (val != null) {
            sb.append(label).append("：").append(val).append("\n");
        }
    }

    private String formatTimeRange(String start, String end) {
        String s = formatTime(start);
        String e = formatTime(end);
        if (s != null && e != null) return s + "~" + e;
        if (s != null) return s;
        return "时间待定";
    }

    private String formatTime(String datetime) {
        if (datetime == null) return null;
        try {
            if (datetime.length() >= 16) return datetime.substring(11, 16);
            if (datetime.length() >= 10) return datetime;
        } catch (Exception e) { /* ignore */ }
        return datetime;
    }

    private String formatMeetingStatus(String status) {
        if (status == null) return "未知";
        try {
            switch (Integer.parseInt(status)) {
                case 0: return "筹备中";
                case 1: return "进行中";
                case 2: return "已结束";
                case 3: return "已取消";
                default: return status;
            }
        } catch (NumberFormatException e) {
            return status;
        }
    }

    private String formatRegStatus(String status) {
        if (status == null) return "未知";
        try {
            switch (Integer.parseInt(status)) {
                case 0: return "待审核";
                case 1: return "已通过";
                case 2: return "已拒绝";
                default: return status;
            }
        } catch (NumberFormatException e) {
            return status;
        }
    }

    // ==================== 缓存内部类 ====================

    private static class CachedKnowledge {
        final String content;
        final LocalDateTime expireTime;

        CachedKnowledge(String content) {
            this.content = content;
            this.expireTime = LocalDateTime.now().plusMinutes(CACHE_TTL_MINUTES);
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expireTime);
        }
    }
}
