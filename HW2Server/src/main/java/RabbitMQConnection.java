import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnection {
  private static final int CHANNEL_POOL_SIZE = 100;
  private static final String EXCHANGE_NAME = "review";

  public ConcurrentLinkedDeque<Channel> createChannelPool(String host) throws IOException, TimeoutException {
    ConcurrentLinkedDeque<Channel> channelPool = new ConcurrentLinkedDeque<>();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    for (int i = 0; i < CHANNEL_POOL_SIZE; i++) {
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();
      channel.exchangeDeclare(EXCHANGE_NAME, "direct");
      channelPool.add(channel);
    }
    return channelPool;
  }

  public void closeChannelPool(ConcurrentLinkedDeque<Channel> channelPool) throws IOException, TimeoutException {
    for (Channel channel : channelPool) {
      channel.close();
    }
  }


}
