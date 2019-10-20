package art.iculate.flume;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Projections.excludeId;

public class MongoUtil {

  /**
   * Retrieve all JSON objects (as <code>String</code>s from the given MongoDB host.
   */
  public static List<String> getJsonDocuments(String host, int port, String user, String password, String database, String collection) {
    MongoClient client = MongoClients.create(
            MongoClientSettings.builder()
                    .applyToClusterSettings(builder ->
                            builder.hosts(Arrays.asList(new ServerAddress(host, port))))
                    .credential(MongoCredential.createCredential(user, database, password.toCharArray()))
                    .build());

    List<String> results = new ArrayList<>();
    for (Document cur: client.getDatabase(database).getCollection(collection).find().projection(excludeId())) {
      results.add(cur.toJson());
    }
    return results;
  }
}
