package art.iculate.flume;

import com.mongodb.ServerAddress;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MongoSinkTest {

  @Test
  public void serverAddressesWithoutPortsGetDefaultMongoPort() {
    List<ServerAddress> expected = new ArrayList<>();
    expected.add(new ServerAddress("host1", 27017));
    expected.add(new ServerAddress("host2", 27017));

    assertThat(MongoSink.parseHostnames("host1,host2"), is(equalTo(expected)));
  }

  @Test
  public void serverAddressesWithPorts() {
    List<ServerAddress> expected = new ArrayList<>();
    expected.add(new ServerAddress("host1", 1234));
    expected.add(new ServerAddress("host2", 2345));

    assertThat(MongoSink.parseHostnames("host1:1234,host2:2345"), is(equalTo(expected)));
  }

  @Test
  public void junkWhitespaceIsRemovedFromHostNames() {
    List<ServerAddress> expected = new ArrayList<>();
    expected.add(new ServerAddress("host1", 1234));
    expected.add(new ServerAddress("host2", 2345));

    assertThat(MongoSink.parseHostnames("  host1:1234, host2:2345  "), is(equalTo(expected)));
  }

}
