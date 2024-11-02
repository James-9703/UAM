package serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import streamProcessor.appRecord;

public class AppSerde<T>  {

public static Serde<appRecord> customSerde(){
  GsonSerializer<appRecord> serializer = new GsonSerializer<>();
  GsonDeserializer <appRecord>  deserializer = new GsonDeserializer<>();
  return Serdes.serdeFrom (serializer, deserializer);
}

}