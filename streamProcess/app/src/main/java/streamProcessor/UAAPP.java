package streamProcessor;

//import java.sql.SQLException;
import java.util.*;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
//import org.apache.kafka.streams.ConsumerConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.HostInfo;
public class UAAPP {

    public static void main(String[] args) throws Exception {

        String host = System.getProperty("host","192.168.1.169");
        Integer port = Integer.parseInt(System.getProperty("port","8080"));
        String endpoint = String.format("%s:%s", host, port);
        System.out.println(endpoint+"end point is here..................................");
        HostInfo hostInfo = new HostInfo(host, port);
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev1");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "broker-1:19092,broker-2:19092,broker-3:19092");
       // config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass());
      //  config.put(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);


        Topology statustopology = StatusTopology.build();

        
        KafkaStreams statusStream = new KafkaStreams(statustopology, config);
        Runtime.getRuntime().addShutdownHook(new Thread(statusStream::close));
        statusStream.start();

        /*Topology sudoTopology = SudoTopology.build();
        KafkaStreams sudoStream = new KafkaStreams(sudoTopology,config);
        Runtime.getRuntime().addShutdownHook(new Thread(sudoStream::close));
        sudoStream.start();*/
        /* StreamsBuilder builder = new StreamsBuilder();
        KTable<String, appRecord> input = builder.table("statusV",Consumed.with(Serdes.String(), AppSerde.appRecordSerde()),Materialized.as("StatusViolation"));
        KafkaStreams resultStreams = new KafkaStreams(builder.build(), config);
        resultStreams.start(); */

    
       // StatusService statusDashboard = new StatusService(hostInfo, processstream);
       // statusDashboard.start();
        
    }
}
