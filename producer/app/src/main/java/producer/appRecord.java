package producer;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class appRecord {
    private String userName;
    private String ip;
    private ArrayList <String> openedApp = new ArrayList<>();
    private int idleTime;
    private boolean firewall;
    private boolean  disk;
    //0 - unauthorized app, 1 - idletime, 2 - firewall, 3 - encryption
    public String violation = "";    
    
    public appRecord () {
       this.setProfile();
    }
    
    public String  getUserName(){
        return userName;
    }

    public String getIp(){
        return ip;
    }

    public ArrayList getOpenedApp(){
        return openedApp;
    }

    public int getIdleTime(){
        return idleTime;
    }

    public boolean getFirewall(){
        return firewall;
    }

    
    public void setFirewall(){

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "bash",
                "-c",
                "if systemctl status ufw | grep -q \"Active: active\"; then\n" + //
                                        "    echo \"true\"\n" + //
                                        "else\n" + //
                                        "    echo \"false\"\n" + //
                                        "fi\n" 
            );
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
           String line;
           line = reader.readLine();
           firewall = Boolean.parseBoolean(line);
        
            
            int exitCode = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setEncrypt(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "bash",
                "-c",
                "if sudo blkid -s TYPE | grep -vq \"LUKS\"; then\n" + //
                                        "    echo \"true\"\n" + //
                                        "else\n" + //
                                        "    echo \"false\"\n" + //
                                        "fi\n"
            );
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String line;
            line = reader.readLine();
            disk = Boolean.parseBoolean(line);

            int exitCode = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }


    public void setOpenedApp(){
            openedApp.clear();
        

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "bash",
                "-c",
                "wmctrl -l | awk '{for(i=4;i<=NF;i++) printf \"%s \", $i; print \"\"}'"
            );
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                openedApp.add(line);
            }
            
            int exitCode = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setIdleTime(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "bash",
                "-c",
                "xprintidle"
            );
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String line;
            line = reader.readLine();
            idleTime = Integer.parseInt(line);
             
    
            int exitCode = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
    public void setProfile(){
        //get username
        StringBuilder userName = new StringBuilder();
            try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                        "bash",
                        "-c",
                        "whoami"
                    );
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                userName.append(line);
            }
            this.userName = userName.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    


    // get ip address
        StringBuilder ip = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                        "bash",
                        "-c",
                        "hostname -I | awk '{print $1}'"
                    );
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                ip.append(line);
            }
            this.ip = ip.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
