package sudoStream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import java.sql.*;

public class SudoTopology {
    public static Topology build() throws SQLException {

        final String jdbc_url = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=password";
        Connection conn = DriverManager.getConnection(jdbc_url);

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> input = builder.stream("privilege_command",
                Consumed.with(Serdes.String(), Serdes.String()));

        input.foreach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                
                try {
                    PreparedStatement st = conn.prepareStatement("INSERT INTO sudo (log) VALUES (?)");
                    st.setString(1, value);
                     int rowsDeleted = st.executeUpdate();
                     st.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
        });

        return builder.build();
    }

}
