package Util.RicartAgrawala.Messages;


public class RequestMessage extends Message {

    private String criticalRegion;

    public RequestMessage(int PID, int timestamp, String criticalRegion){
        super(PID,timestamp);
    }

    @Override
    public String request() {
        return Integer.toString(PID) + ";" + Long.toString(this.timestamp) + ";" + this.criticalRegion;
    }

    @Override
    public String ok() {
        return "";
    }
}
