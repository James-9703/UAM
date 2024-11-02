package producer;

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
        final String topic = "current-app";

        final Map<String, Object> config = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "192.168.1.19:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName(),
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName(),
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
            true
        );

        StringBuilder userName = new StringBuilder();
        StringBuilder output = new StringBuilder();
        String command =
            "wmctrl -l | awk '{for(i=4;i<=NF;i++) printf \"%s \", $i; print \"\"}'";
        try {
            Process process = Runtime.getRuntime().exec("whoami");

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                userName.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (var producer = new KafkaProducer<String, String>(config)) {
            while (true) {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(
                        "bash",
                        "-c",
                        command
                    );
                    Process process = processBuilder.start();

                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                    );
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line + "\n");
                    }
                    int exitCode = process.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final var key = userName.toString();
                final var value = output.toString();

                final Callback callback = (metadata, exception) -> {
                    out.format(
                        "Published with metadata: %s, error: %s%n",
                        metadata,
                        exception
                    );
                };

                // publish the record, handling the metadata in the callback
                producer.send(
                    new ProducerRecord<>(topic, key, value),
                    callback
                );
                output = new StringBuilder();
                // wait a second before publishing another
                Thread.sleep(1000);
            }
        }
    }
}
