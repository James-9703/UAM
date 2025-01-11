package streamProcessor;

import serde.AppSerde;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.Topology;

public class StatusTopology {

    public static Topology build(){
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
        KTable<String, appRecord> input = builder.table("idle-time",Consumed.with(Serdes.String(), AppSerde.appRecordSerde()));
        KTable<String, appRecord> filtered = input.filter((key,appRecord)->{
            if (appRecord.getIdleTime()>10000){
                    appRecord.violation ="idle more than 10s";
                return true;

            }
            
            ArrayList<String> openedapp = appRecord.getOpenedApp();
            
            for (String element : openedapp) {
                if (pattern.matcher(element).find()) {
                    appRecord.violation="using "+ appRecord.violation.concat(element);
                    return true;
                }
            }

            if(!appRecord.getFirewall()){ appRecord.violation = "firewall is off"; return true;}

            if(appRecord.getDisk()){return true;}
            return false;
        });
        filtered.toStream().to("statusV");

       /* ,
        Materialized.<String, appRecord, KeyValueStore<Bytes, byte[]>>as("StatusViolation").with(Serdes.String(),AppSerde.appRecordSerde()));*/
        //StreamsBuilder b = new StreamsBuilder();

        
        return builder.build();
    }
}
