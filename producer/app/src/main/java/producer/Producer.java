package producer;

import serde.GsonSerializer;
import static java.lang.System.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;

public class Producer {

    public static void main(String[] args) throws InterruptedException {
        final String topic = "idle-time";
        //server config
        final Map<String, Object> config = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:29092,localhost:39092,localhost:49092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName(),
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            GsonSerializer.class.getName(),
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
            true
        );
 
        //get running app and produce record
                // "xprop -root _NET_ACTIVE_WINDOW | cut -d ' ' -f 5 | xargs -I {} xprop -id {} WM_NAME";

        appRecord record = new appRecord();
        try (var producer = new KafkaProducer<String, appRecord>(config)) {
            while (true) {
                record.setFirewall();;
                record.setIdleTime();
                record.setEncrypt();
                record.setOpenedApp();

                final Callback callback = (metadata, exception) -> {
                    out.format(
                        "Published with metadata: %s, error: %s%n",
                        metadata,
                        exception
                    );
                };
               
                // publish the record, handling the metadata in the callback
                producer.send(
                    new ProducerRecord<>(topic,record.getUserName(), record),
                    callback
                );
                // wait a second before publishing another
                Thread.sleep(3000);
            }
        }
    }
}
