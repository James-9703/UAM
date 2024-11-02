package streamProcessor;

import static java.lang.System.*;
import serde.AppSerde;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

public class UAAPP {

    public static void main(String[] args) throws InterruptedException {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev1");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.19:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
         config.put(
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
            Serdes.String().getClass()
        );
        config.put(
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
            serde.AppSerde.class
        );


        Set<String> blacklist = new HashSet<>(
            Arrays.asList("Wireshark", "Kate", "forbidden", "prohibited")
        );
        String regex = String.join("|", blacklist);
        Pattern pattern = Pattern.compile(regex);

        /*
edit from kstream to ktable, this will be a stateful processing
use filter, alert email, then publish to another topic/sink subscribe by redis for reporting.

*/
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, appRecord> input = builder.stream("current-app",Consumed.with(Serdes.String(), AppSerde.customSerde()));
        KStream<String, appRecord> fitered = input.filter((key,arrayList)->{
            ArrayList<String> openedapp = arrayList.getOpenedApp();
            
            for (String element : openedapp) {
                if (pattern.matcher(element).find()) {
                    System.out.println("found = "+element+"returning true");
                    return true; // Return true if there's a match
                }
            }
            System.out.println("no matched returning false");
            return false;
            }
        );


        fitered.to("unAuthorize-app", Produced.with(Serdes.String(), AppSerde.customSerde()));

        KafkaStreams stream = new KafkaStreams(builder.build(), config);
        stream.start();
        Runtime.getRuntime().addShutdownHook(new Thread(stream::close));
    }
}
