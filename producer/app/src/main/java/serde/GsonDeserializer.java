package serde;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.kafka.common.serialization.Deserializer;
import java.nio.charset.StandardCharsets;

public class GsonDeserializer<T> implements Deserializer<T> {
    private final Class<T> clazz;
    private final Gson gson = new Gson();

    public GsonDeserializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;
        String json = new String(data, StandardCharsets.UTF_8);
        try {
            return gson.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }
}
