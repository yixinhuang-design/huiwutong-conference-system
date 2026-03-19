package com.conference.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.conference.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE tenant_id = #{tenantId} AND username = #{username} AND deleted = 0 LIMIT 1")
    SysUser selectByTenantAndUsername(@Param("tenantId") Long tenantId, @Param("username") String username);
}
