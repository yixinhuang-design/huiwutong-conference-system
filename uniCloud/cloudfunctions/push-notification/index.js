/**
 * uni-push 2.0 推送云函数（URL化版本）
 * 
 * 参考官方文档：https://ask.dcloud.net.cn/article/40283
 * URL化文档：https://doc.dcloud.net.cn/uniCloud/http.html
 * 
 * 使用方式：
 *   1. 在 HBuilderX 中上传部署此云函数到 uniCloud 服务空间
 *   2. 在 uniCloud web 控制台设置云函数 URL化 path 为 /push-notification
 *   3. Java 后端通过 HTTP POST 调用 URL化 地址
 * 
 * POST 请求体示例（JSON）：
 * {
 *   "request_id": "唯一ID_10到32位",
 *   "cids": "单个cid字符串或数组",
 *   "title": "通知标题",
 *   "content": "通知内容",
 *   "force_notification": true,
 *   "payload": { "type": "meeting", "id": "123" },
 *   "settings": { "ttl": 86400000 },
 *   "options": {},
 *   "category": {}
 * }
 * 
 * 注意：如果不传 cids，则为全推（每分钟不超过5次，10分钟内不能推重复消息体）
 * 
 * @author Conference System
 * @since 2026-04-05
 */
'use strict';

// ★★★ 替换为你的 uni-app 应用的 AppId（__UNI__ 开头）★★★
const uniPush = uniCloud.getPushManager({
    appId: "__UNI__28271C6"
});

exports.main = async (event, context) => {
    // ========== 1. 解析请求体 ==========
    // URL化时，POST body 在 event.body 中（可能是 JSON 字符串或 Base64）
    let obj;
    try {
        if (event.body) {
            let bodyStr = event.body;
            // URL化的请求体可能是 base64 编码的
            if (event.isBase64Encoded) {
                bodyStr = Buffer.from(bodyStr, 'base64').toString('utf8');
            }
            obj = typeof bodyStr === 'string' ? JSON.parse(bodyStr) : bodyStr;
        } else {
            // HBuilderX 本地调试时，参数直接在 event 中
            obj = event;
        }
    } catch (e) {
        return { code: 400, msg: '请求参数解析失败: ' + e.message };
    }

    // ========== 2. 生成 request_id（防重复，10-32位）==========
    let reqId = obj.request_id || ('conf' + Date.now() + Math.random().toString(36).substr(2, 6));
    if (reqId.length < 10) reqId = reqId.padEnd(10, '0');
    if (reqId.length > 32) reqId = reqId.substring(0, 32);

    // ========== 3. 构建推送参数（严格按官方 API）==========
    const pushMessage = {
        "push_clientid": obj.cids,                          // 设备id，支持数组如["cid-1","cid-2"]，不传则全推
        "title": obj.title || "新通知",                      // 通知标题
        "content": obj.content || "您有一条新的通知消息",      // 通知内容
        "force_notification": obj.force_notification !== false, // 默认true，在线时自动创建通知栏消息
        "request_id": reqId,                                 // 请求唯一标识
        "payload": obj.payload || {},                        // 点击通知后传给客户端的自定义参数
        "settings": obj.settings || { "ttl": 86400000 },    // 消息有效期，默认24小时
        "options": obj.options || {},                        // 厂商扩展参数（消息分类等）
        "category": obj.category || {}                       // 鸿蒙消息分类
    };

    // 如果不传 cids，删除 push_clientid 字段以触发全推
    if (!obj.cids || (Array.isArray(obj.cids) && obj.cids.length === 0)) {
        delete pushMessage.push_clientid;
    }

    // ========== 4. 发送推送 ==========
    try {
        console.log('[push-notification] 发送推送:', JSON.stringify({
            has_cids: !!obj.cids,
            title: pushMessage.title,
            request_id: reqId
        }));

        const res = await uniPush.sendMessage(pushMessage);

        console.log('[push-notification] 推送结果:', JSON.stringify(res));
        return res;
    } catch (e) {
        console.error('[push-notification] 推送失败:', e);
        return { code: 500, msg: '推送失败: ' + e.message };
    }
};
