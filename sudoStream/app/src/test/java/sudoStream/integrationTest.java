package sudoStream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sudoStream.SudoTopology;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class integrationTest {

    private static KafkaStreams kafkaStreams;
    private static KafkaProducer<String, String> producer;
    private static Connection dbConn;

    @BeforeAll
    static void setup() throws SQLException {
       


        // 2. Initialize Kafka Producer to send test data
        producer = createProducer();

    //     3. Initialize Database Connection for verification
        String jdbc_url = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=password"; // Use JDBC_URL from env if available, else default
        dbConn = DriverManager.getConnection(jdbc_url); 
    }

    @AfterAll
    static void teardown() throws SQLException {
        if (kafkaStreams != null) {
            kafkaStreams.close();
        }
        if (producer != null) {
            producer.close();
        }
        if (dbConn != null) {
            dbConn.close();
        }
    }

    @Test
    void testSudoCommandProcessing() throws ExecutionException, InterruptedException, SQLException {
        String testCommand = "sudo apt update";
        String testKey = "test-key";

        // 1. Produce a message to the input topic
        ProducerRecord<String, String> record = new ProducerRecord<>("privilege_command", testKey, testCommand);
        producer.send(record).get(); // Wait for send to complete



         //  Start Kafka Streams application (assuming it's packaged as app.jar and in the same directory when running tests)
         try {
            
            kafkaStreams = new KafkaStreams(SudoTopology.build(), createStreamsConfig()); // Build topology and create KafkaStreams
            kafkaStreams.start();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to start Kafka Streams application: " + e.getMessage());
        }

        // Wait for a short time to allow Kafka Streams to process the message
        try {
            Thread.sleep(3000); // Adjust wait time as needed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted during sleep: " + e.getMessage());
        }

        // 2. Verify database insertion
        String logFromDb = fetchLogFromDatabase();
        assertNotNull(logFromDb, "Log should be inserted into the database");
        assertEquals(testCommand, logFromDb, "Log content in database should match the command");

        // 3. Email Verification (Manual - Check your Gmail sent folder)
        System.out.println("Manually check your Gmail sent folder for the 'Privilege escalation alert' email.");
        // In a more advanced setup, you'd use a mock email server and verify programmatically
    }


    private static Properties createStreamsConfig() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:29092,localhost:39092,localhost:49092"); // Connect to your Docker Kafka broker
        props.put("application.id", "integration-test-app"); // Unique app ID for testing
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,StringSerializer.class.getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,StringSerializer.class.getName());
        return props;
    }

    private static KafkaProducer<String, String> createProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:29092,localhost:39092,localhost:49092"); // Connect to your Docker Kafka broker
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }


    private String fetchLogFromDatabase() throws SQLException {
        String sql = "SELECT log FROM sudo ORDER BY id DESC LIMIT 1"; // Get the latest log entry
        try (PreparedStatement pstmt = dbConn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("log");
            } else {
                return null; // No log found
            }
        }
    }
}