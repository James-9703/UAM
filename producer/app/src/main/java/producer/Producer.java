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
        final String topic = "current-app";
        //server config
        final Map<String, Object> config = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "192.168.1.19:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName(),
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            GsonSerializer.class.getName(),
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
            true
        );
        
        //get username
        StringBuilder userName = new StringBuilder();
            try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                        "bash",
                        "-c",
                        "whoami"
                    );
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                userName.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        // get ip address
        StringBuilder ip = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                        "bash",
                        "-c",
                        "hostname -I | awk '{print $1}'"
                    );
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                ip.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get running app and produce record
        String command =
            "wmctrl -l | awk '{for(i=4;i<=NF;i++) printf \"%s \", $i; print \"\"}'";
        // "xprop -root _NET_ACTIVE_WINDOW | cut -d ' ' -f 5 | xargs -I {} xprop -id {} WM_NAME";

        appRecord record = new appRecord(userName.toString(),ip.toString());
        ArrayList openedApp = new ArrayList<String>();
        try (var producer = new KafkaProducer<String, appRecord>(config)) {
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
                        openedApp.add(line);
                    }
                    
                    int exitCode = process.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final Callback callback = (metadata, exception) -> {
                    out.format(
                        "Published with metadata: %s, error: %s%n",
                        metadata,
                        exception
                    );
                };
                record.setOpenedApp(openedApp);
               
                // publish the record, handling the metadata in the callback
                producer.send(
                    new ProducerRecord<>(topic,record.getUserName(), record),
                    callback
                );
                openedApp.clear();
                // wait a second before publishing another
                Thread.sleep(1000);
            }
        }
    }
}
