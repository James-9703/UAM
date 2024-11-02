package streamProcessor;
import java.util.ArrayList;


public class appRecord {
    private String userName;
    private String ip;
    private ArrayList openedApp;
    
    
    public appRecord (String userName,String ip) {
        this.userName = userName;
        this.ip = ip;
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

    public void setOpenedApp(ArrayList appliist){
        this.openedApp = appliist;
    }
}
