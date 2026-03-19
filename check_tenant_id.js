const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration'
});

async function checkTenantId() {
  const conn = await pool.getConnection();
  
  try {
    console.log('\n【租户ID核实】\n');
    
    // 查询插入脚本中的租户ID
    console.log('插入脚本中的租户ID: 2027317834622709762');
    const [tenant1] = await conn.query(
      `SELECT COUNT(*) as count FROM conf_meeting WHERE tenant_id = 2027317834622709762`
    );
    console.log(`  - conf_meeting表中有${tenant1[0].count}条记录`);

    const [tenant1Reg] = await conn.query(
      `SELECT COUNT(*) as count FROM conf_registration WHERE tenant_id = 2027317834622709762`
    );
    console.log(`  - conf_registration表中有${tenant1Reg[0].count}条记录`);

    // 查询数据库中实际的租户ID
    console.log('\n数据库中实际的租户ID: 2027317834622709800');
    const [tenant2] = await conn.query(
      `SELECT COUNT(*) as count FROM conf_meeting WHERE tenant_id = 2027317834622709800`
    );
    console.log(`  - conf_meeting表中有${tenant2[0].count}条记录`);

    const [tenant2Reg] = await conn.query(
      `SELECT COUNT(*) as count FROM conf_registration WHERE tenant_id = 2027317834622709800`
    );
    console.log(`  - conf_registration表中有${tenant2Reg[0].count}条记录`);

    // 查看会议2030309010523144194对应的租户ID
    console.log('\n会议ID=2030309010523144194 对应的租户ID:');
    const [meeting] = await conn.query(
      `SELECT id, meeting_name, tenant_id FROM conf_meeting WHERE id = 2030309010523144194`
    );
    if (meeting.length > 0) {
      console.log(`  会议名: ${meeting[0].meeting_name}`);
      console.log(`  租户ID: ${meeting[0].tenant_id}`);
    }

    // 查看conf_registration中报名数据用的租户ID
    console.log('\n报名数据中使用的租户ID:');
    const [regTenants] = await conn.query(
      `SELECT DISTINCT tenant_id, COUNT(*) as count 
       FROM conf_registration 
       WHERE deleted = 0 
       GROUP BY tenant_id`
    );
    regTenants.forEach(t => {
      console.log(`  ${t.tenant_id}: ${t.count}条`);
    });

    // 结论
    console.log('\n【结论】');
    console.log('-'.repeat(100));
    const meetingTenantId = meeting.length > 0 ? meeting[0].tenant_id : null;
    console.log(`会议2030309010523144194的租户ID是: ${meetingTenantId}`);
    console.log(`这才是正确的租户ID，需要把报名数据改成这个ID`);

  } catch (error) {
    console.error('错误:', error.message);
  } finally {
    conn.release();
    await pool.end();
  }
}

checkTenantId();
