import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReviewConsumerThread implements Runnable{

  private final Connection connection;
  private final String queueName;
  private final String exchangeName;
  private final String exchangeType;
  private final Gson gson = new Gson();


  public ReviewConsumerThread(Connection connection, String queueName, String exchangeName, String exchangeType) {
    this.connection = connection;
    this.queueName = queueName;
    this.exchangeName = exchangeName;
    this.exchangeType = exchangeType;
  }


  @Override
  public void run() {
    try {
      Channel channel = connection.createChannel();
      channel.exchangeDeclare(exchangeName, exchangeType);
      channel.queueDeclare(queueName, false, false, false, null);
      channel.queueBind(queueName, exchangeName, queueName);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        ReceiveMessage(channel, delivery);
      };

      boolean autoAck = true;
      channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {
      });

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void ReceiveMessage(Channel channel, com.rabbitmq.client.Delivery delivery) throws IOException {
    String message = new String(delivery.getBody(), "UTF-8");
    JsonObject messageJson = gson.fromJson(message, JsonObject.class);
    String albumId = messageJson.get("albumId").getAsString();
    String action = messageJson.get("action").getAsString();
    String sql = "";
    if (action.equals("like")) {
      sql = "UPDATE albums SET likes = likes + 1 WHERE album_id = " + albumId;
    } else if (action.equals("dislike")) {
      sql = "UPDATE albums SET dislikes = dislikes + 1 WHERE album_id = " + albumId;
    }
    try (java.sql.Connection connection = setupDatabaseConnection()) {
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    System.out.println(" [x] Received '" + message + "'");

  }

  private java.sql.Connection setupDatabaseConnection() {
    try {
      return DatabaseConnectionPool.getConnection();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }



}
