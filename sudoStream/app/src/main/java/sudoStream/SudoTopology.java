package sudoStream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

import java.io.IOException;
import java.sql.*;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class SudoTopology {
    public static Topology build() throws SQLException, MessagingException, IOException {

        final String jdbc_url = "jdbc:postgresql://db:5432/postgres?user=postgres&password=password";
        Connection conn = DriverManager.getConnection(jdbc_url);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS sudo (id SERIAL PRIMARY KEY, log TEXT)");
            System.out.println("Table 'sudo' created (if it didn't exist).");
        }

        final String username = "jameskoh970323@gmail.com";
        final String password = "wdmpkozvipivgwba";

        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
        Session session = Session.getInstance(prop,
                new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

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
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress("jameskoh970323@gmail.com"));
                    message.setRecipients(
                            Message.RecipientType.TO,
                            InternetAddress.parse("0135323@student.uow.edu.my")
                    );
                    message.setSubject("Privilege escalation alert");
                    message.setText("Sudo command is used, refer to postgres for more info");

                    Transport.send(message);

                    System.out.println("Done");
                    //String currentDirectory = System.getProperty("user.dir");
                    //System.out.println("Current Directory: " + currentDirectory);
                } catch (SQLException| MessagingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        return builder.build();
    }

}
