import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String rawPassword = "123456";
        String dbPassword = "$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUe";
        
        // 测试 1: 验证数据库中的密码
        boolean matches = encoder.matches(rawPassword, dbPassword);
        System.out.println("原始密码 '123456' 与数据库密码是否匹配: " + matches);
        
        // 测试 2: 生成新的 BCrypt 密码（用于对比）
        String newHash = encoder.encode(rawPassword);
        System.out.println("新生成的 BCrypt 密码: " + newHash);
        
        // 测试 3: 验证新密码
        boolean newMatches = encoder.matches(rawPassword, newHash);
        System.out.println("新密码验证结果: " + newMatches);
        
        // 测试 4: 检查数据库密码格式
        System.out.println("\n数据库密码分析:");
        System.out.println("格式检查:");
        System.out.println("  - 以 $2a$ 开头: " + dbPassword.startsWith("$2a$"));
        System.out.println("  - 长度: " + dbPassword.length() + " (应该是 60)");
        System.out.println("  - 完整密码: " + dbPassword);
    }
}
