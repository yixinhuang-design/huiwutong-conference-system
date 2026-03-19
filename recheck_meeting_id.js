const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration'
});

async function recheck() {
  const conn = await pool.getConnection();
  
  try {
    console.log('\n' + '='.repeat(100));
    console.log('重新核实会议ID');
    console.log('='.repeat(100));

    // 查看所有会议
    console.log('\n【所有会议列表】');
    console.log('-'.repeat(100));
    const [allMeetings] = await conn.query(
      `SELECT id, meeting_name, meeting_code, tenant_id FROM conf_meeting ORDER BY id DESC LIMIT 10`
    );
    console.table(allMeetings);

    // 查看这两个特定ID的会议是否都存在
    console.log('\n【特定会议ID检查】');
    console.log('-'.repeat(100));
    const [specific] = await conn.query(
      `SELECT id, meeting_name, tenant_id FROM conf_meeting 
       WHERE id IN (2030309010523144194, 2030309010523144200)`
    );
    console.log('存在的会议:');
    specific.forEach(m => {
      console.log(`  ID: ${m.id}`);
      console.log(`  名称: ${m.meeting_name}`);
      console.log(`  租户ID: ${m.tenant_id}\n`);
    });

    // 检查2030309010523144194这个ID是否存在
    console.log('\n【2030309010523144194是否存在】');
    console.log('-'.repeat(100));
    const [check194] = await conn.query(
      `SELECT COUNT(*) as count FROM conf_meeting WHERE id = 2030309010523144194`
    );
    console.log(`结果: ${check194[0].count} (${check194[0].count > 0 ? '存在' : '不存在'})`);

    // 检查2030309010523144200这个ID是否存在
    console.log('\n【2030309010523144200是否存在】');
    console.log('-'.repeat(100));
    const [check200] = await conn.query(
      `SELECT COUNT(*) as count FROM conf_meeting WHERE id = 2030309010523144200`
    );
    console.log(`结果: ${check200[0].count} (${check200[0].count > 0 ? '存在' : '不存在'})`);

    // 查看conf_registration中有哪些meeting_id
    console.log('\n【报名数据中的所有meeting_id及其数量】');
    console.log('-'.repeat(100));
    const [regMeetings] = await conn.query(
      `SELECT meeting_id, COUNT(*) as count 
       FROM conf_registration 
       WHERE deleted = 0 
       GROUP BY meeting_id 
       ORDER BY count DESC`
    );
    console.table(regMeetings);

    // 重点：前端打开的会议ID应该是什么？
    console.log('\n【关键问题】');
    console.log('-'.repeat(100));
    console.log('请确认：你在前端打开的会议ID是多少？');
    console.log('从UI上能看到会议的名称吗？这样可以确认到底是哪个ID。');

    console.log('\n' + '='.repeat(100) + '\n');

  } catch (error) {
    console.error('检查出错:', error.message);
  } finally {
    conn.release();
    await pool.end();
  }
}

recheck();
