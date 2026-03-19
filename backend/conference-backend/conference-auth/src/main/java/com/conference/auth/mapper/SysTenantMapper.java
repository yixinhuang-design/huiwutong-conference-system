package com.conference.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.auth.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {

    @Select("SELECT * FROM sys_tenant WHERE tenant_code = #{tenantCode} AND deleted = 0 LIMIT 1")
    SysTenant selectByTenantCode(@Param("tenantCode") String tenantCode);
}
