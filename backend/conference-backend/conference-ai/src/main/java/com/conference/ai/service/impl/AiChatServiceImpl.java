package com.conference.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.conference.ai.entity.*;
import com.conference.ai.mapper.*;
import com.conference.ai.service.AiChatService;
import com.conference.ai.service.AiFaqService;
import com.conference.ai.service.AiKnowledgeService;
import com.conference.ai.service.BusinessDataService;
import com.conference.common.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI聊天服务实现 - 智能问答引擎
 * 实现流程: 用户输入 → 意图分析 → FAQ匹配 → 知识库检索 → 智能回答生成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation>
        implements AiChatService {

    private static final Long DEFAULT_TENANT_ID = 2027317834622709762L;

    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final AiContextMapper contextMapper;
    private final AiUsageStatsMapper usageStatsMapper;
    private final AiFaqService faqService;
    private final AiKnowledgeService knowledgeService;
    private final BusinessDataService businessDataService;

    private Long getTenantId() {
        try {
            Long tenantId = TenantContextHolder.getTenantId();
            return tenantId != null ? tenantId : DEFAULT_TENANT_ID;
        } catch (Exception e) {
            return DEFAULT_TENANT_ID;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> chat(Long conversationId, String message, Long userId, String userName, Long conferenceId) {
        long startTime = System.currentTimeMillis();
        Long tenantId = getTenantId();

        // 1. 自动创建或获取对话
        AiConversation conversation;
        if (conversationId == null) {
            conversation = createConversation(userId, userName, conferenceId,
                    message.length() > 20 ? message.substring(0, 20) + "..." : message);
            conversationId = conversation.getId();
        } else {
            conversation = conversationMapper.selectById(conversationId);
            if (conversation == null) {
                conversation = createConversation(userId, userName, conferenceId,
                        message.length() > 20 ? message.substring(0, 20) + "..." : message);
                conversationId = conversation.getId();
            }
        }

        // 2. 保存用户消息
        AiMessage userMsg = new AiMessage();
        userMsg.setTenantId(tenantId);
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setStatus("sent");
        userMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMsg);

        // 3. 智能回答生成
        String aiAnswer = generateIntelligentAnswer(message, conferenceId, conversationId);
        long responseTime = System.currentTimeMillis() - startTime;

        // 4. 保存AI回复消息
        AiMessage aiMsg = new AiMessage();
        aiMsg.setTenantId(tenantId);
        aiMsg.setConversationId(conversationId);
        aiMsg.setRole("ai");
        aiMsg.setContent(aiAnswer);
        aiMsg.setModel("conference-ai-v1");
        aiMsg.setResponseTime((int) responseTime);
        aiMsg.setStatus("sent");
        aiMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(aiMsg);

        // 5. 更新对话信息
        conversation.setLastMessage(message);
        conversation.setMessageCount(conversation.getMessageCount() + 2);
        conversation.setUpdateTime(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        // 6. 更新上下文记忆
        updateContext(userId, userName, conferenceId, message);

        // 7. 更新使用统计
        updateUsageStats(tenantId, (int) responseTime);

        // 8. 构建返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("conversationId", conversationId.toString());
        result.put("userMessage", toMessageMap(userMsg));
        result.put("aiMessage", toMessageMap(aiMsg));
        result.put("responseTime", responseTime);

        log.info("[租户{}] AI对话完成, 用户:{}, 耗时:{}ms", tenantId, userName, responseTime);
        return result;
    }

    /**
     * 智能回答生成引擎
     * 优先级: FAQ精确匹配 → 知识库检索 → 智能分析生成
     */
    private String generateIntelligentAnswer(String question, Long conferenceId, Long conversationId) {
        // Step 1: 分析用户意图
        String intent = analyzeIntent(question);
        log.debug("意图分析结果: {} -> {}", question, intent);

        // Step 2: 尝试FAQ精确匹配
        try {
            AiFaq matchedFaq = faqService.matchFaq(question, conferenceId);
            if (matchedFaq != null) {
                log.debug("FAQ匹配成功: {}", matchedFaq.getQuestion());
                return matchedFaq.getAnswer();
            }
        } catch (Exception e) {
            log.warn("FAQ匹配异常", e);
        }

        // Step 3: 从知识库检索相关内容
        StringBuilder knowledgeContext = new StringBuilder();
        try {
            List<AiKnowledge> relevantKnowledge = knowledgeService.searchKnowledge(question, conferenceId);
            if (!relevantKnowledge.isEmpty()) {
                for (AiKnowledge k : relevantKnowledge) {
                    knowledgeContext.append("【").append(k.getTitle()).append("】");
                    if (StringUtils.hasText(k.getContent())) {
                        knowledgeContext.append(k.getContent());
                    } else if (StringUtils.hasText(k.getSummary())) {
                        knowledgeContext.append(k.getSummary());
                    }
                    knowledgeContext.append("\n");
                }
            }
        } catch (Exception e) {
            log.warn("知识库检索异常", e);
        }

        // Step 4: 获取对话上下文 (最近5条消息)
        List<AiMessage> recentMessages = getRecentMessages(conversationId, 5);

        // Step 5: 生成智能回答（传入conferenceId以支持实时业务查询）
        return buildAnswer(question, intent, knowledgeContext.toString(), recentMessages, conferenceId);
    }

    /**
     * 意图分析 - 基于关键词匹配
     */
    private String analyzeIntent(String question) {
        String q = question.toLowerCase();

        // 日程相关 - 扩展口语化表达
        if (containsAny(q, "日程", "安排", "议程", "时间表", "schedule", "agenda",
                "几点", "什么时候", "接下来", "下一个", "正在进行", "当前", "今天",
                "上午", "下午", "明天", "开始", "结束", "课程", "培训")) return "schedule_query";
        // 座位相关
        if (containsAny(q, "座位", "位置", "坐哪", "座位号", "seat", "排座", "几排", "几号")) return "seat_query";
        // 签到相关
        if (containsAny(q, "签到", "报到", "check-in", "checkin", "打卡")) return "checkin_query";
        // 地点交通
        if (containsAny(q, "地点", "地址", "怎么去", "交通", "路线", "导航", "location",
                "在哪", "会场", "班车", "停车")) return "location_query";
        // 住宿
        if (containsAny(q, "住宿", "酒店", "宾馆", "hotel", "房间", "入住", "退房")) return "hotel_query";
        // 讲师
        if (containsAny(q, "讲师", "嘉宾", "主讲", "老师", "speaker", "专家", "谁来讲")) return "speaker_query";
        // 报名
        if (containsAny(q, "报名", "注册", "registration", "多少人", "参会", "名单")) return "registration_query";
        // 资料
        if (containsAny(q, "资料", "下载", "文件", "文档", "material", "课件", "PPT", "ppt")) return "material_query";
        // 联系
        if (containsAny(q, "联系", "电话", "客服", "会务", "contact", "咨询")) return "contact_query";
        // 餐饮
        if (containsAny(q, "餐饮", "用餐", "午餐", "晚餐", "meal", "吃饭", "早餐", "餐厅", "吃什么")) return "meal_query";
        // 考试
        if (containsAny(q, "考试", "测验", "结业", "exam", "测评")) return "exam_query";
        // 会议概况 - 整体信息
        if (containsAny(q, "会议", "活动", "培训班", "概况", "详情", "介绍")) return "location_query";
        // 任务相关
        if (containsAny(q, "任务", "工作", "待办", "task")) return "general_query";
        // 分组相关
        if (containsAny(q, "分组", "小组", "组员", "组长")) return "general_query";

        if (containsAny(q, "翻译", "translate")) return "translate";
        if (containsAny(q, "总结", "摘要", "summary")) return "summary";

        return "general_query";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    /**
     * 根据意图和上下文构建智能回答
     * 优先级：FAQ → 知识库 → 真实业务数据查询 → 通用兜底回答
     */
    private String buildAnswer(String question, String intent, String knowledgeContext, List<AiMessage> history, Long conferenceId) {
        // 如果知识库有匹配内容，基于知识生成回答
        if (StringUtils.hasText(knowledgeContext)) {
            return buildKnowledgeBasedAnswer(question, intent, knowledgeContext);
        }

        // 优先从真实业务系统查询数据
        String liveAnswer = queryLiveBusinessData(question, intent, conferenceId);
        if (liveAnswer != null) {
            return liveAnswer;
        }

        // 兜底：通用引导回答
        return buildFallbackAnswer(question, intent);
    }

    private String buildKnowledgeBasedAnswer(String question, String intent, String knowledgeContext) {
        StringBuilder answer = new StringBuilder();

        switch (intent) {
            case "schedule_query":
                answer.append("📅 **会议日程安排**\n\n");
                answer.append("根据当前会议信息，以下是相关日程：\n\n");
                break;
            case "seat_query":
                answer.append("🪑 **座位信息**\n\n");
                break;
            case "location_query":
                answer.append("📍 **地点与交通指南**\n\n");
                break;
            case "speaker_query":
                answer.append("👨‍🏫 **讲师信息**\n\n");
                break;
            default:
                answer.append("📋 **查询结果**\n\n");
                break;
        }

        answer.append(knowledgeContext.trim());
        answer.append("\n\n---\n*如有其他问题，请继续提问。*");
        return answer.toString();
    }

    /**
     * 查询真实业务数据 - 根据意图调用对应的微服务API
     */
    private String queryLiveBusinessData(String question, String intent, Long conferenceId) {
        try {
            switch (intent) {
                case "schedule_query": {
                    // 判断是问当前、下一个、还是全部日程
                    String q = question.toLowerCase();
                    if (containsAny(q, "现在", "当前", "正在")) {
                        String result = businessDataService.queryCurrentSchedule(conferenceId);
                        if (result != null) return result;
                        result = businessDataService.queryOngoingSchedules(conferenceId);
                        if (result != null) return result;
                    }
                    if (containsAny(q, "下一个", "接下来", "马上", "即将")) {
                        String result = businessDataService.queryNextSchedule(conferenceId);
                        if (result != null) return result;
                        result = businessDataService.queryUpcomingSchedules(conferenceId);
                        if (result != null) return result;
                    }
                    // 默认查全部日程
                    String result = businessDataService.queryAllSchedules(conferenceId);
                    if (result != null) return result;
                    break;
                }

                case "seat_query": {
                    String result = businessDataService.querySeatInfo(conferenceId);
                    if (result != null) return result;
                    break;
                }

                case "registration_query": {
                    String result = businessDataService.queryRegistrationStats(conferenceId);
                    if (result != null) return result;
                    break;
                }

                case "location_query": {
                    // 查询会议详情中的地点信息
                    String result = businessDataService.queryMeetingDetail(conferenceId);
                    if (result != null) return result;
                    break;
                }

                case "hotel_query": {
                    String result = businessDataService.queryAccommodation(conferenceId);
                    if (result != null) return result;
                    break;
                }

                case "meal_query": {
                    String result = businessDataService.queryDining(conferenceId);
                    if (result != null) return result;
                    break;
                }

                case "speaker_query": {
                    // 从日程中获取讲师信息
                    String result = businessDataService.queryAllSchedules(conferenceId);
                    if (result != null) return result;
                    break;
                }

                case "material_query": {
                    // 从问题中提取关键词搜索资料
                    String keyword = extractKeyword(question, "资料", "下载", "文件", "文档");
                    String result = businessDataService.queryMaterials(keyword, conferenceId);
                    if (result != null) return result;
                    break;
                }

                case "checkin_query": {
                    // 查询需要签到的日程
                    Long meetingId = conferenceId != null ? conferenceId : businessDataService.getLatestMeetingId();
                    if (meetingId != null) {
                        String result = businessDataService.queryAllSchedules(meetingId);
                        if (result != null) {
                            return "✅ **签到信息**\n\n以下日程需要签到，请按时参加：\n\n" + result;
                        }
                    }
                    break;
                }

                case "exam_query":
                case "contact_query": {
                    // 这些从会议详情获取
                    String result = businessDataService.queryMeetingDetail(conferenceId);
                    if (result != null) return result;
                    break;
                }

                case "general_query": {
                    // 通用查询：尝试多维度获取
                    String q = question.toLowerCase();
                    if (containsAny(q, "会议", "培训", "活动")) {
                        String result = businessDataService.queryMeetingList();
                        if (result != null) return result;
                    }
                    if (containsAny(q, "任务", "工作", "安排")) {
                        String result = businessDataService.queryTasks(conferenceId);
                        if (result != null) return result;
                    }
                    if (containsAny(q, "统计", "数据", "概览", "总览")) {
                        String result = businessDataService.queryMeetingStats();
                        if (result != null) return result;
                    }
                    if (containsAny(q, "分组", "小组")) {
                        String result = businessDataService.queryGrouping(conferenceId);
                        if (result != null) return result;
                    }
                    if (containsAny(q, "交通", "车辆", "班车", "接送")) {
                        String result = businessDataService.queryTransport(conferenceId);
                        if (result != null) return result;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("查询业务数据异常: intent={}, error={}", intent, e.getMessage());
        }
        return null;
    }

    /**
     * 从问题中提取搜索关键词
     */
    private String extractKeyword(String question, String... excludeWords) {
        String q = question;
        for (String word : excludeWords) {
            q = q.replace(word, "");
        }
        q = q.replaceAll("[？?！!。，,\\s]+", " ").trim();
        // 取最长的连续词
        String[] parts = q.split("\\s+");
        String longest = question; // fallback
        for (String p : parts) {
            if (p.length() > 1 && p.length() > longest.length()) longest = p;
        }
        return longest.length() > 20 ? longest.substring(0, 20) : longest;
    }

    /**
     * 兜底回答 - 当业务服务不可用或无数据时返回引导性回答
     */
    private String buildFallbackAnswer(String question, String intent) {
        switch (intent) {
            case "schedule_query":
                return "📅 **日程查询**\n\n" +
                        "暂时无法获取实时日程数据（日程服务可能未启动）。\n\n" +
                        "您可以：\n" +
                        "1. 在管理后台的 **\"日程管理\"** 页面查看完整日程\n" +
                        "2. 确认会议服务(8084)是否已启动\n" +
                        "3. 稍后再试，我会自动查询最新日程";

            case "seat_query":
                return "🪑 **座位查询**\n\n" +
                        "暂时无法获取实时座位数据（排座服务可能未启动）。\n\n" +
                        "您可以在 **\"智能排座\"** 页面查看座位图。";

            case "registration_query":
                return "📝 **报名查询**\n\n" +
                        "暂时无法获取实时报名数据（报名服务可能未启动）。\n\n" +
                        "请在 **\"报名管理\"** 页面查看报名状态。";

            case "checkin_query":
                return "✅ **签到方式**\n\n" +
                        "本次会议支持以下签到方式：\n" +
                        "1. **扫码签到** — 到达会场后扫描签到二维码\n" +
                        "2. **人脸识别** — 在入口处进行人脸识别签到\n" +
                        "3. **手动签到** — 在APP端\"报到签到\"页面手动签到\n\n" +
                        "请在会议开始前30分钟完成签到。";

            case "location_query":
                return "📍 **地点查询**\n\n" +
                        "暂时无法获取实时会议地点数据。\n\n" +
                        "请查看会议详情页面获取会场地址和交通指南。";

            case "hotel_query":
                return "🏨 **住宿查询**\n\n" +
                        "暂时无法获取实时住宿数据（排座服务可能未启动）。\n\n" +
                        "请查看会议通知中的住宿安排。";

            case "speaker_query":
                return "👨‍🏫 **讲师查询**\n\n" +
                        "暂时无法获取实时讲师数据。\n\n" +
                        "请在日程管理页面查看各环节的讲师介绍。";

            case "material_query":
                return "📁 **资料查询**\n\n" +
                        "暂时无法获取实时资料数据（协同服务可能未启动）。\n\n" +
                        "请在 **\"资料管理\"** 页面查看和下载。";

            case "meal_query":
                return "🍽️ **餐饮查询**\n\n" +
                        "暂时无法获取实时用餐数据。\n\n" +
                        "请查看会议日程中的用餐时间安排。";

            case "contact_query":
                return "📞 **联系方式**\n\n" +
                        "如需帮助，请通过以下方式联系：\n" +
                        "- 查看会议详情页面的联系信息\n" +
                        "- 现场服务台咨询\n" +
                        "- 通过APP内\"沟通\"功能联系工作人员";

            case "exam_query":
                return "📝 **考试信息**\n\n" +
                        "结业考试相关事项请关注会议通知。考试安排会提前通知。";

            default:
                return "您好！感谢您的提问。\n\n" +
                        "关于您的问题：**" + question + "**\n\n" +
                        "我是AI智能助教，可以为您实时查询以下信息：\n" +
                        "- 📅 会议日程安排（实时查询日程服务）\n" +
                        "- 🪑 座位信息（实时查询排座服务）\n" +
                        "- 📝 报名统计（实时查询报名服务）\n" +
                        "- 🏨 住宿/餐饮/交通安排\n" +
                        "- 📋 任务进度\n" +
                        "- 👥 分组信息\n" +
                        "- 📁 资料搜索\n\n" +
                        "请尝试提问，例如：\"今天的日程安排是什么？\" \"我的座位在哪？\" \"报名了多少人？\"";
        }
    }

    private List<AiMessage> getRecentMessages(Long conversationId, int limit) {
        if (conversationId == null) return Collections.emptyList();
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getConversationId, conversationId)
                .orderByDesc(AiMessage::getCreateTime)
                .last("LIMIT " + limit);
        List<AiMessage> messages = messageMapper.selectList(wrapper);
        Collections.reverse(messages);
        return messages;
    }

    @Override
    @Transactional
    public AiConversation createConversation(Long userId, String userName, Long conferenceId, String title) {
        Long tenantId = getTenantId();
        AiConversation conversation = new AiConversation();
        conversation.setTenantId(tenantId);
        conversation.setUserId(userId);
        conversation.setUserName(userName);
        conversation.setConferenceId(conferenceId);
        conversation.setTitle(StringUtils.hasText(title) ? title : "新对话");
        conversation.setStatus("active");
        conversation.setMessageCount(0);
        conversation.setDeleted(0);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        conversationMapper.insert(conversation);
        log.info("[租户{}] 创建新对话: id={}", tenantId, conversation.getId());
        return conversation;
    }

    @Override
    public List<AiConversation> listConversations(Long userId, Long conferenceId) {
        Long tenantId = getTenantId();
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getTenantId, tenantId)
                .eq(AiConversation::getDeleted, 0);
        if (userId != null) wrapper.eq(AiConversation::getUserId, userId);
        if (conferenceId != null) wrapper.eq(AiConversation::getConferenceId, conferenceId);
        wrapper.orderByDesc(AiConversation::getUpdateTime);
        return conversationMapper.selectList(wrapper);
    }

    @Override
    public List<AiMessage> getConversationMessages(Long conversationId) {
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getConversationId, conversationId)
                .orderByAsc(AiMessage::getCreateTime);
        return messageMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId) {
        // 删除消息
        messageMapper.delete(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getConversationId, conversationId));
        // 删除对话
        conversationMapper.deleteById(conversationId);
        log.info("对话{}已删除", conversationId);
    }

    @Override
    @Transactional
    public void clearConversation(Long conversationId) {
        messageMapper.delete(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getConversationId, conversationId));
        AiConversation conv = conversationMapper.selectById(conversationId);
        if (conv != null) {
            conv.setMessageCount(0);
            conv.setLastMessage(null);
            conv.setUpdateTime(LocalDateTime.now());
            conversationMapper.updateById(conv);
        }
        log.info("对话{}消息已清空", conversationId);
    }

    @Override
    public void rateMessage(Long messageId, String rating) {
        AiMessage msg = messageMapper.selectById(messageId);
        if (msg != null) {
            msg.setRating(rating);
            messageMapper.updateById(msg);
            log.info("消息{}已评价: {}", messageId, rating);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> regenerateResponse(Long messageId) {
        AiMessage originalMsg = messageMapper.selectById(messageId);
        if (originalMsg == null || !"ai".equals(originalMsg.getRole())) {
            return Map.of("error", "无效消息");
        }

        // 找到该AI消息之前的用户消息
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getConversationId, originalMsg.getConversationId())
                .eq(AiMessage::getRole, "user")
                .lt(AiMessage::getCreateTime, originalMsg.getCreateTime())
                .orderByDesc(AiMessage::getCreateTime)
                .last("LIMIT 1");
        AiMessage userMsg = messageMapper.selectOne(wrapper);

        if (userMsg == null) {
            return Map.of("error", "找不到原始提问");
        }

        // 标记原消息为regenerated
        originalMsg.setStatus("regenerated");
        messageMapper.updateById(originalMsg);

        // 重新生成回答
        AiConversation conv = conversationMapper.selectById(originalMsg.getConversationId());
        Long conferenceId = conv != null ? conv.getConferenceId() : null;

        long startTime = System.currentTimeMillis();
        String newAnswer = generateIntelligentAnswer(userMsg.getContent(), conferenceId, originalMsg.getConversationId());
        long responseTime = System.currentTimeMillis() - startTime;

        AiMessage newMsg = new AiMessage();
        newMsg.setTenantId(originalMsg.getTenantId());
        newMsg.setConversationId(originalMsg.getConversationId());
        newMsg.setRole("ai");
        newMsg.setContent(newAnswer);
        newMsg.setModel("conference-ai-v1");
        newMsg.setResponseTime((int) responseTime);
        newMsg.setStatus("sent");
        newMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(newMsg);

        return Map.of("newMessage", toMessageMap(newMsg));
    }

    @Override
    public Map<String, Object> getAiStats(Long conferenceId) {
        Long tenantId = getTenantId();

        // 总查询数 = ai_message中role=user的数量
        long totalQueries = messageMapper.selectCount(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getTenantId, tenantId)
                .eq(AiMessage::getRole, "user"));

        // 今日查询数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayQueries = messageMapper.selectCount(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getTenantId, tenantId)
                .eq(AiMessage::getRole, "user")
                .ge(AiMessage::getCreateTime, todayStart));

        // 平均响应时间
        List<AiMessage> aiMessages = messageMapper.selectList(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getTenantId, tenantId)
                .eq(AiMessage::getRole, "ai")
                .isNotNull(AiMessage::getResponseTime)
                .orderByDesc(AiMessage::getCreateTime)
                .last("LIMIT 100"));
        int avgResponse = aiMessages.isEmpty() ? 0 :
                (int) aiMessages.stream().mapToInt(AiMessage::getResponseTime).average().orElse(0);

        // 满意度
        long goodCount = messageMapper.selectCount(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getTenantId, tenantId)
                .eq(AiMessage::getRating, "good"));
        long badCount = messageMapper.selectCount(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getTenantId, tenantId)
                .eq(AiMessage::getRating, "bad"));
        long totalRated = goodCount + badCount;
        int satisfaction = totalRated > 0 ? (int) (goodCount * 100 / totalRated) : 95;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalQueries", totalQueries);
        stats.put("todayQueries", todayQueries);
        stats.put("avgResponse", avgResponse);
        stats.put("satisfaction", satisfaction);
        return stats;
    }

    private void updateContext(Long userId, String userName, Long conferenceId, String message) {
        if (userId == null) return;
        Long tenantId = getTenantId();
        try {
            AiContext ctx = contextMapper.selectOne(new LambdaQueryWrapper<AiContext>()
                    .eq(AiContext::getTenantId, tenantId)
                    .eq(AiContext::getUserId, userId));
            if (ctx == null) {
                ctx = new AiContext();
                ctx.setTenantId(tenantId);
                ctx.setUserId(userId);
                ctx.setUserName(userName);
                ctx.setConferenceId(conferenceId);
                ctx.setTurns(1);
                ctx.setLastMessage(message);
                ctx.setDuration("1分钟");
                ctx.setCreateTime(LocalDateTime.now());
                ctx.setLastUpdate(LocalDateTime.now());
                contextMapper.insert(ctx);
            } else {
                ctx.setTurns(ctx.getTurns() + 1);
                ctx.setLastMessage(message);
                ctx.setLastUpdate(LocalDateTime.now());
                contextMapper.updateById(ctx);
            }
        } catch (Exception e) {
            log.warn("更新上下文失败", e);
        }
    }

    private void updateUsageStats(Long tenantId, int responseTime) {
        try {
            LocalDate today = LocalDate.now();
            AiUsageStats stats = usageStatsMapper.selectOne(new LambdaQueryWrapper<AiUsageStats>()
                    .eq(AiUsageStats::getTenantId, tenantId)
                    .eq(AiUsageStats::getStatDate, today));
            if (stats == null) {
                stats = new AiUsageStats();
                stats.setTenantId(tenantId);
                stats.setStatDate(today);
                stats.setTotalQueries(1);
                stats.setTotalTokens(0);
                stats.setAvgResponseMs(responseTime);
                stats.setPositiveCount(0);
                stats.setNegativeCount(0);
                stats.setNeutralCount(0);
                stats.setCreateTime(LocalDateTime.now());
                usageStatsMapper.insert(stats);
            } else {
                int newTotal = stats.getTotalQueries() + 1;
                int newAvg = (stats.getAvgResponseMs() * stats.getTotalQueries() + responseTime) / newTotal;
                stats.setTotalQueries(newTotal);
                stats.setAvgResponseMs(newAvg);
                usageStatsMapper.updateById(stats);
            }
        } catch (Exception e) {
            log.warn("更新使用统计失败", e);
        }
    }

    private Map<String, Object> toMessageMap(AiMessage msg) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", msg.getId().toString());
        map.put("role", msg.getRole());
        map.put("content", msg.getContent());
        map.put("model", msg.getModel());
        map.put("responseTime", msg.getResponseTime());
        map.put("rating", msg.getRating());
        map.put("timestamp", msg.getCreateTime() != null ?
                msg.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        return map;
    }
}
