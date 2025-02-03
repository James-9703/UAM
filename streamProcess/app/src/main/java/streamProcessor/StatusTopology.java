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
import org.apache.kafka.streams.kstream.KTable;

import serde.AppSerde;


public class StatusTopology {

    public static Topology build() throws IOException{
        Set<String> blacklist = new HashSet<>(
            Arrays.asList("Wireshark", "Kate", "forbidden", "prohibited")
        );
        String regex = String.join("|", blacklist);
        Pattern pattern = Pattern.compile(regex);

       /*  Counter eventCounter = Counter.builder().name("eventCounter").help("number of event process").register();
        
        HTTPServer server = HTTPServer.builder()
          .port(9400)
          .buildAndStart();*/


        StreamsBuilder builder = new StreamsBuilder();
        KTable<String, appRecord> input = builder.table("idle-time",Consumed.with(Serdes.String(), AppSerde.appRecordSerde()));
        KTable<String, appRecord> filtered = input.filter((key,appRecord)->{
            if (appRecord.getIdleTime()>10000){
                    appRecord.violation ="idle more than 10s";
                    //eventCounter.labelValues("GET","200").inc();
                return true;

            }
            
            ArrayList<String> openedapp = appRecord.getOpenedApp();
            
            for (String element : openedapp) {
                if (pattern.matcher(element).find()) {
                    appRecord.violation="using "+ appRecord.violation.concat(element);
                    //eventCounter.inc();
                    return true;
                }
            }

            if(!appRecord.getFirewall()){ 
            appRecord.violation = "firewall is off"; 
           
           // eventCounter.inc();
            return true;}

            if(appRecord.getPw() != 100){
                appRecord.violation = "password policy incorrect";
                return true;}
            return false;
        });
        filtered.toStream().to("statusV");

       /* ,
        Materialized.<String, appRecord, KeyValueStore<Bytes, byte[]>>as("StatusViolation").with(Serdes.String(),AppSerde.appRecordSerde()));*/
        //StreamsBuilder b = new StreamsBuilder();

        
        return builder.build();
    }
}
