const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration'
});

async function diagnose() {
  const conn = await pool.getConnection();
  
  try {
    console.log('='.repeat(80));
    console.log('【诊断开始】报名数据诊断脚本');
    console.log('='.repeat(80));

    // 步骤1：查看所有报名记录
    console.log('\n【Step 1】最近10条报名记录');
    console.log('-'.repeat(80));
    const [reg1] = await conn.query(
      `SELECT id, tenant_id, meeting_id, name, phone, organization, status, create_time, deleted 
       FROM conf_registration 
       ORDER BY create_time DESC 
       LIMIT 10`
    );
    console.table(reg1);

    // 步骤2：查看所有不同的meeting_id
    console.log('\n【Step 2】所有不同的meeting_id及其报名数');
    console.log('-'.repeat(80));
    const [reg2] = await conn.query(
      `SELECT meeting_id, COUNT(*) as record_count, GROUP_CONCAT(DISTINCT tenant_id) as tenant_ids
       FROM conf_registration
       WHERE deleted = 0
       GROUP BY meeting_id
       ORDER BY record_count DESC`
    );
    console.table(reg2);

    // 步骤3：查询特定会议的报名数据
    console.log('\n【Step 3】会议ID=2030309010523144194的报名数据');
    console.log('-'.repeat(80));
    const [reg3] = await conn.query(
      `SELECT id, tenant_id, name, phone, organization, position, status, create_time
       FROM conf_registration
       WHERE meeting_id = 2030309010523144194 AND deleted = 0
       ORDER BY create_time DESC`
    );
    console.log(`总数: ${reg3.length} 条`);
    if (reg3.length > 0) {
      console.table(reg3);
    } else {
      console.log('❌ 未找到数据');
    }

    // 步骤4：查看租户ID分布
    console.log('\n【Step 4】租户ID分布');
    console.log('-'.repeat(80));
    const [reg4] = await conn.query(
      `SELECT tenant_id, COUNT(*) as record_count, GROUP_CONCAT(DISTINCT meeting_id) as meeting_ids
       FROM conf_registration
       WHERE deleted = 0
       GROUP BY tenant_id`
    );
    console.table(reg4);

    // 步骤5：查看被删除的记录
    console.log('\n【Step 5】被删除的报名记录统计');
    console.log('-'.repeat(80));
    const [reg5] = await conn.query(
      `SELECT COUNT(*) as deleted_count
       FROM conf_registration
       WHERE deleted = 1`
    );
    console.table(reg5);

    // 步骤6：检查当前用户（从会议表中推断）
    console.log('\n【Step 6】会议主表conf_meeting的信息');
    console.log('-'.repeat(80));
    const [reg6] = await conn.query(
      `SELECT id, meeting_name, meeting_code, tenant_id
       FROM conf_meeting
       LIMIT 5`
    );
    console.table(reg6);

    // 步骤7：检查插入脚本中的数据（验证插入逻辑）
    console.log('\n【Step 7】特定手机号数据查询（验证插入脚本的20条数据）');
    console.log('-'.repeat(80));
    const [reg7] = await conn.query(
      `SELECT id, meeting_id, tenant_id, name, phone, organization, position, status, create_time
       FROM conf_registration
       WHERE phone IN ('13800138001', '13800138002', '13800138003', '13800138004', '13800138005')
       ORDER BY phone`
    );
    console.log(`找到 ${reg7.length} 条特定数据`);
    console.table(reg7);

    console.log('\n' + '='.repeat(80));
    console.log('【诊断完成】');
    console.log('='.repeat(80));
    console.log('\n【问题排查指南】');
    console.log('1. 如果Step 3中没有数据：');
    console.log('   - 检查meeting_id是否正确（应该是2030309010523144194）');
    console.log('   - 检查租户ID是否匹配（应该是2027317834622709762）');
    console.log('2. 如果Step 7中没有特定手机号的数据：');
    console.log('   - 说明insert_registrations_with_node.js脚本没有成功执行');
    console.log('3. 如果Step 2中的meeting_id不是2030309010523144194：');
    console.log('   - 数据可能被插入到了其他会议下');
    
  } catch (error) {
    console.error('❌ 诊断出错:', error.message);
  } finally {
    conn.release();
    await pool.end();
  }
}

diagnose();
