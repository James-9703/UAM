package streamProcessor;

import serde.AppSerde;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Map;
import java.util.HashMap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsMetadata;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;

public class StatusService {
    private final KafkaStreams streams;
    private final HostInfo hostInfo;

    StatusService(HostInfo hostNip, KafkaStreams streams){
        this.streams = streams;
        this.hostInfo = hostNip;
    }


  ReadOnlyKeyValueStore<String, appRecord> getStore() {
    return streams.store(
        StoreQueryParameters.fromNameAndType(
            // state store name
            "StatusViolation",
            // state store type
            QueryableStoreTypes.keyValueStore()));
  }

   void start() {
    Javalin app = Javalin.create().start(hostInfo.port());

    app.get("/status", this::getAll);

   }


   void getAll(Context ctx) {
    Map<String, appRecord> user = new HashMap<>();

    KeyValueIterator<String, appRecord> range = getStore().all();
    while (range.hasNext()) {
      KeyValue<String, appRecord> next = range.next();
      String host = next.key;
      appRecord status = next.value;
      user.put(host, status);
    }
    //prevent memory leak
    range.close();
    // return a JSON response
    ctx.json(user);
  }


}