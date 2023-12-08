import com.google.gson.Gson;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

@WebServlet(name = "AlbumServlet", value = "/albums/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,
                 maxFileSize = 1024 * 1024 * 50,
                 maxRequestSize = 1024 * 1024 * 100)
public class AlbumServlet extends HttpServlet {

  static Map<Integer, Album> albumMap = new HashMap<>();
  static class Album {
    public byte[] imageContent;
    Profile albumInfo;
  }

  static class Profile {
    String artist;
    String title;
    String year;

  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    try {
      res.setContentType("application/json");
      String urlPath = req.getPathInfo();
      if (urlPath == null || urlPath.isEmpty()) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write(new Gson().toJson("URL missing parameter."));
        return;
      }

      String[] urlParts = urlPath.split("/");

      if(!isUrlValid(urlParts)) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write(new Gson().toJson("URL format is incorrect."));
        return;
      } else {
        int albumId = Integer.parseInt(urlParts[1]);
        //Album album = albumMap.get(albumId);

        try (Connection connection = setupDatabaseConnection()) {
          String sql = "SELECT artist, title, year FROM albums WHERE album_id = ?";
          try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, albumId);

            try (ResultSet resultSet = statement.executeQuery()) {
              if (resultSet.next()) {
                Profile albumInfo1 = new Profile();
                albumInfo1.artist = resultSet.getString("artist");
                albumInfo1.title = resultSet.getString("title");
                albumInfo1.year = resultSet.getString("year");
                res.setStatus(HttpServletResponse.SC_OK);
                res.setContentType("application/json");
                res.getWriter().write(new Gson().toJson(albumInfo1));
              } else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.setContentType("application/json");
                res.getWriter().write(new Gson().toJson("Album not found."));
              }
            }

            try {
              connection.close();
              statement.close();
            } catch (SQLException e) {
              e.printStackTrace();
            }
          }
        } catch (Exception e) {
          // Handle exceptions and send error response
          res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          res.setContentType("application/json");
          res.getWriter().write(new Gson().toJson("Cannot connect to database."));
          e.printStackTrace();
        }

      }
    } catch (Exception e) {
      res.getWriter().write(e.getMessage());
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    try {
      res.setContentType("application/json");

      //check multipart
      if (!ServletFileUpload.isMultipartContent(req)) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write(new Gson().toJson("Request is not multipart."));
        return;
      }
      String urlPath = req.getPathInfo();
      ServletFileUpload upload = new ServletFileUpload();
      FileItemIterator iter = upload.getItemIterator(req);
      Album album1 = new Album();
      Profile profile = new Profile();

      while(iter.hasNext()) {
        FileItemStream item = iter.next();
        String name = item.getFieldName();
        InputStream stream = item.openStream();
        if (!item.isFormField()) {
          if ("image".equals(name)) {
            album1.imageContent = IOUtils.toByteArray(stream);  // Apache Commons IO
          }
        } else {
          if ("profile".equals(name)) {
            String albumInfoJson = IOUtils.toString(stream, String.valueOf(StandardCharsets.UTF_8));
            profile = new Gson().fromJson(albumInfoJson, Profile.class);
          }
        }
      }
      album1.albumInfo = profile;
      //int id1 = albumMap.size() + 1;
      //albumMap.put(id1, album1);
      try (Connection connection = setupDatabaseConnection()) {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO albums (artist, title, year, image, likes, dislikes) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, album1.albumInfo.artist);
        statement.setString(2, album1.albumInfo.title);
        statement.setString(3, album1.albumInfo.year);
        statement.setBytes(4, album1.imageContent);
        statement.setInt(5, 0);
        statement.setInt(6, 0);
        int rowsInserted = statement.executeUpdate();
        if (rowsInserted == 0) {
          throw new SQLException("Creating album failed, no rows affected.");
        }
        try (ResultSet resultSet = statement.getGeneratedKeys()){
          if (resultSet.next()) {
            long id1 = resultSet.getLong(1);
            System.out.println("Album created with id: " + id1);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("albumId", id1);
            responseData.put("imageSize", album1.imageContent.length);
            String responseMessage = new Gson().toJson(responseData);
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(responseMessage);
          } else {
            throw new SQLException("Creating album failed, no ID obtained.");
          }
        }
        try {
          connection.close();
          statement.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      } catch (Exception e) {
        // Handle exceptions and send error response
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.setContentType("application/json");
        res.getWriter().write(new Gson().toJson("Cannot connect to database."));
        e.printStackTrace();
      }
    } catch (Exception e) {
      res.getWriter().write(e.getMessage());
    }
  }


  boolean isUrlValid(String[] urlPath) {
    if (urlPath.length != 2) {
      return false;
    }
    String str = urlPath[1];
    for (int i = 0; i < str.length(); i++) {
      System.out.println(str.charAt(i));
      if (!Character.isDigit(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private Connection setupDatabaseConnection() {
    try {
      return DatabaseConnectionPool.getConnection();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}