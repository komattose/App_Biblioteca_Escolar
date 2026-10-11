package biblioteca.config;

import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/**
 * Una instancia y una conexión física por aplicación. Todas las operaciones utilizan el mismo
 * monitor, evitando intercalar transacciones en la conexión.
 */
public final class DatabaseConnection {
  private static final DatabaseConnection INSTANCE = new DatabaseConnection();
  private Connection connection;

  private DatabaseConnection() {}

  public static DatabaseConnection getInstance() {
    return INSTANCE;
  }

  @FunctionalInterface
  public interface SqlWork<T> {
    T run(Connection c) throws Exception;
  }

  private Connection getConnection() throws Exception {
    if (connection == null || connection.isClosed() || !connection.isValid(2)) {
      Properties p = new Properties();
      Path file = Path.of(System.getProperty("biblioteca.config", "config.properties"));
      if (Files.exists(file))
        try (var in = Files.newInputStream(file)) {
          p.load(in);
        }
      String url =
          System.getenv()
              .getOrDefault(
                  "BIBLIOTECA_DB_URL",
                  p.getProperty(
                      "db.url",
                      "jdbc:mysql://localhost:3306/biblioteca_escolar?connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=15000"));
      String user =
          System.getenv()
              .getOrDefault("BIBLIOTECA_DB_USER", p.getProperty("db.user", "biblioteca_app"));
      String pass =
          System.getenv()
              .getOrDefault(
                  "BIBLIOTECA_DB_PASSWORD", p.getProperty("db.password", "BibliotecaLocal2026!"));
      connection = DriverManager.getConnection(url, user, pass);
    }
    return connection;
  }

  public synchronized <T> T read(SqlWork<T> work) throws Exception {
    return work.run(getConnection());
  }

  public synchronized <T> T transaction(SqlWork<T> work) throws Exception {
    Connection c = getConnection();
    c.setAutoCommit(false);
    try {
      T value = work.run(c);
      c.commit();
      return value;
    } catch (Exception e) {
      try {
        c.rollback();
      } catch (SQLException rollback) {
        e.addSuppressed(rollback);
      }
      throw e;
    } finally {
      c.setAutoCommit(true);
    }
  }

  public synchronized void close() throws SQLException {
    if (connection != null) connection.close();
  }
}
