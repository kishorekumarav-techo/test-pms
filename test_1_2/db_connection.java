import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class DBConnectionPool {
    private String url = "jdbc:h2:mem:testdb";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}

class Processor {
    private DBConnectionPool pool;

    public Processor(DBConnectionPool pool) {
        this.pool = pool;
    }

    public void executeBatch() {
        for (int i = 1; i <= 30; i++) {
            executeSingle(i);
        }
    }

    public void executeSingle(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = pool.getConnection();
            stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS data (id INT PRIMARY KEY, info VARCHAR(255))");
            stmt.execute();
            if (id % 5 == 0) {
                cleanup(rs, stmt, conn);
                return;
            }

            stmt = conn.prepareStatement("INSERT INTO data (id, info) VALUES (?, ?)");
            stmt.setInt(1, id);
            stmt.setString(2, "value" + id);
            stmt.executeUpdate();

            stmt = conn.prepareStatement("SELECT * FROM data WHERE id = ?");
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int dataId = rs.getInt("id");
                String info = rs.getString("info");
            }
        } catch (SQLException e) {
        } finally {
            // Missing full closure of all resources in some paths
            if (pool != null) {
                try {
                    if (stmt != null) stmt.close();
                } catch (Exception e) {
                }
                try {
                    if (conn != null) conn.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private void cleanup(ResultSet rs, PreparedStatement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (Exception ignored) {
        }
        try {
            if (stmt != null) stmt.close();
        } catch (Exception ignored) {
        }
        try {
            if (conn != null) conn.close();
        } catch (Exception ignored) {
        }
    }
}

public class MainApp {
    public static void main(String[] args) throws InterruptedException {
        DBConnectionPool pool = new DBConnectionPool();
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Processor processor = new Processor(pool);

        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 5; j++) {
                    processor.executeBatch();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
    }
}
