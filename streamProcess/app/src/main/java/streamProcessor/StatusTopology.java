package streamProcessor;

import serde.AppSerde;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.Topology;
import com.google.common.primitives.Bytes;
import org.apache.kafka.streams.kstream.Materialized;

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
        KTable<String, appRecord> input = builder.table("current-app",Consumed.with(Serdes.String(), AppSerde.appRecordSerde()));
        KTable<String, appRecord> fitered = input.filter((key,appRecord)->{
            if (appRecord.getIdleTime()>10){
                return true;

            }
            
            ArrayList<String> openedapp = appRecord.getOpenedApp();
            
            for (String element : openedapp) {
                if (pattern.matcher(element).find()) {
                    System.out.println("found = "+element+"returning true");
                    return true;
                }
            }

            if(appRecord.getFirewall()){ return true;}
            if(appRecord.getDisk()){return true;}

            return false;
            }
        , Materialized.<String, appRecord, KeyValueStore<Bytes, byte[]>>as("StatusViolation").with(Serdes.String(), AppSerde.appRecordSerde()));

        return builder.build();
    }
}
