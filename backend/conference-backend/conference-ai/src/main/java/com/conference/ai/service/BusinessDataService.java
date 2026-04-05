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

/**
 * 业务数据查询服务 - 跨服务调用获取真实业务数据
 * 
 * 调用其他微服务的REST API获取真实数据，替代AI硬编码回答
 */
@Slf4j
@Service
public class BusinessDataService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public BusinessDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ============ 通用请求头 ============
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Tenant-Id", defaultTenantId);
        return headers;
    }

    /**
     * 安全调用远程服务，返回JSON节点
     */
    private JsonNode safeCall(String url) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode root = objectMapper.readTree(resp.getBody());
                // 统一返回格式 { code: 200, data: ... }
                if (root.has("code") && root.get("code").asInt() == 200 && root.has("data")) {
                    return root.get("data");
                }
                return root;
            }
        } catch (Exception e) {
            log.warn("远程调用失败: {} - {}", url, e.getMessage());
        }
        return null;
    }

    // ====================================================================
    //                        会议服务 (8084)
    // ====================================================================

    /**
     * 获取会议列表
     */
    public String queryMeetingList() {
        JsonNode data = safeCall(meetingService + "/api/meeting/list?pageNum=1&pageSize=10");
        if (data == null) return null;

        try {
            JsonNode records = data.has("records") ? data.get("records") : data;
            if (!records.isArray() || records.isEmpty()) return "当前暂无会议信息。";

            StringBuilder sb = new StringBuilder("📋 **当前会议列表**\n\n");
            int i = 1;
            for (JsonNode m : records) {
                String name = getField(m, "name", "title", "meetingName");
                String status = getMeetingStatus(m);
                String startDate = getField(m, "startDate", "startTime");
                String endDate = getField(m, "endDate", "endTime");

                sb.append(String.format("**%d. %s** %s\n", i++, name, status));
                if (startDate != null) sb.append(String.format("   📅 时间：%s ~ %s\n", startDate, endDate != null ? endDate : "待定"));
                String location = getField(m, "location", "address", "venue");
                if (location != null) sb.append(String.format("   📍 地点：%s\n", location));
                sb.append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("解析会议列表失败", e);
            return null;
        }
    }

    /**
     * 获取进行中的会议
     */
    public String queryOngoingMeetings() {
        JsonNode data = safeCall(meetingService + "/api/meeting/ongoing");
        if (data == null || !data.isArray() || data.isEmpty()) return null;

        StringBuilder sb = new StringBuilder("🔴 **正在进行的会议**\n\n");
        for (JsonNode m : data) {
            String name = getField(m, "name", "title", "meetingName");
            String location = getField(m, "location", "address");
            sb.append(String.format("• **%s**", name));
            if (location != null) sb.append(String.format(" - 📍 %s", location));
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * 获取会议统计
     */
    public String queryMeetingStats() {
        JsonNode data = safeCall(meetingService + "/api/meeting/statistics");
        if (data == null) return null;

        try {
            StringBuilder sb = new StringBuilder("📊 **会议统计**\n\n");
            appendStat(sb, data, "总会议数", "total", "totalCount");
            appendStat(sb, data, "进行中", "ongoing", "ongoingCount");
            appendStat(sb, data, "已结束", "finished", "finishedCount");
            appendStat(sb, data, "待开始", "pending", "pendingCount");
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    // ====================================================================
    //                        日程服务 (8084)
    // ====================================================================

    /**
     * 查询会议的全部日程
     */
    public String queryAllSchedules(Long meetingId) {
        if (meetingId == null) {
            // 先获取最新的会议ID
            meetingId = getLatestMeetingId();
            if (meetingId == null) return null;
        }

        JsonNode data = safeCall(meetingService + "/api/schedule/all?meetingId=" + meetingId);
        if (data == null || !data.isArray() || data.isEmpty()) return "该会议暂未设置日程。";

        StringBuilder sb = new StringBuilder("📅 **会议日程安排**\n\n");
        String lastDate = "";
        int i = 1;
        for (JsonNode s : data) {
            String startTime = getField(s, "startTime");
            String endTime = getField(s, "endTime");
            String title = getField(s, "title");
            String speaker = getField(s, "speaker");
            String location = getField(s, "location");

            // 按日期分组
            String date = startTime != null && startTime.length() >= 10 ? startTime.substring(0, 10) : "";
            if (!date.equals(lastDate)) {
                sb.append(String.format("\n### 📌 %s\n\n", date.isEmpty() ? "未知日期" : date));
                lastDate = date;
            }

            String timeRange = formatTimeRange(startTime, endTime);
            sb.append(String.format("**%s** %s\n", timeRange, title != null ? title : ""));
            if (speaker != null) sb.append(String.format("   👨‍🏫 主讲：%s\n", speaker));
            if (location != null) sb.append(String.format("   📍 地点：%s\n", location));
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * 查询当前正在进行的日程
     */
    public String queryCurrentSchedule(Long meetingId) {
        if (meetingId == null) meetingId = getLatestMeetingId();
        if (meetingId == null) return null;

        JsonNode data = safeCall(meetingService + "/api/schedule/current?meetingId=" + meetingId);
        if (data == null || data.isNull()) return "当前没有正在进行的日程。";

        String title = getField(data, "title");
        String startTime = getField(data, "startTime");
        String endTime = getField(data, "endTime");
        String speaker = getField(data, "speaker");
        String location = getField(data, "location");

        StringBuilder sb = new StringBuilder("🔴 **当前日程**\n\n");
        sb.append(String.format("**%s**\n", title != null ? title : "进行中"));
        sb.append(String.format("⏰ 时间：%s\n", formatTimeRange(startTime, endTime)));
        if (speaker != null) sb.append(String.format("👨‍🏫 主讲：%s\n", speaker));
        if (location != null) sb.append(String.format("📍 地点：%s\n", location));
        return sb.toString().trim();
    }

    /**
     * 查询下一个日程
     */
    public String queryNextSchedule(Long meetingId) {
        if (meetingId == null) meetingId = getLatestMeetingId();
        if (meetingId == null) return null;

        JsonNode data = safeCall(meetingService + "/api/schedule/next?meetingId=" + meetingId);
        if (data == null || data.isNull()) return "暂无后续日程安排。";

        String title = getField(data, "title");
        String startTime = getField(data, "startTime");
        String endTime = getField(data, "endTime");
        String speaker = getField(data, "speaker");
        String location = getField(data, "location");

        StringBuilder sb = new StringBuilder("⏭️ **下一个日程**\n\n");
        sb.append(String.format("**%s**\n", title != null ? title : ""));
        sb.append(String.format("⏰ 时间：%s\n", formatTimeRange(startTime, endTime)));
        if (speaker != null) sb.append(String.format("👨‍🏫 主讲：%s\n", speaker));
        if (location != null) sb.append(String.format("📍 地点：%s\n", location));
        return sb.toString().trim();
    }

    /**
     * 查询即将开始的日程（30分钟内）
     */
    public String queryUpcomingSchedules(Long meetingId) {
        if (meetingId == null) meetingId = getLatestMeetingId();
        if (meetingId == null) return null;

        JsonNode data = safeCall(meetingService + "/api/schedule/upcoming?meetingId=" + meetingId);
        if (data == null || !data.isArray() || data.isEmpty()) return "最近30分钟内没有即将开始的日程。";

        StringBuilder sb = new StringBuilder("⏰ **即将开始的日程**（30分钟内）\n\n");
        for (JsonNode s : data) {
            String title = getField(s, "title");
            String startTime = getField(s, "startTime");
            String location = getField(s, "location");
            sb.append(String.format("• **%s** - %s", title, formatTime(startTime)));
            if (location != null) sb.append(String.format(" @ %s", location));
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * 查询进行中的日程
     */
    public String queryOngoingSchedules(Long meetingId) {
        if (meetingId == null) meetingId = getLatestMeetingId();
        if (meetingId == null) return null;

        JsonNode data = safeCall(meetingService + "/api/schedule/ongoing?meetingId=" + meetingId);
        if (data == null || !data.isArray() || data.isEmpty()) return "当前没有进行中的日程。";

        StringBuilder sb = new StringBuilder("🔴 **进行中的日程**\n\n");
        for (JsonNode s : data) {
            String title = getField(s, "title");
            String timeRange = formatTimeRange(getField(s, "startTime"), getField(s, "endTime"));
            String speaker = getField(s, "speaker");
            sb.append(String.format("• **%s** %s", title, timeRange));
            if (speaker != null) sb.append(String.format(" - 主讲：%s", speaker));
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    // ====================================================================
    //                        报名服务 (8082)
    // ====================================================================

    /**
     * 查询报名统计
     */
    public String queryRegistrationStats(Long conferenceId) {
        String url = registrationService + "/api/registration/stats";
        if (conferenceId != null) url += "?conferenceId=" + conferenceId;
        JsonNode data = safeCall(url);
        if (data == null) return null;

        try {
            StringBuilder sb = new StringBuilder("📊 **报名统计**\n\n");
            appendStat(sb, data, "报名总数", "total", "totalCount", "registrationCount");
            appendStat(sb, data, "已审核", "approved", "approvedCount");
            appendStat(sb, data, "待审核", "pending", "pendingCount");
            appendStat(sb, data, "已拒绝", "rejected", "rejectedCount");
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询分组信息
     */
    public String queryGrouping(Long conferenceId) {
        String url = registrationService + "/api/grouping/list";
        if (conferenceId != null) url += "?conferenceId=" + conferenceId;
        JsonNode data = safeCall(url);
        if (data == null || !data.isArray() || data.isEmpty()) return "暂无分组信息。";

        StringBuilder sb = new StringBuilder("👥 **分组信息**\n\n");
        int i = 1;
        for (JsonNode g : data) {
            String name = getField(g, "groupName", "name");
            JsonNode members = g.has("members") ? g.get("members") : null;
            int count = members != null && members.isArray() ? members.size() : 0;
            String leader = getField(g, "leaderName", "leader");
            sb.append(String.format("**%d. %s** （%d人）", i++, name != null ? name : "未命名组", count));
            if (leader != null) sb.append(String.format(" - 组长：%s", leader));
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    // ====================================================================
    //                        排座服务 (8086)
    // ====================================================================

    /**
     * 查询座位信息
     */
    public String querySeatInfo(Long conferenceId) {
        if (conferenceId == null) conferenceId = getLatestMeetingId();
        if (conferenceId == null) return null;

        // 先获取会场
        JsonNode venues = safeCall(seatingService + "/api/seating/venues/" + conferenceId);
        if (venues == null) return "暂无座位信息。";

        try {
            // venues可能是数组或单个对象
            JsonNode venueList = venues.isArray() ? venues : (venues.has("data") ? venues.get("data") : venues);
            if (venueList.isArray() && venueList.isEmpty()) return "暂未设置会场座位。";

            StringBuilder sb = new StringBuilder("🪑 **座位信息**\n\n");
            JsonNode venueArray = venueList.isArray() ? venueList : objectMapper.createArrayNode();
            if (!venueList.isArray()) {
                // 单个会场对象
                appendVenueInfo(sb, venueList, conferenceId);
            } else {
                for (JsonNode v : venueArray) {
                    appendVenueInfo(sb, v, conferenceId);
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("解析座位信息失败", e);
            return null;
        }
    }

    private void appendVenueInfo(StringBuilder sb, JsonNode venue, Long conferenceId) {
        String name = getField(venue, "name", "venueName");
        sb.append(String.format("**📍 %s**\n", name != null ? name : "会场"));

        // 尝试获取座位统计
        String venueId = getField(venue, "id", "venueId");
        if (venueId != null) {
            JsonNode stats = safeCall(seatingService + "/api/seating/seats/stats/" + venueId);
            if (stats != null) {
                appendStat(sb, stats, "总座位数", "totalSeats", "total");
                appendStat(sb, stats, "已分配", "assignedSeats", "assigned");
                appendStat(sb, stats, "空闲", "availableSeats", "available");
            }
        }
        sb.append("\n");
    }

    /**
     * 查询住宿信息
     */
    public String queryAccommodation(Long conferenceId) {
        if (conferenceId == null) conferenceId = getLatestMeetingId();
        if (conferenceId == null) return null;

        JsonNode data = safeCall(seatingService + "/api/seating/accommodations/" + conferenceId);
        if (data == null || (data.isArray() && data.isEmpty())) return "暂无住宿安排信息。";

        StringBuilder sb = new StringBuilder("🏨 **住宿安排**\n\n");
        JsonNode list = data.isArray() ? data : objectMapper.createArrayNode();
        if (data.isArray()) {
            for (JsonNode a : list) {
                String hotelName = getField(a, "hotelName", "name");
                String address = getField(a, "address", "location");
                String checkIn = getField(a, "checkInTime", "checkIn");
                String checkOut = getField(a, "checkOutTime", "checkOut");
                sb.append(String.format("**🏨 %s**\n", hotelName != null ? hotelName : "住宿"));
                if (address != null) sb.append(String.format("   📍 地址：%s\n", address));
                if (checkIn != null) sb.append(String.format("   🔑 入住：%s\n", checkIn));
                if (checkOut != null) sb.append(String.format("   🚪 退房：%s\n", checkOut));
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * 查询餐饮信息
     */
    public String queryDining(Long conferenceId) {
        if (conferenceId == null) conferenceId = getLatestMeetingId();
        if (conferenceId == null) return null;

        JsonNode data = safeCall(seatingService + "/api/seating/dinings/" + conferenceId);
        if (data == null || (data.isArray() && data.isEmpty())) return "暂无用餐安排信息。";

        StringBuilder sb = new StringBuilder("🍽️ **用餐安排**\n\n");
        if (data.isArray()) {
            for (JsonNode d : data) {
                String mealType = getField(d, "mealType", "type", "name");
                String time = getField(d, "mealTime", "time");
                String location = getField(d, "location", "place");
                sb.append(String.format("• **%s**", mealType != null ? mealType : "用餐"));
                if (time != null) sb.append(String.format(" — %s", time));
                if (location != null) sb.append(String.format(" @ %s", location));
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * 查询交通信息
     */
    public String queryTransport(Long conferenceId) {
        if (conferenceId == null) conferenceId = getLatestMeetingId();
        if (conferenceId == null) return null;

        JsonNode data = safeCall(seatingService + "/api/seating/transports/" + conferenceId);
        if (data == null || (data.isArray() && data.isEmpty())) return "暂无交通安排信息。";

        StringBuilder sb = new StringBuilder("🚗 **交通安排**\n\n");
        if (data.isArray()) {
            for (JsonNode t : data) {
                String vehicleType = getField(t, "vehicleType", "type");
                String plateNumber = getField(t, "plateNumber", "plate");
                String driverName = getField(t, "driverName", "driver");
                String route = getField(t, "route", "routeDescription");
                sb.append(String.format("• **%s**", vehicleType != null ? vehicleType : "车辆"));
                if (plateNumber != null) sb.append(String.format(" %s", plateNumber));
                if (driverName != null) sb.append(String.format(" - 司机：%s", driverName));
                if (route != null) sb.append(String.format("\n   路线：%s", route));
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    // ====================================================================
    //                        协同服务 (8089)
    // ====================================================================

    /**
     * 查询任务列表
     */
    public String queryTasks(Long conferenceId) {
        String url = collaborationService + "/api/task/list?pageNum=1&pageSize=10";
        if (conferenceId != null) url += "&conferenceId=" + conferenceId;
        JsonNode data = safeCall(url);
        if (data == null) return null;

        try {
            JsonNode records = data.has("records") ? data.get("records") : data;
            if (!records.isArray() || records.isEmpty()) return "暂无任务信息。";

            StringBuilder sb = new StringBuilder("📋 **任务列表**\n\n");
            for (JsonNode t : records) {
                String title = getField(t, "title", "taskName", "name");
                String status = getTaskStatus(t);
                String deadline = getField(t, "deadline", "endTime", "dueDate");
                sb.append(String.format("• **%s** %s\n", title != null ? title : "未命名任务", status));
                if (deadline != null) sb.append(String.format("   ⏰ 截止：%s\n", deadline));
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查询任务统计
     */
    public String queryTaskStats(Long conferenceId) {
        String url = collaborationService + "/api/task/stats";
        if (conferenceId != null) url += "?conferenceId=" + conferenceId;
        JsonNode data = safeCall(url);
        if (data == null) return null;

        try {
            StringBuilder sb = new StringBuilder("📊 **任务统计**\n\n");
            appendStat(sb, data, "总任务数", "total", "totalCount");
            appendStat(sb, data, "已完成", "completed", "completedCount");
            appendStat(sb, data, "进行中", "inProgress", "inProgressCount");
            appendStat(sb, data, "待处理", "pending", "pendingCount");
            appendStat(sb, data, "已逾期", "overdue", "overdueCount");
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 搜索资料
     */
    public String queryMaterials(String keyword, Long conferenceId) {
        String url = collaborationService + "/api/material/search?keyword=" + keyword;
        if (conferenceId != null) url += "&meetingId=" + conferenceId;
        JsonNode data = safeCall(url);
        if (data == null) return null;

        try {
            JsonNode records = data.has("records") ? data.get("records") : data;
            if (!records.isArray() || records.isEmpty()) return "未找到相关资料。";

            StringBuilder sb = new StringBuilder("📁 **搜索结果**\n\n");
            for (JsonNode m : records) {
                String name = getField(m, "name", "fileName", "title");
                String type = getField(m, "fileType", "type");
                String size = getField(m, "fileSize", "size");
                sb.append(String.format("• **%s**", name != null ? name : "文件"));
                if (type != null) sb.append(String.format(" (%s)", type));
                if (size != null) sb.append(String.format(" - %s", size));
                sb.append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    // ====================================================================
    //                        辅助方法
    // ====================================================================

    /**
     * 获取最新/最近的会议ID
     */
    public Long getLatestMeetingId() {
        try {
            JsonNode data = safeCall(meetingService + "/api/meeting/list?pageNum=1&pageSize=1");
            if (data == null) return null;
            JsonNode records = data.has("records") ? data.get("records") : data;
            if (records.isArray() && !records.isEmpty()) {
                JsonNode first = records.get(0);
                String id = getField(first, "id");
                return id != null ? Long.parseLong(id) : null;
            }
        } catch (Exception e) {
            log.warn("获取最新会议ID失败", e);
        }
        return null;
    }

    /**
     * 获取会议详情
     */
    public String queryMeetingDetail(Long meetingId) {
        if (meetingId == null) meetingId = getLatestMeetingId();
        if (meetingId == null) return null;

        JsonNode data = safeCall(meetingService + "/api/meeting/" + meetingId);
        if (data == null) return null;

        try {
            StringBuilder sb = new StringBuilder("📋 **会议详情**\n\n");
            String name = getField(data, "name", "title", "meetingName");
            String startDate = getField(data, "startDate", "startTime");
            String endDate = getField(data, "endDate", "endTime");
            String location = getField(data, "location", "address", "venue");
            String desc = getField(data, "description", "desc");
            String status = getMeetingStatus(data);

            sb.append(String.format("**%s** %s\n\n", name != null ? name : "会议", status));
            if (startDate != null) sb.append(String.format("📅 时间：%s ~ %s\n", startDate, endDate != null ? endDate : "待定"));
            if (location != null) sb.append(String.format("📍 地点：%s\n", location));
            if (desc != null) sb.append(String.format("📝 说明：%s\n", desc));
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    // ============ 字段取值 & 格式化工具 ============

    private String getField(JsonNode node, String... fieldNames) {
        for (String field : fieldNames) {
            if (node.has(field) && !node.get(field).isNull()) {
                String val = node.get(field).asText();
                if (!val.isEmpty() && !"null".equals(val)) return val;
            }
        }
        return null;
    }

    private void appendStat(StringBuilder sb, JsonNode data, String label, String... fieldNames) {
        String val = getField(data, fieldNames);
        if (val != null) {
            sb.append(String.format("• %s：**%s**\n", label, val));
        }
    }

    private String getMeetingStatus(JsonNode m) {
        String statusField = getField(m, "status");
        if (statusField == null) return "";
        try {
            int s = Integer.parseInt(statusField);
            switch (s) {
                case 0: return "🟡 筹备中";
                case 1: return "🟢 进行中";
                case 2: return "🔵 已结束";
                case 3: return "🔴 已取消";
                default: return "";
            }
        } catch (NumberFormatException e) {
            return statusField;
        }
    }

    private String getTaskStatus(JsonNode t) {
        String statusField = getField(t, "status");
        if (statusField == null) return "";
        try {
            int s = Integer.parseInt(statusField);
            switch (s) {
                case 0: return "⏳ 待处理";
                case 1: return "🔄 进行中";
                case 2: return "✅ 已完成";
                case 3: return "❌ 已取消";
                default: return "";
            }
        } catch (NumberFormatException e) {
            return statusField;
        }
    }

    private String formatTimeRange(String start, String end) {
        String s = formatTime(start);
        String e = formatTime(end);
        if (s != null && e != null) return s + " ~ " + e;
        if (s != null) return s;
        return "时间待定";
    }

    private String formatTime(String datetime) {
        if (datetime == null) return null;
        // 提取 HH:mm 部分
        try {
            if (datetime.length() >= 16) return datetime.substring(11, 16);
            if (datetime.length() >= 10) return datetime;
        } catch (Exception e) {
            // ignore
        }
        return datetime;
    }
}
