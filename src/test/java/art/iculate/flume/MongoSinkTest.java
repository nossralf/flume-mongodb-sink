package art.iculate.flume;

import com.mongodb.ServerAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class MongoSinkTest {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"host1,host2", 27017, 27017},
        {"host1:1234,host2:2345", 1234, 2345},
        {"  host1:1234, host2:2345  ", 1234, 2345},
      });
  }

  @Parameter(0)
  public String hostNames;

  @Parameter(1)
  public int firstPort;

  @Parameter(2)
  public int secondPort;

  @Test
  public void serverAddresses() {
    List<ServerAddress> expected = new ArrayList<>();
    expected.add(new ServerAddress("host1", firstPort));
    expected.add(new ServerAddress("host2", secondPort));

    assertThat(MongoSink.parseHostnames(hostNames), is(equalTo(expected)));
  }
}
