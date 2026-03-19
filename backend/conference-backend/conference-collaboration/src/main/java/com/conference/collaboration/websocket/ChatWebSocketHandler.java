package com.conference.collaboration.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.conference.collaboration.entity.ChatMessage;
import com.conference.collaboration.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket聊天处理器
 * 支持群聊消息的实时收发、在线状态管理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageService messageService;

    /** 用户ID -> WebSocket Session */
    private static final Map<String, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    /** 群组ID -> 用户ID集合 */
    private static final Map<String, Set<String>> GROUP_USERS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        if (userId != null) {
            USER_SESSIONS.put(userId, session);
            log.info("WebSocket连接建立: userId={}, total={}", userId, USER_SESSIONS.size());

            // 发送连接成功确认
            JSONObject resp = new JSONObject();
            resp.put("type", "connected");
            resp.put("userId", userId);
            resp.put("onlineCount", USER_SESSIONS.size());
            session.sendMessage(new TextMessage(resp.toJSONString()));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject msg = JSON.parseObject(payload);
        String type = msg.getString("type");

        switch (type) {
            case "join_group":
                handleJoinGroup(session, msg);
                break;
            case "leave_group":
                handleLeaveGroup(session, msg);
                break;
            case "chat_message":
                handleChatMessage(session, msg);
                break;
            case "typing":
                handleTyping(session, msg);
                break;
            case "heartbeat":
                handleHeartbeat(session);
                break;
            default:
                log.warn("未知消息类型: {}", type);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserId(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            // 从所有群组中移除
            GROUP_USERS.values().forEach(users -> users.remove(userId));
            log.info("WebSocket连接关闭: userId={}, remaining={}", userId, USER_SESSIONS.size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = getUserId(session);
        log.error("WebSocket传输错误: userId={}", userId, exception);
        if (session.isOpen()) {
            session.close();
        }
    }

    /** 加入群组 */
    private void handleJoinGroup(WebSocketSession session, JSONObject msg) throws IOException {
        String userId = getUserId(session);
        String groupId = msg.getString("groupId");
        if (groupId == null || userId == null) return;

        GROUP_USERS.computeIfAbsent(groupId, k -> new CopyOnWriteArraySet<>()).add(userId);

        // 通知群内其他成员
        JSONObject notification = new JSONObject();
        notification.put("type", "user_joined");
        notification.put("groupId", groupId);
        notification.put("userId", userId);
        notification.put("userName", msg.getString("userName"));
        notification.put("onlineMembers", GROUP_USERS.get(groupId).size());
        broadcastToGroup(groupId, notification.toJSONString(), userId);

        log.info("用户{}加入群组{}", userId, groupId);
    }

    /** 离开群组 */
    private void handleLeaveGroup(WebSocketSession session, JSONObject msg) throws IOException {
        String userId = getUserId(session);
        String groupId = msg.getString("groupId");
        if (groupId == null || userId == null) return;

        Set<String> users = GROUP_USERS.get(groupId);
        if (users != null) {
            users.remove(userId);
        }

        JSONObject notification = new JSONObject();
        notification.put("type", "user_left");
        notification.put("groupId", groupId);
        notification.put("userId", userId);
        broadcastToGroup(groupId, notification.toJSONString(), userId);

        log.info("用户{}离开群组{}", userId, groupId);
    }

    /** 处理聊天消息 */
    private void handleChatMessage(WebSocketSession session, JSONObject msg) throws IOException {
        String groupId = msg.getString("groupId");
        String senderId = msg.getString("senderId");
        String senderName = msg.getString("senderName");
        String content = msg.getString("content");
        String msgType = msg.getString("msgType");
        String extra = msg.getString("extra");

        if (groupId == null || content == null) return;

        // 持久化消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setGroupId(Long.valueOf(groupId));
        chatMessage.setSenderId(senderId != null ? Long.valueOf(senderId) : null);
        chatMessage.setSenderName(senderName);
        chatMessage.setMsgType(msgType != null ? msgType : "text");
        chatMessage.setContent(content);
        chatMessage.setExtra(extra);

        // 私聊消息
        String receiverId = msg.getString("receiverId");
        if (receiverId != null) {
            chatMessage.setReceiverId(Long.valueOf(receiverId));
        }

        ChatMessage saved = messageService.sendMessage(chatMessage);

        // 构建广播消息
        JSONObject broadcastMsg = new JSONObject();
        broadcastMsg.put("type", "new_message");
        broadcastMsg.put("messageId", saved.getId() != null ? saved.getId().toString() : null);
        broadcastMsg.put("groupId", groupId);
        broadcastMsg.put("senderId", senderId);
        broadcastMsg.put("senderName", senderName);
        broadcastMsg.put("msgType", saved.getMsgType());
        broadcastMsg.put("content", content);
        broadcastMsg.put("extra", extra);
        broadcastMsg.put("createTime", saved.getCreateTime() != null ? saved.getCreateTime().toString() : null);

        if (receiverId != null) {
            // 私聊只发给目标用户和自己
            sendToUser(receiverId, broadcastMsg.toJSONString());
            sendToUser(senderId, broadcastMsg.toJSONString());
        } else {
            // 群聊广播给所有群成员
            broadcastToGroup(groupId, broadcastMsg.toJSONString(), null);
        }
    }

    /** 处理正在输入状态 */
    private void handleTyping(WebSocketSession session, JSONObject msg) throws IOException {
        String groupId = msg.getString("groupId");
        String userId = getUserId(session);
        String userName = msg.getString("userName");

        JSONObject typingMsg = new JSONObject();
        typingMsg.put("type", "typing");
        typingMsg.put("groupId", groupId);
        typingMsg.put("userId", userId);
        typingMsg.put("userName", userName);
        broadcastToGroup(groupId, typingMsg.toJSONString(), userId);
    }

    /** 心跳处理 */
    private void handleHeartbeat(WebSocketSession session) throws IOException {
        JSONObject pong = new JSONObject();
        pong.put("type", "heartbeat_ack");
        pong.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(pong.toJSONString()));
    }

    /** 广播消息给群组内所有在线用户 */
    private void broadcastToGroup(String groupId, String message, String excludeUserId) {
        Set<String> users = GROUP_USERS.get(groupId);
        if (users == null || users.isEmpty()) return;

        TextMessage textMessage = new TextMessage(message);
        for (String userId : users) {
            if (userId.equals(excludeUserId)) continue;
            WebSocketSession userSession = USER_SESSIONS.get(userId);
            if (userSession != null && userSession.isOpen()) {
                try {
                    userSession.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("发送消息给用户{}失败", userId, e);
                }
            }
        }
    }

    /** 发送消息给指定用户 */
    private void sendToUser(String userId, String message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息给用户{}失败", userId, e);
            }
        }
    }

    /** 从WebSocket Session中提取用户ID */
    private String getUserId(WebSocketSession session) {
        // 从URL参数中获取 ?userId=xxx
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2 && "userId".equals(kv[0])) {
                    return kv[1];
                }
            }
        }
        return session.getId();
    }

    /** 获取在线用户数 */
    public int getOnlineCount() {
        return USER_SESSIONS.size();
    }

    /** 获取群组在线用户列表 */
    public Set<String> getGroupOnlineUsers(String groupId) {
        return GROUP_USERS.getOrDefault(groupId, Collections.emptySet());
    }
}
