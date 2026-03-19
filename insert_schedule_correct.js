const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration',
  supportBigNumbers: true,
  bigNumberStrings: true
});

async function executeSql() {
  let conn;
  try {
    conn = await pool.getConnection();
    
    // 清除旧数据
    console.log('[1] 清除旧数据...');
    await conn.query('DELETE FROM conf_schedule');
    
    // 插入新数据 - 使用字符串处理大数字
    console.log('[2] 插入新日程数据...');
    const MEETING_ID = '2030309010523144194';  // 作为字符串
    const TENANT_ID = '2027317834622709762';   // 作为字符串
    
    const meetings = [
      {id: 1001, title: '开幕式', startHour: 9, startMin: 0, endHour: 10, endMin: 0},
      {id: 1002, title: '主题报告：数字化转型', startHour: 10, startMin: 0, endHour: 11, endMin: 30},
      {id: 1003, title: '分论坛：人工智能应用', startHour: 11, startMin: 45, endHour: 13, endMin: 15},
      {id: 1004, title: '午餐休息', startHour: 13, startMin: 30, endHour: 14, endMin: 30},
      {id: 1005, title: '闭幕式', startHour: 14, startMin: 30, endHour: 15, endMin: 30}
    ];
    
    for (const m of meetings) {
      const startTime = new Date();
      startTime.setHours(m.startHour, m.startMin, 0, 0);
      
      const endTime = new Date();
      endTime.setHours(m.endHour, m.endMin, 0, 0);
      
      await conn.query(
        'INSERT INTO conf_schedule (id, meeting_id, tenant_id, title, start_time, end_time, location, host, speaker, status, priority, created_by, created_time, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 2, 8, NOW(), 0)',
        [m.id, MEETING_ID, TENANT_ID, m.title, startTime, endTime, '主会场', '主持人', '演讲者']
      );
      console.log('  ✓ ' + m.id + ': ' + m.title);
    }
    
    // 验证
    const [result] = await conn.query(
      'SELECT COUNT(*) as cnt FROM conf_schedule WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0',
      [MEETING_ID, TENANT_ID]
    );
    
    console.log('[SUCCESS] ✓ 已插入 ' + result[0].cnt + ' 条日程');
    console.log('[NEXT] 请刷新浏览器页面查看数据');
    
  } catch (error) {
    console.error('[ERROR] ' + error.message);
  } finally {
    if (conn) conn.release();
    await pool.end();
  }
}

executeSql();
