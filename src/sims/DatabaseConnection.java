package sims;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection connect() {
        try {
            // Load SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String pass = System.getenv("DB_PASS");

            if (url == null) url = "jdbc:sqlserver://LPT-JOYAHEL;databaseName=DBSIMS;encrypt=true;trustServerCertificate=true;";
            if (user == null) user = "sa";
            if (pass == null) pass = "password123@";
            
            Connection conn = DriverManager.getConnection(url, user, pass);
            
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("SQL Server JDBC Driver not found.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
            return null;
        }
    }
}
