package Util.RicartAgrawala.Messages;

abstract public class Message implements MutualExcludable {

    protected int PID;

    protected long timestamp;

    public Message(int pid, long timestamp)
    {
        this.PID = pid;

        this.timestamp = timestamp;
    }


}
