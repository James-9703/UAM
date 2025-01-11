package sudoStream;
import java.sql.SQLException;
import java.util.*;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.HostInfo;

public class SudoStream {
    public static void main(String[] args) throws Exception {

        String host = System.getProperty("host","192.168.1.169");
        Integer port = Integer.parseInt(System.getProperty("port","8080"));
        String endpoint = String.format("%s:%s", host, port);
        System.out.println(endpoint+"end point is here..................................");
        HostInfo hostInfo = new HostInfo(host, port);
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev2");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.19:9092");
       // config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass());
        config.put(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);
        
        
        Topology sudoTopology = SudoTopology.build();
        KafkaStreams sudoStream = new KafkaStreams(sudoTopology,config);
        Runtime.getRuntime().addShutdownHook(new Thread(sudoStream::close));
        sudoStream.start();
    }
}