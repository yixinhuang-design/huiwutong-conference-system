package com.conference.common.tenant;

/**
 * 租户上下文持有者
 * 使用 ThreadLocal 存储当前线程的租户信息
 */
public class TenantContextHolder {
    
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    
    /**
     * 设置租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }
    
    /**
     * 获取租户ID
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }
    
    /**
     * 设置用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }
    
    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }
    
    /**
     * 设置用户名
     */
    public static void setUsername(String username) {
        USERNAME.set(username);
    }
    
    /**
     * 获取用户名
     */
    public static String getUsername() {
        return USERNAME.get();
    }
    
    /**
     * 清除上下文
     */
    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
        USERNAME.remove();
    }
}
