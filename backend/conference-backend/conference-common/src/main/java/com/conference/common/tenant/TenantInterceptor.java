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
    
    // 不需要租户隔离的表白名单（含无tenant_id列的关联子表和监控表）
    private static final String[] WHITELIST_TABLES = {
        "sys_tenant", "sys_user", "sys_role", "sys_permission", "audit_log",
        "chat_group_member", "task_assignee", "task_log",
        "data_service_health", "data_system_metrics"
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
     * 修复：GROUP BY 优先于 ORDER BY 检查，避免条件被注入到 GROUP BY 子句中
     * 修复：JOIN 查询中使用表别名限定 tenant_id，避免多表歧义
     */
    private String injectTenantCondition(String sql, Long tenantId) {
        String upperSql = sql.toUpperCase();

        // 对 JOIN 查询，使用表别名限定 tenant_id 避免歧义
        String tenantColumn = resolveTenantColumn(sql);

        if (upperSql.contains("WHERE")) {
            // 按优先级在 WHERE 子句中插入条件: GROUP BY > ORDER BY > LIMIT > 直接追加
            if (upperSql.contains("GROUP BY")) {
                int groupByIndex = upperSql.indexOf("GROUP BY");
                String beforeGroupBy = sql.substring(0, groupByIndex).trim();
                String groupByPart = sql.substring(groupByIndex);
                return beforeGroupBy + " AND " + tenantColumn + " = " + tenantId + " " + groupByPart;
            } else if (upperSql.contains("ORDER BY")) {
                int orderByIndex = upperSql.indexOf("ORDER BY");
                String beforeOrderBy = sql.substring(0, orderByIndex).trim();
                String orderByPart = sql.substring(orderByIndex);
                return beforeOrderBy + " AND " + tenantColumn + " = " + tenantId + " " + orderByPart;
            } else if (upperSql.contains("LIMIT")) {
                int limitIndex = upperSql.indexOf("LIMIT");
                String beforeLimit = sql.substring(0, limitIndex).trim();
                String limitPart = sql.substring(limitIndex);
                return beforeLimit + " AND " + tenantColumn + " = " + tenantId + " " + limitPart;
            } else {
                return sql.trim() + " AND " + tenantColumn + " = " + tenantId;
            }
        } else {
            // 无 WHERE 子句时，需要将 WHERE 插入到 GROUP BY / ORDER BY / LIMIT 之前
            if (upperSql.contains("GROUP BY")) {
                int groupByIndex = upperSql.indexOf("GROUP BY");
                String beforeGroupBy = sql.substring(0, groupByIndex).trim();
                String groupByPart = sql.substring(groupByIndex);
                return beforeGroupBy + " WHERE " + tenantColumn + " = " + tenantId + " " + groupByPart;
            } else if (upperSql.contains("ORDER BY")) {
                int orderByIndex = upperSql.indexOf("ORDER BY");
                String beforeOrderBy = sql.substring(0, orderByIndex).trim();
                String orderByPart = sql.substring(orderByIndex);
                return beforeOrderBy + " WHERE " + tenantColumn + " = " + tenantId + " " + orderByPart;
            } else if (upperSql.contains("LIMIT")) {
                int limitIndex = upperSql.indexOf("LIMIT");
                String beforeLimit = sql.substring(0, limitIndex).trim();
                String limitPart = sql.substring(limitIndex);
                return beforeLimit + " WHERE " + tenantColumn + " = " + tenantId + " " + limitPart;
            } else {
                return sql.trim() + " WHERE " + tenantColumn + " = " + tenantId;
            }
        }
    }

    /**
     * 解析 tenant_id 列引用
     * 对于含 JOIN 的主查询，使用第一个表的别名限定 tenant_id，避免多表歧义
     */
    private String resolveTenantColumn(String sql) {
        // 去除子查询内容，只分析主查询结构
        String mainSql = stripSubqueries(sql);
        String upperMain = mainSql.toUpperCase();

        // 主查询无 JOIN 则不需要别名限定
        if (!upperMain.contains("JOIN")) {
            return "tenant_id";
        }

        // 从主查询的 FROM 子句提取第一个表别名
        // 匹配: FROM [database.]table_name alias
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("FROM\\s+\\S+\\s+(\\w+)\\s+", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(mainSql);
        if (m.find()) {
            String alias = m.group(1);
            if (!isSqlKeyword(alias.toUpperCase())) {
                return alias + ".tenant_id";
            }
        }

        return "tenant_id";
    }

    /**
     * 去除 SQL 中括号内的子查询内容，保留主查询结构
     */
    private String stripSubqueries(String sql) {
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '(') { depth++; continue; }
            if (c == ')') { depth--; continue; }
            if (depth == 0) sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 判断是否为 SQL 关键字（避免误将关键字当作表别名）
     */
    private boolean isSqlKeyword(String word) {
        switch (word) {
            case "WHERE": case "INNER": case "LEFT": case "RIGHT": case "FULL":
            case "CROSS": case "JOIN": case "ON": case "GROUP": case "ORDER":
            case "LIMIT": case "HAVING": case "SET": case "VALUES": case "INTO":
            case "AND": case "OR": case "NOT": case "AS": case "UNION":
            case "SELECT": case "FROM": case "BY": case "DISTINCT": case "ALL":
            case "BETWEEN": case "LIKE": case "IN": case "EXISTS": case "CASE":
            case "WHEN": case "THEN": case "ELSE": case "END": case "IS": case "NULL":
            case "DELETE": case "UPDATE": case "INSERT": case "NATURAL": case "OUTER":
                return true;
            default:
                return false;
        }
    }
}
