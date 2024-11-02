package serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import producer.appRecord;

public class GsonSerde<T> implements Serde<T> {

  @Override
  public Serializer<T> serializer() {
    return new GsonSerializer<T>();
  }

  @Override
  public Deserializer<T> deserializer() {
    return new GsonDeserializer(appRecord.class);
  }
}