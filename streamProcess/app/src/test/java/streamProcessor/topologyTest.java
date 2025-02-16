package streamProcessor;

import java.util.Properties;
import org.json.JSONObject;
import static org.assertj.core.api.Assertions.assertThat;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class topologyTest {
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    

    @BeforeEach
    void setup(){
        Topology statustopology = StatusTopology.build();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass());

        testDriver = new TopologyTestDriver(statustopology, props);


        inputTopic =
        testDriver.createInputTopic(
        "idle-time",
        Serdes.String().serializer(), Serdes.String().serializer());
        

       // GsonDeserializer <V> deserializer = new GsonDeserializer<appRecord>(appRecord.class);
        outputTopic =
        testDriver.createOutputTopic(
        "statusV",
        Serdes.String().deserializer(), Serdes.String().deserializer());

    
            
    }
   
    @AfterEach
    void teardown() {
    testDriver.close();
    }


    @Test
    void testTopology(){
        String dataPoint1 = "{\n" + //
                        "  \"userName\": \"jkoh\",\n" + //
                        "  \"ip\": \"192.168.1.169\",\n" + //
                        "  \"openedApp\": [\n" + //
                        "    \"Mastering Kafka Streams and ksqlDB — Okular \",\n" + //
                        "    \"Producer.java - producer - Visual Studio Code \",\n" + //
                        "    \"UAM — Dolphin \",\n" + //
                        "    \"Wireshark \"\n" + //
                        "  ],\n" + //
                        "  \"idleTime\": 10001,\n" + //
                        "  \"firewall\": false,\n" + //
                        "  \"pw\": 101,\n" + //
                        "  \"violation\": \"\"\n" + //
                        "}";


        inputTopic.pipeInput("jkoh",dataPoint1);
        assertThat(outputTopic.isEmpty()).isFalse();
        TestRecord<String, String> record = outputTopic.readRecord();
        JSONObject jsonObject = new JSONObject(record.getValue());
        String violation = jsonObject.getString("violation");
        System.out.println(violation);
        assertThat(violation).isEqualTo("idle more than 10s;  using Wireshark  firewall is off; password policy incorrect");


    }


}