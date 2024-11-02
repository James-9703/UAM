/*
 * This source file was generated by the Gradle 'init' task
 */
package consumer;

import static java.lang.System.*;

import java.time.*;
import java.util.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.*;

public class Consumer {

    public static void main(String[] args) {
        final var topic = "current-app";

        final Map<String, Object> config = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "192.168.1.19:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class.getName(),
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class.getName(),
            ConsumerConfig.GROUP_ID_CONFIG,
            "basic-consumer-sample",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
            "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
            false
        );

        try (var consumer = new KafkaConsumer<String, String>(config)) {
            consumer.subscribe(Set.of(topic));

            while (true) {
                final var records = consumer.poll(Duration.ofMillis(100));
                for (var record : records) {
                    System.out.println("record =" + record);
                }
                consumer.commitAsync();
            }
        }
    }
}
