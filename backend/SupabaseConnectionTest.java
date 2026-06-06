import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SupabaseConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://db.epynhaubdajbrtiavzrc.supabase.co:5432/postgres";
        String user = "postgres";
        String password = "RAGSupabase";

        System.out.println("Testing Supabase connection...");
        System.out.println("URL: " + url);
        System.out.println("User: " + user);

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ SUCCESS: Connected to Supabase database!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("❌ FAILED: Could not connect to Supabase");
            System.out.println("Error: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }
}