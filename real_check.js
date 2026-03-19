const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration'
});

async function realCheck() {
  const conn = await pool.getConnection();
  
  try {
    console.log('\n【真实情况检查】\n');
    
    // 查看会议2030309010523144194
    const [m194] = await conn.query(
      `SELECT id, meeting_name, tenant_id FROM conf_meeting WHERE id = 2030309010523144194`
    );
    console.log('会议2030309010523144194:');
    if (m194.length > 0) {
      console.log(`  存在`);
      console.log(`  名称: ${m194[0].meeting_name}`);
      console.log(`  租户ID: ${m194[0].tenant_id}`);
    } else {
      console.log(`  不存在`);
    }

    // 查看会议2030309010523144200
    const [m200] = await conn.query(
      `SELECT id, meeting_name, tenant_id FROM conf_meeting WHERE id = 2030309010523144200`
    );
    console.log('\n会议2030309010523144200:');
    if (m200.length > 0) {
      console.log(`  存在`);
      console.log(`  名称: ${m200[0].meeting_name}`);
      console.log(`  租户ID: ${m200[0].tenant_id}`);
    } else {
      console.log(`  不存在`);
    }

    // 报名数据现状
    console.log('\n报名数据现状:');
    const [reg] = await conn.query(
      `SELECT meeting_id, tenant_id, COUNT(*) as count FROM conf_registration WHERE deleted = 0 GROUP BY meeting_id, tenant_id`
    );
    console.table(reg);

    // 哪个会议才是有效的？
    console.log('\n【用户确认】');
    console.log('您说的是：');
    console.log('  - 会议ID: 2030309010523144194');
    console.log('  - 租户ID: 2027317834622709762');
    console.log('\n请确认这两个ID在数据库的conf_meeting表中是否确实存在且匹配？');

  } catch (error) {
    console.error('错误:', error.message);
  } finally {
    conn.release();
    await pool.end();
  }
}

realCheck();
