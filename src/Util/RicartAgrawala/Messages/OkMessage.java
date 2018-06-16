package Util.RicartAgrawala.Messages;


public class OkMessage extends Message {

    public OkMessage(int PID, int timestamp){
        super(PID,timestamp);
    }

    @Override
    public String request() {
        return "";
    }

    @Override
    public String ok() {
        return Integer.toString(this.PID) + ";" + Long.toString(this.timestamp);
    }

}
