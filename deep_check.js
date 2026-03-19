const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration'
});

async function deepCheck() {
  const conn = await pool.getConnection();
  
  try {
    console.log('\n【直接查询两个ID】\n');
    
    // 查询194
    const [result194] = await conn.query(
      `SELECT * FROM conf_meeting WHERE id = 2030309010523144194 LIMIT 1`
    );
    console.log('ID = 2030309010523144194:');
    if (result194.length > 0) {
      console.log('  ✓ 存在');
      console.log(`  会议名: ${result194[0].meeting_name}`);
    } else {
      console.log('  ✗ 不存在');
    }

    // 查询200
    const [result200] = await conn.query(
      `SELECT * FROM conf_meeting WHERE id = 2030309010523144200 LIMIT 1`
    );
    console.log('\nID = 2030309010523144200:');
    if (result200.length > 0) {
      console.log('  ✓ 存在');
      console.log(`  会议名: ${result200[0].meeting_name}`);
    } else {
      console.log('  ✗ 不存在');
    }

    // 检查数据库中是否真的有这些数据
    console.log('\n【数据库中实际存在的conf_meeting记录】\n');
    const [allMeetings] = await conn.query(
      `SELECT id, meeting_name FROM conf_meeting`
    );
    console.log(`共${allMeetings.length}条记录:`);
    allMeetings.forEach(m => {
      console.log(`  ${m.id} - ${m.meeting_name}`);
    });

  } catch (error) {
    console.error('错误:', error.message);
  } finally {
    conn.release();
    await pool.end();
  }
}

deepCheck();
