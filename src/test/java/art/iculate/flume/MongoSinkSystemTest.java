package art.iculate.flume;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.conf.Configurables;
import org.apache.flume.event.SimpleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class MongoSinkSystemTest {

  private final String MONGO_COLLECTION = "json";
  private final String MONGO_DATABASE = "admin";
  private static final String MONGO_PASSWORD = "root";
  private static final String MONGO_USERNAME = "root";

  @Container
  public static GenericContainer mongo =
      new GenericContainer<>("mongo:8-noble")
          .withExposedPorts(27017)
          .withEnv("MONGO_INITDB_ROOT_USERNAME", MONGO_USERNAME)
          .withEnv("MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD);

  private MongoSink sink;

  @BeforeEach
  public void setUp() {
    sink = new MongoSink();
  }

  @Test
  public void storesInMongoDb() throws EventDeliveryException {
    final String jsonDocument = "{\"foo\": \"bar\"}";

    Channel channel = startSink(sink, createContext(mongo));

    Transaction txn = channel.getTransaction();
    txn.begin();
    Event event = new SimpleEvent();
    event.setBody(jsonDocument.getBytes());
    channel.put(event);
    txn.commit();
    txn.close();
    sink.process();
    sink.stop();

    List<String> mongoJsonDocuments =
        MongoUtil.getJsonDocuments(
            mongo.getHost(),
            mongo.getFirstMappedPort(),
            MONGO_USERNAME,
            MONGO_PASSWORD,
            MONGO_DATABASE,
            MONGO_COLLECTION);
    assertThat(mongoJsonDocuments, hasItem(jsonDocument));
  }

  @Test
  public void nonJsonBodyThrowsEventDeliveryException() throws EventDeliveryException {
    assertThrows(
        EventDeliveryException.class,
        () -> {
          Channel channel = startSink(sink, createContext(mongo));

          Transaction txn = channel.getTransaction();
          txn.begin();
          Event event = new SimpleEvent();
          String body = "invalid-body";
          event.setBody(body.getBytes());
          channel.put(event);
          txn.commit();
          txn.close();
          sink.process();
          sink.stop();
        });
  }

  private Context createContext(GenericContainer mongoContainer) {
    String hostName =
        String.format("%s:%s", mongoContainer.getHost(), mongoContainer.getFirstMappedPort());

    Context context = new Context();
    context.put("hostNames", hostName);
    context.put("database", MONGO_DATABASE);
    context.put("collection", MONGO_COLLECTION);
    context.put("user", MONGO_USERNAME);
    context.put("password", MONGO_PASSWORD);
    return context;
  }

  private Channel startSink(MongoSink sink, Context context) {
    Configurables.configure(sink, context);

    Channel channel = new MemoryChannel();
    Configurables.configure(channel, context);
    sink.setChannel(channel);
    sink.start();
    return channel;
  }
}
