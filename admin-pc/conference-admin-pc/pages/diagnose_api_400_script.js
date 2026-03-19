// 复制以下代码到浏览器 F12 console 中执行
// 这个脚本会自动诊断API 400错误的原因

(function diagnoseAPI400Error() {
    console.clear();
    console.log('🔍 开始诊断 API 400 错误...\n');
    
    // ==========================================
    // 1. 检查token存在性
    // ==========================================
    console.log('📋 步骤1: Token检查');
    console.log('─'.repeat(50));
    
    const authToken = localStorage.getItem('authToken');
    const token = localStorage.getItem('token');
    
    if (!authToken && !token) {
        console.error('❌ 错误: localStorage 中没有找到 token!');
        console.log('   请先登录获取token');
        return;
    }
    
    const activeToken = authToken || token;
    console.log('✓ Token 存在');
    console.log('  来自: ' + (authToken ? 'authToken' : 'token'));
    console.log('  预览: ' + activeToken.substring(0, 30) + '...\n');
    
    // ==========================================
    // 2. 检查token格式
    // ==========================================
    console.log('📋 步骤2: Token格式检查');
    console.log('─'.repeat(50));
    
    const parts = activeToken.split('.');
    if (parts.length !== 3) {
        console.error('❌ 错误: Token 不是有效的 JWT 格式!');
        console.log('   JWT 应该是: header.payload.signature');
        console.log('   当前格式: ' + parts.length + ' 部分\n');
        return;
    }
    
    console.log('✓ Token 格式正确 (JWT标准格式)\n');
    
    // ==========================================
    // 3. 解析token payload
    // ==========================================
    console.log('📋 步骤3: Token内容解析');
    console.log('─'.repeat(50));
    
    let payload;
    try {
        payload = JSON.parse(atob(parts[1]));
        console.log('✓ Token 已成功解析\n');
    } catch (e) {
        console.error('❌ 错误: 无法解析 Token');
        console.log('   ' + e.message + '\n');
        return;
    }
    
    // ==========================================
    // 4. 检查关键字段
    // ==========================================
    console.log('📋 步骤4: 关键字段检查');
    console.log('─'.repeat(50));
    
    const checks = {
        'userId': payload.userId,
        'tenantId': payload.tenantId,
        'username': payload.username,
        'exp': payload.exp
    };
    
    let allValid = true;
    Object.keys(checks).forEach(key => {
        if (checks[key]) {
            console.log('✓ ' + key + ': ' + JSON.stringify(checks[key]));
        } else {
            console.error('❌ ' + key + ': 缺失或为空!');
            allValid = false;
        }
    });
    
    console.log();
    
    // ==========================================
    // 5. 检查token过期时间
    // ==========================================
    console.log('📋 步骤5: Token过期检查');
    console.log('─'.repeat(50));
    
    const now = Math.floor(Date.now() / 1000);
    const expTime = payload.exp;
    const expDate = new Date(expTime * 1000);
    const remainingSeconds = expTime - now;
    
    console.log('  发行时间: ' + new Date(payload.iat * 1000).toLocaleString());
    console.log('  过期时间: ' + expDate.toLocaleString());
    console.log('  当前时间: ' + new Date().toLocaleString());
    
    if (remainingSeconds > 0) {
        console.log('✓ Token 未过期 (剩余' + Math.floor(remainingSeconds / 60) + '分钟)\n');
    } else {
        console.error('❌ Token 已过期 (过期' + Math.floor(Math.abs(remainingSeconds) / 60) + '分钟)\n');
    }
    
    // ==========================================
    // 6. 检查Authorization header
    // ==========================================
    console.log('📋 步骤6: Authorization header模拟');
    console.log('─'.repeat(50));
    
    const authHeader = 'Bearer ' + activeToken.substring(0, 20) + '...';
    console.log('✓ 将使用header: ' + authHeader + '\n');
    
    // ==========================================
    // 7. 完整的payload显示
    // ==========================================
    console.log('📋 步骤7: 完整Token内容');
    console.log('─'.repeat(50));
    console.log('Header:', JSON.parse(atob(parts[0])));
    console.log('Payload:', payload);
    console.log();
    
    // ==========================================
    // 8. 诊断结果
    // ==========================================
    console.log('🎯 诊断结果');
    console.log('═'.repeat(50));
    
    const issues = [];
    
    if (!payload.userId) {
        issues.push('❌ userId 缺失 - 这是 400 错误的最可能原因！');
    }
    if (!payload.tenantId) {
        issues.push('❌ tenantId 缺失');
    }
    if (remainingSeconds <= 0) {
        issues.push('❌ Token 已过期，需要刷新');
    }
    
    if (issues.length === 0) {
        console.log('✅ 所有检查都通过！');
        console.log('\n如果仍然收到400错误，可能的原因：');
        console.log('1. 后端TenantFilter没有正确处理token');
        console.log('2. 后端权限检查有问题');
        console.log('3. 网络请求没有正确添加Authorization header');
        console.log('\n建议: 检查浏览器Network标签，查看API请求的完整headers');
    } else {
        console.log(issues.join('\n'));
        console.log('\n建议: ' + (issues[0].includes('userId') ? 
            '重新登录获取新的token，确保包含userId字段' : 
            '清除localStorage中的token，重新登录'));
    }
    
    console.log('\n' + '═'.repeat(50));
    console.log('诊断完成！');
})();
