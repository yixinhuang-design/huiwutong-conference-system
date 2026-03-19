package com.conference.common.tenant;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;

/**
 * MyBatis 多租户拦截器
 * 自动在 SQL 中添加 tenant_id 条件，实现多租户数据隔离
 */
@Slf4j
public class TenantInterceptor implements InnerInterceptor {
    
    // 不需要租户隔离的表白名单（含无tenant_id列的关联子表）
    private static final String[] WHITELIST_TABLES = {
        "sys_tenant", "sys_user", "sys_role", "sys_permission", "audit_log",
        "chat_group_member", "task_assignee", "task_log"
    };
    
    /**
     * 查询前处理：为 SQL 添加 tenant_id 条件
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, 
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        Long tenantId = TenantContextHolder.getTenantId();
        
        if (tenantId != null) {
            String sql = boundSql.getSql();
            String upperSql = sql.toUpperCase().trim();
            
            // 只处理 SELECT 语句
            if (upperSql.startsWith("SELECT")) {
                // 检查是否需要添加租户条件
                if (!shouldSkip(sql)) {
                    try {
                        String modifiedSql = injectTenantCondition(sql, tenantId);
                        
                        // 使用反射更新 BoundSql 中的 SQL
                        Field sqlField = BoundSql.class.getDeclaredField("sql");
                        sqlField.setAccessible(true);
                        sqlField.set(boundSql, modifiedSql);
                        
                        log.debug("租户隔离 - 租户ID: {}, SQL: {}", tenantId, modifiedSql);
                    } catch (Exception e) {
                        log.error("添加租户隔离条件失败", e);
                    }
                }
            }
        }
    }
    
    /**
     * 检查是否应该跳过租户隔离
     */
    private boolean shouldSkip(String sql) {
        String upperSql = sql.toUpperCase();
        
        // 跳过白名单中的表
        for (String table : WHITELIST_TABLES) {
            if (upperSql.contains(table.toUpperCase())) {
                return true;
            }
        }
        
        // 如果 SQL 中已经包含 tenant_id 条件，则跳过
        if (upperSql.contains("TENANT_ID")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 为 SQL 注入 tenant_id 条件
     */
    private String injectTenantCondition(String sql, Long tenantId) {
        String upperSql = sql.toUpperCase();
        
        if (upperSql.contains("WHERE")) {
            // 在 WHERE 子句后添加 AND 条件
            // 处理 ORDER BY 的情况
            if (upperSql.contains("ORDER BY")) {
                int orderByIndex = upperSql.indexOf("ORDER BY");
                String beforeOrderBy = sql.substring(0, orderByIndex).trim();
                String orderByPart = sql.substring(orderByIndex);
                return beforeOrderBy + " AND tenant_id = " + tenantId + " " + orderByPart;
            } else if (upperSql.contains("LIMIT")) {
                int limitIndex = upperSql.indexOf("LIMIT");
                String beforeLimit = sql.substring(0, limitIndex).trim();
                String limitPart = sql.substring(limitIndex);
                return beforeLimit + " AND tenant_id = " + tenantId + " " + limitPart;
            } else if (upperSql.contains("GROUP BY")) {
                int groupByIndex = upperSql.indexOf("GROUP BY");
                String beforeGroupBy = sql.substring(0, groupByIndex).trim();
                String groupByPart = sql.substring(groupByIndex);
                return beforeGroupBy + " AND tenant_id = " + tenantId + " " + groupByPart;
            } else {
                // 直接追加
                return sql.trim() + " AND tenant_id = " + tenantId;
            }
        } else {
            // 添加 WHERE 子句
            return sql.trim() + " WHERE tenant_id = " + tenantId;
        }
    }
}
