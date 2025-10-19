package art.iculate.flume;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mongodb.ServerAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MongoSinkTest {

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of("host1,host2", 27017, 27017),
        Arguments.of("host1:1234,host2:2345", 1234, 2345),
        Arguments.of("  host1:1234, host2:2345  ", 1234, 2345));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serverAddresses(String hostNames, int firstPort, int secondPort) {
    List<ServerAddress> expected = new ArrayList<>();
    expected.add(new ServerAddress("host1", firstPort));
    expected.add(new ServerAddress("host2", secondPort));

    assertThat(MongoSink.parseHostnames(hostNames), is(equalTo(expected)));
  }
}
