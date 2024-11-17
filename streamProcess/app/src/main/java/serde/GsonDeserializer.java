package serde;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import streamProcessor.appRecord;

import org.apache.kafka.common.serialization.Deserializer;
import java.nio.charset.StandardCharsets;

public class GsonDeserializer<T> implements Deserializer<T> {
 private Class<T> destinationClass;

  public GsonDeserializer(Class<T> destinationClass) {
    this.destinationClass = destinationClass;
  }

    public final Gson gson = new GsonBuilder().create();
    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;
        String json = new String(data, StandardCharsets.UTF_8);
        try {

            return gson.fromJson(json, destinationClass);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }
}
