const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration'
});

async function insertSchedules() {
  const conn = await pool.getConnection();
  
  try {
    const MEETING_ID = 2030309010523144194;
    const TENANT_ID = 2027317834622709762;
    const CREATED_BY = 8;

    // Insert 5 schedules one by one
    await conn.query('INSERT INTO conf_schedule (id, meeting_id, tenant_id, title, start_time, end_time, location, host, speaker, status, priority, created_by, created_time, deleted) VALUES (1001, ?, ?, ?, DATE_ADD(CURDATE(), INTERVAL 9 HOUR), DATE_ADD(CURDATE(), INTERVAL 10 HOUR), ?, ?, ?, 1, 3, ?, NOW(), 0)', [MEETING_ID, TENANT_ID, '开幕式', '主会场A厅', '李主任', '张总经理', CREATED_BY]);
    
    await conn.query('INSERT INTO conf_schedule (id, meeting_id, tenant_id, title, start_time, end_time, location, host, speaker, status, priority, created_by, created_time, deleted) VALUES (1002, ?, ?, ?, DATE_ADD(CURDATE(), INTERVAL 10 HOUR), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 10 HOUR), INTERVAL 90 MINUTE), ?, ?, ?, 1, 2, ?, NOW(), 0)', [MEETING_ID, TENANT_ID, '主题报告：数字化转型', '主会场A厅', '王主持', '赵教授', CREATED_BY]);
    
    await conn.query('INSERT INTO conf_schedule (id, meeting_id, tenant_id, title, start_time, end_time, location, host, speaker, status, priority, created_by, created_time, deleted) VALUES (1003, ?, ?, ?, DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 11 HOUR), INTERVAL 45 MINUTE), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 13 HOUR), INTERVAL 15 MINUTE), ?, ?, ?, 1, 2, ?, NOW(), 0)', [MEETING_ID, TENANT_ID, '分论坛：人工智能应用', '分会场B厅', '陈主持', '刘博士', CREATED_BY]);
    
    await conn.query('INSERT INTO conf_schedule (id, meeting_id, tenant_id, title, start_time, end_time, location, host, speaker, status, priority, created_by, created_time, deleted) VALUES (1004, ?, ?, ?, DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 13 HOUR), INTERVAL 30 MINUTE), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 14 HOUR), INTERVAL 30 MINUTE), ?, NULL, NULL, 1, 1, ?, NOW(), 0)', [MEETING_ID, TENANT_ID, '午餐休息', '餐饮区', CREATED_BY]);
    
    await conn.query('INSERT INTO conf_schedule (id, meeting_id, tenant_id, title, start_time, end_time, location, host, speaker, status, priority, created_by, created_time, deleted) VALUES (1005, ?, ?, ?, DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 14 HOUR), INTERVAL 30 MINUTE), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 15 HOUR), INTERVAL 30 MINUTE), ?, ?, ?, 1, 3, ?, NOW(), 0)', [MEETING_ID, TENANT_ID, '闭幕式', '主会场A厅', '李主任', '李主任', CREATED_BY]);
    
    console.log('[OK] Inserted 5 schedules');

    const [rows] = await conn.query('SELECT COUNT(*) as total FROM conf_schedule WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0', [MEETING_ID, TENANT_ID]);
    console.log('[OK] Total schedules: ' + rows[0].total);
    console.log('[SUCCESS] Data inserted! Refresh browser to see schedules.');
    
  } catch (error) {
    console.error('[ERROR] ' + error.message);
  } finally {
    conn.release();
    await pool.end();
  }
}

insertSchedules();
