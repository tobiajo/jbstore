package se.kth.id2203.jbstore.system.network;

import se.sics.kompics.network.Transport;
import se.sics.test.TAddress;
import se.sics.test.TMessage;

import java.io.Serializable;

public class NetMsg extends TMessage {

    public static final byte VIEW_SYNC = 0;
    public static final byte JOIN = 1;
    public static final byte VIEW = 2;
    public static final byte GET_VIEW = 3;

    public static final byte KV_STORE = 10;
    public static final byte GET = 11;
    public static final byte READ = 12;
    public static final byte VALUE = 13;
    public static final byte PUT = 14;
    public static final byte WRITE = 15;
    public static final byte ACK = 16;

    public final long time;
    public final byte comp;
    public final byte cmd;
    public final Serializable body;

    public NetMsg(TAddress src, TAddress dst, long time, byte comp, byte cmd, Serializable body) {
        super(src, dst, Transport.TCP);
        this.time = time;
        this.comp = comp;
        this.cmd = cmd;
        this.body = body;
    }

    public String cmdString() {
        switch (cmd) {
            case JOIN:
                return "JOIN";
            case VIEW:
                return "VIEW";
            case GET_VIEW:
                return "GET_VIEW";
            case GET:
                return "GET";
            case READ:
                return "READ";
            case VALUE:
                return "VALUE";
            case PUT:
                return "PUT";
            case WRITE:
                return "WRITE";
            case ACK:
                return "ACK";
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "{" + getSource() + ", " + getDestination() + ", " + time + ", " + cmdString() + ", " + body + "}";
    }
}
