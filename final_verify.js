const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration'
});

async function verify() {
  const conn = await pool.getConnection();
  
  try {
    console.log('\n【最终核实】\n');
    
    // 正确的值
    const CORRECT_MEETING_ID = 2030309010523144194;
    const CORRECT_TENANT_ID = 2027317834622709762;
    
    console.log(`正确的会议ID: ${CORRECT_MEETING_ID}`);
    console.log(`正确的租户ID: ${CORRECT_TENANT_ID}`);

    // 查看会议信息
    const [meeting] = await conn.query(
      `SELECT id, meeting_name, tenant_id FROM conf_meeting WHERE id = ?`,
      [CORRECT_MEETING_ID]
    );
    console.log(`\n会议${CORRECT_MEETING_ID}的信息:`);
    if (meeting.length > 0) {
      console.log(`  名称: ${meeting[0].meeting_name}`);
      console.log(`  租户ID: ${meeting[0].tenant_id}`);
    }

    // 查看报名数据当前状态
    const [current] = await conn.query(
      `SELECT COUNT(*) as count FROM conf_registration WHERE deleted = 0`
    );
    console.log(`\n报名数据当前总数: ${current[0].count}`);

    const [byMeeting] = await conn.query(
      `SELECT meeting_id, COUNT(*) as count FROM conf_registration WHERE deleted = 0 GROUP BY meeting_id`
    );
    console.log('按会议ID分布:');
    byMeeting.forEach(row => {
      console.log(`  ${row.meeting_id}: ${row.count}条`);
    });

    const [byTenant] = await conn.query(
      `SELECT tenant_id, COUNT(*) as count FROM conf_registration WHERE deleted = 0 GROUP BY tenant_id`
    );
    console.log('按租户ID分布:');
    byTenant.forEach(row => {
      console.log(`  ${row.tenant_id}: ${row.count}条`);
    });

    // 需要修复的SQL
    console.log('\n【需要执行的SQL修复语句】');
    console.log('-'.repeat(100));
    console.log(`UPDATE conf_registration`);
    console.log(`SET meeting_id = ${CORRECT_MEETING_ID},`);
    console.log(`    tenant_id = ${CORRECT_TENANT_ID}`);
    console.log(`WHERE meeting_id = 2030309010523144200 AND deleted = 0;`);

  } catch (error) {
    console.error('错误:', error.message);
  } finally {
    conn.release();
    await pool.end();
  }
}

verify();
