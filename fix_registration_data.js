const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: 'localhost',
  port: 3308,
  user: 'root',
  password: 'Hnhx@123',
  database: 'conference_registration'
});

async function fix() {
  const conn = await pool.getConnection();
  
  try {
    console.log('\n【执行修复】\n');
    
    // 修复SQL：改为正确的ID
    const fixSQL = `UPDATE conf_registration 
                    SET meeting_id = 2030309010523144194,
                        tenant_id = 2027317834622709762
                    WHERE meeting_id = 2030309010523144200 AND deleted = 0`;
    
    const result = await conn.query(fixSQL);
    console.log(`✅ 修复成功！`);
    console.log(`   - 更新了 ${result[0].affectedRows} 条记录`);
    
    // 验证修复后的结果
    const [verify] = await conn.query(
      `SELECT meeting_id, tenant_id, COUNT(*) as count 
       FROM conf_registration 
       WHERE deleted = 0 
       GROUP BY meeting_id, tenant_id`
    );
    
    console.log('\n修复后的数据分布:');
    console.table(verify);
    
  } catch (error) {
    console.error('❌ 修复失败:', error.message);
  } finally {
    conn.release();
    await pool.end();
  }
}

fix();
