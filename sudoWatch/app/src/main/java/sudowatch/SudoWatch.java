package sudowatch;
import java.io.IOException;
import java.nio.file.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;
import java.util.Map;

public class SudoWatch {

    public static void main(String[] args) throws IOException, InterruptedException {
        final String topic = "privilege_command";
        //server config
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
        String filePath = "/tmp/sudo"; // Replace with the actual file path

        // Create a WatchService and register the file's directory
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(filePath).getParent();
        path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        while (true) {
            // Wait for an event
            WatchKey key = watchService.take();

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // Check if the event is of type ENTRY_MODIFY
                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    // Get the file name
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    // Check if the modified file is the target file
                    if (fileName.toString().equals(filePath.substring(filePath.lastIndexOf("/") + 1))) {
                        try {
                                ProcessBuilder processBuilder = new ProcessBuilder(
                                    "bash",
                                    "-c",
                                    "tail --lines=2 /tmp/sudo "
                                );
                                Process process = processBuilder.start();

                                BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(process.getInputStream())
                                );
                                String line;

                                StringBuilder sb = new StringBuilder();
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line).append("\n"); 
                                }
                                try (var producer = new KafkaProducer<String, String>(config)) {
                                            
                                        final Callback callback = (metadata, exception) -> {
                                                    System.out.format(
                                                        "Published with metadata: %s, error: %s%n",
                                                        metadata,
                                                        exception
                                                    );
                                                };
                                            
                                                // publish the record, handling the metadata in the callback
                                                producer.send(
                                                    new ProducerRecord<>(topic,sb.toString()),
                                                    callback
                                                );
                                                 System.out.println("File " + filePath + " has been modified, content = " + sb.toString());
                                }
                        
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            // Reset the key to receive further events
            boolean valid = key.reset();
            if (!valid) {
                break;
            }

        }
    }
}
}