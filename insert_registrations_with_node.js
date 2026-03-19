const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration'
});

async function insertRegistrations() {
  const conn = await pool.getConnection();
  
  try {
    const MEETING_ID = 2030309010523144194;
    const TENANT_ID = 2027317834622709762;

    // 先清理该会议下的旧数据，避免重复执行导致重复记录
    await conn.query(
      'DELETE FROM conf_registration WHERE meeting_id = ? AND tenant_id = ?',
      [MEETING_ID, TENANT_ID]
    );
    console.log('[OK] Cleared existing registrations for this meeting');

    // 插入20条报名数据
    const registrations = [
      { name: '李明', phone: '13800138001', organization: '北京市政府', position: '处长', email: 'li@example.com' },
      { name: '王芳', phone: '13800138002', organization: '上海市政府', position: '科长', email: 'wang@example.com' },
      { name: '张军', phone: '13800138003', organization: '深圳市政府', position: '副主任', email: 'zhang@example.com' },
      { name: '刘丽', phone: '13800138004', organization: '广州市政府', position: '主任', email: 'liu@example.com' },
      { name: '陈伟', phone: '13800138005', organization: '杭州市政府', position: '处长', email: 'chen@example.com' },
      { name: '周娜', phone: '13800138006', organization: '南京市政府', position: '科员', email: 'zhou@example.com' },
      { name: '吴强', phone: '13800138007', organization: '苏州市政府', position: '副处长', email: 'wu@example.com' },
      { name: '郭燕', phone: '13800138008', organization: '成都市政府', position: '科长', email: 'guo@example.com' },
      { name: '何涛', phone: '13800138009', organization: '武汉市政府', position: '处长', email: 'he@example.com' },
      { name: '游倩', phone: '13800138010', organization: '西安市政府', position: '主任', email: 'you@example.com' },
      { name: '邱林', phone: '13800138011', organization: '北京大学', position: '教授', email: 'qiu@example.com' },
      { name: '唐静', phone: '13800138012', organization: '清华大学', position: '副教授', email: 'tang@example.com' },
      { name: '彭晓', phone: '13800138013', organization: '浙江大学', position: '讲师', email: 'peng@example.com' },
      { name: '曾浩', phone: '13800138014', organization: '复旦大学', position: '教授', email: 'zeng@example.com' },
      { name: '蔡琪', phone: '13800138015', organization: '中科院', position: '研究员', email: 'cai@example.com' },
      { name: '冷月', phone: '13800138016', organization: '人民日报', position: '记者', email: 'leng@example.com' },
      { name: '洪涛', phone: '13800138017', organization: '新华社', position: '编辑', email: 'hong@example.com' },
      { name: '章秋', phone: '13800138018', organization: '央视', position: '制片人', email: 'zhang2@example.com' },
      { name: '孟涛', phone: '13800138019', organization: '中国工程院', position: '院士', email: 'meng@example.com' },
      { name: '常欣', phone: '13800138020', organization: '国务院', position: '主任', email: 'chang@example.com' }
    ];

    for (const reg of registrations) {
      await conn.query(
        'INSERT INTO conf_registration (tenant_id, meeting_id, name, phone, id_card, organization, position, email, status, create_time, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), 0)',
        [TENANT_ID, MEETING_ID, reg.name, reg.phone, '110101199001011234', reg.organization, reg.position, reg.email, 1]
      );
    }
    
    console.log('[OK] Inserted 20 registrations');

    const [rows] = await conn.query(
      'SELECT COUNT(*) as total FROM conf_registration WHERE meeting_id = ? AND tenant_id = ? AND deleted = 0',
      [MEETING_ID, TENANT_ID]
    );
    console.log('[OK] Total registrations: ' + rows[0].total);
    console.log('[SUCCESS] Registration data inserted! Refresh browser to see the summary.');
    
  } catch (error) {
    console.error('[ERROR] ' + error.message);
  } finally {
    conn.release();
    await pool.end();
  }
}

insertRegistrations();
