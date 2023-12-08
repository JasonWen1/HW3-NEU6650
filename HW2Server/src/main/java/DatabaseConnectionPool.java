import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbcp2.BasicDataSource;

public class DatabaseConnectionPool {



  public static final String URL = "jdbc:mysql://localhost:3306/";
  public static final String DATABASE_NAME = "HW3";
  public static final String USERNAME = "root";
  public static final String PASSWORD = "";

  static BasicDataSource dataSource = new BasicDataSource();

  static {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    dataSource.setUrl(URL + DATABASE_NAME);
    dataSource.setUsername(USERNAME);
    dataSource.setPassword(PASSWORD);
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    dataSource.setInitialSize(20);
    dataSource.setMaxTotal(60);

    checkAndCreateDatabase();
    checkAndCreateAlbumsTable();
  }

  public static BasicDataSource getDataSource() {
    return dataSource;
  }

  public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  private static void checkAndCreateDatabase() {
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
      Statement statement = connection.createStatement();
      statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }


  private static void checkAndCreateAlbumsTable() {
    Connection connection = null;
    try {
      connection = getConnection();
      Statement statement = connection.createStatement();
      statement.executeUpdate("CREATE TABLE IF NOT EXISTS albums ("
          + "album_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
          + "artist VARCHAR(255),"
          + "title VARCHAR(255),"
          + "year VARCHAR(255),"
          + "image LONGBLOB,"
          + "likes INT DEFAULT 0,"
          + "dislikes INT DEFAULT 0"
          + ")");
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }




}
