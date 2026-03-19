const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

async function insertSchedules() {
  let conn;
  try {
    conn = await pool.getConnection();
    
    const MEETING_ID = 2030309010523144194;
    const TENANT_ID = 2027317834622709762;
    const CREATED_BY = 8;
    
    // 检查现有数据
    const [existing] = await conn.query(
      'SELECT COUNT(*) as cnt FROM conf_schedule WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0',
      [MEETING_ID, TENANT_ID]
    );
    console.log('[CHECK] 现有日程数: ' + existing[0].cnt);
    
    if (existing[0].cnt > 0) {
      console.log('[INFO] 数据已存在，清除旧数据...');
      await conn.query(
        'DELETE FROM conf_schedule WHERE meeting_id = ? AND tenant_id = ?',
        [MEETING_ID, TENANT_ID]
      );
    }

    // 逐条插入，确保每条都成功
    const schedules = [
      {
        id: 2001,
        title: '开幕式',
        start: '09:00:00',
        end: '10:00:00',
        location: '主会场A厅',
        host: '李主任',
        speaker: '张总经理'
      },
      {
        id: 2002,
        title: '主题报告：数字化转型',
        start: '10:00:00',
        end: '11:30:00',
        location: '主会场A厅',
        host: '王主持',
        speaker: '赵教授'
      },
      {
        id: 2003,
        title: '分论坛：人工智能应用',
        start: '11:45:00',
        end: '13:15:00',
        location: '分会场B厅',
        host: '陈主持',
        speaker: '刘博士'
      },
      {
        id: 2004,
        title: '午餐休息',
        start: '13:30:00',
        end: '14:30:00',
        location: '餐饮区',
        host: null,
        speaker: null
      },
      {
        id: 2005,
        title: '闭幕式',
        start: '14:30:00',
        end: '15:30:00',
        location: '主会场A厅',
        host: '李主任',
        speaker: '李主任'
      }
    ];

    for (const sched of schedules) {
      const startTime = new Date();
      startTime.setHours(parseInt(sched.start.split(':')[0]), parseInt(sched.start.split(':')[1]));
      
      const endTime = new Date();
      endTime.setHours(parseInt(sched.end.split(':')[0]), parseInt(sched.end.split(':')[1]));
      
      await conn.query(
        'INSERT INTO conf_schedule (id, meeting_id, tenant_id, title, start_time, end_time, location, host, speaker, status, priority, created_by, created_time, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 0)',
        [sched.id, MEETING_ID, TENANT_ID, sched.title, startTime, endTime, sched.location, sched.host, sched.speaker, 1, 2, CREATED_BY]
      );
      console.log('[INSERT] ' + sched.id + ': ' + sched.title);
    }

    // 验证
    const [final] = await conn.query(
      'SELECT COUNT(*) as cnt FROM conf_schedule WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0',
      [MEETING_ID, TENANT_ID]
    );
    console.log('[SUCCESS] 最终日程数: ' + final[0].cnt);
    
  } catch (error) {
    console.error('[ERROR] ' + error.message);
    console.error('[STACK] ' + error.stack);
  } finally {
    if (conn) conn.release();
    await pool.end();
  }
}

insertSchedules();
