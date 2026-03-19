const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration'
});

async function summarize() {
  const conn = await pool.getConnection();
  
  try {
    console.log('\n' + '='.repeat(100));
    console.log('报名数据诊断汇总');
    console.log('='.repeat(100));

    // 1. 检查会议ID是否正确
    console.log('\n【问题1】会议ID验证');
    console.log('-'.repeat(100));
    const [meetings] = await conn.query(
      `SELECT id, meeting_name, tenant_id FROM conf_meeting WHERE id IN (2030309010523144194, 2030309010523144200)`
    );
    console.log('查询的会议ID:');
    console.log('  - 插入脚本中的: 2030309010523144194');
    console.log('  - 数据库实际的: 2030309010523144200');
    console.log('\n会议表记录:');
    meetings.forEach(m => {
      console.log(`  ID: ${m.id}`);
      console.log(`  名称: ${m.meeting_name}`);
      console.log(`  租户ID: ${m.tenant_id}`);
    });

    // 2. 检查租户ID
    console.log('\n【问题2】租户ID验证');
    console.log('-'.repeat(100));
    console.log('插入脚本中设置的租户ID: 2027317834622709762');
    const [tenants] = await conn.query(
      `SELECT DISTINCT tenant_id FROM conf_registration WHERE deleted = 0`
    );
    console.log('数据库中实际存在的租户ID:');
    tenants.forEach(t => console.log('  - ' + t.tenant_id));

    // 3. 报名数据统计
    console.log('\n【问题3】报名数据统计');
    console.log('-'.repeat(100));
    const [stats] = await conn.query(
      `SELECT 
        COUNT(*) as total_count,
        SUM(CASE WHEN meeting_id = 2030309010523144194 THEN 1 ELSE 0 END) as count_with_id_194,
        SUM(CASE WHEN meeting_id = 2030309010523144200 THEN 1 ELSE 0 END) as count_with_id_200
       FROM conf_registration 
       WHERE deleted = 0`
    );
    console.log(`总报名记录数: ${stats[0].total_count}`);
    console.log(`  - meeting_id = 2030309010523144194: ${stats[0].count_with_id_194 || 0} 条`);
    console.log(`  - meeting_id = 2030309010523144200: ${stats[0].count_with_id_200 || 0} 条`);

    // 4. 根本原因分析
    console.log('\n【根本原因分析】');
    console.log('-'.repeat(100));
    
    const insertedCount = stats[0].count_with_id_200;
    const expectedCount = 20;
    
    if (insertedCount === expectedCount) {
      console.log('✅ 插入脚本已成功执行！');
      console.log(`   - 成功插入了${insertedCount}条报名数据`);
      console.log('\n❌ 但是看不到数据的原因：');
      console.log('   插入脚本中使用的会议ID有误！');
      console.log(`   - 脚本代码中写的: const MEETING_ID = 2030309010523144194`);
      console.log(`   - 实际插入的数据: meeting_id = 2030309010523144200`);
      console.log(`   - 这是两个不同的会议！`);
    } else if (insertedCount > 0) {
      console.log(`⚠️  部分数据已插入(${insertedCount}条)，可能有多次执行`);
    } else {
      console.log('❌ 插入脚本从未成功执行');
      console.log('   需要运行: node insert_registrations_with_node.js');
    }

    // 5. 解决方案
    console.log('\n【解决方案】');
    console.log('-'.repeat(100));
    console.log('方案A: 修改脚本，使用正确的会议ID');
    console.log('  1. 编辑 insert_registrations_with_node.js');
    console.log('  2. 找到: const MEETING_ID = 2030309010523144194;');
    console.log('  3. 改为: const MEETING_ID = 2030309010523144200;');
    console.log('  4. 再次运行脚本');
    
    console.log('\n方案B: 用SQL直接查看已插入的数据');
    console.log(`  运行: SELECT * FROM conf_registration WHERE meeting_id = 2030309010523144200 LIMIT 20;`);

    // 6. 获取实际数据URL
    console.log('\n【已插入的报名数据（前5条）】');
    console.log('-'.repeat(100));
    const [data] = await conn.query(
      `SELECT id, name, phone, organization, position, create_time 
       FROM conf_registration 
       WHERE meeting_id = 2030309010523144200 
       AND deleted = 0 
       ORDER BY create_time DESC 
       LIMIT 5`
    );
    if (data.length > 0) {
      data.forEach((row, idx) => {
        console.log(`${idx + 1}. ${row.name} (${row.phone}) - ${row.organization} - ${row.position}`);
      });
    }

    console.log('\n' + '='.repeat(100) + '\n');

  } catch (error) {
    console.error('诊断出错:', error.message);
  } finally {
    conn.release();
    await pool.end();
  }
}

summarize();
