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

    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND deleted = 0 LIMIT 1")
    SysUser selectByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM sys_user WHERE tenant_id = #{tenantId} AND phone = #{phone} AND deleted = 0 LIMIT 1")
    SysUser selectByTenantAndPhone(@Param("tenantId") Long tenantId, @Param("phone") String phone);
}
