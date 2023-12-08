import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewConsumer {

  private final static String LIKE_QUEUE_NAME = "like";
  private final static String DISLIKE_QUEUE_NAME = "dislike";
  private final static String EXCHANGE_NAME = "review";
  private final static String EXCHANGE_TYPE = "direct";
  private final static String HOST = "localhost";
  public final static int MAX_POOL_SIZE = 100;

  public static void main(String[] argv) throws Exception {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(HOST);

    Connection likeConnection = factory.newConnection();
    Connection dislikeConnection = factory.newConnection();

    ExecutorService executorServiceLike = Executors.newFixedThreadPool(MAX_POOL_SIZE);
    ExecutorService executorServiceDislike = Executors.newFixedThreadPool(MAX_POOL_SIZE);

    for (int i = 0; i < MAX_POOL_SIZE; i++) {
      executorServiceLike.execute(new ReviewConsumerThread(likeConnection, LIKE_QUEUE_NAME, EXCHANGE_NAME, EXCHANGE_TYPE));
      executorServiceDislike.execute(new ReviewConsumerThread(dislikeConnection, DISLIKE_QUEUE_NAME, EXCHANGE_NAME, EXCHANGE_TYPE));
    }


  }

}
