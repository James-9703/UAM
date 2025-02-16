package streamProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import com.google.gson.Gson;

import serde.AppSerde;


public class StatusTopology {

    public static Topology build() {
        Set<String> blacklist = new HashSet<>(
            Arrays.asList("Wireshark", "Kate", "forbidden", "prohibited")
        );
        String regex = String.join("|", blacklist);
        Pattern pattern = Pattern.compile(regex);

       /*  Counter eventCounter = Counter.builder().name("eventCounter").help("number of event process").register();
        
        HTTPServer server = HTTPServer.builder()
          .port(9400)
          .buildAndStart();*/

        Gson gson = new Gson();
        StreamsBuilder builder = new StreamsBuilder();
        KTable<String, String> input = builder.table("idle-time",Consumed.with(Serdes.String(), Serdes.String()));
        KTable<String, appRecord>  recordtTable = input.mapValues(json -> gson.fromJson(json, appRecord.class));

        KTable<String, appRecord> filtered = recordtTable.filter((key,appRecord)->{
            boolean filter = false;
            StringBuilder log = new StringBuilder();
            if (appRecord.getIdleTime()>10000){
                log.append("idle more than 10s; ");
                    
                    //eventCounter.labelValues("GET","200").inc();
                filter = true;

            }
            
            ArrayList<String> openedapp = appRecord.getOpenedApp();
            
            for (String element : openedapp) {
                if (pattern.matcher(element).find()) {
                    log.append(" using "+ element);
                    //eventCounter.inc();
                    filter = true;
                }
            }

            if(!appRecord.getFirewall()){ 
                log.append(" firewall is off; ");
           
           // eventCounter.inc();
            filter = true;}

            if(appRecord.getPw() != 100){
                log.append("password policy incorrect");
                filter = true;}
            appRecord.violation = log.toString();
            return filter;
        });

        KStream<String, String> outputStream = filtered
            .toStream()
            .mapValues(record -> gson.toJson(record));

        outputStream.to("statusV");

       /* ,
        Materialized.<String, appRecord, KeyValueStore<Bytes, byte[]>>as("StatusViolation").with(Serdes.String(),AppSerde.appRecordSerde()));*/
        //StreamsBuilder b = new StreamsBuilder();
        
        
        return builder.build();
    }
}
