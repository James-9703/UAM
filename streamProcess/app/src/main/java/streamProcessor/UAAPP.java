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
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.HostInfo;
import com.google.common.primitives.Bytes;

import org.apache.kafka.streams.kstream.Materialized;
public class UAAPP {

    public static void main(String[] args) throws InterruptedException {

        String host = System.getProperty("host");
        Integer port = Integer.parseInt(System.getProperty("port"));
        String endpoint = String.format("%s:%s", host, port);
        System.out.println(endpoint);
        HostInfo hostInfo = new HostInfo(host, port);
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev1");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.19:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,serde.AppSerde.class);
        config.put(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);

        Topology topology = StatusTopology.build();

        
        KafkaStreams stream = new KafkaStreams(topology, config);
        stream.start();
        StatusService statusDashboard = new StatusService(hostInfo, stream);
        statusDashboard.start();
        Runtime.getRuntime().addShutdownHook(new Thread(stream::close));
    }
}
