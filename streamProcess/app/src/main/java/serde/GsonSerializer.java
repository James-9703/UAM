
package serde;
import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

public class GsonSerializer<V> implements Serializer<V> {
    private final Gson gson = new Gson();

    @Override
    public byte[] serialize(String topic, V data) {
        if (data == null) return null;
        return gson.toJson(data).getBytes(StandardCharsets.UTF_8);
    }
}
