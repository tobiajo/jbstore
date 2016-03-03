package se.kth.id2203.jbstore.system.network;

import se.sics.kompics.network.Transport;
import se.sics.test.TAddress;
import se.sics.test.TMessage;

import java.io.Serializable;

public class NetMsg extends TMessage {

    public static final byte VIEW_SYNC = 0;
    public static final byte JOIN = 1;
    public static final byte VIEW = 2;
    public static final byte VIEW_REQUEST = 3;

    public static final byte KV_STORE = 10;
    public static final byte GET = 11;
    public static final byte PUT = 12;
    public static final byte GET_RESPONSE = 13;
    public static final byte PUT_RESPONSE = 14;
    public static final byte READ = 15;
    public static final byte VALUE = 16;
    public static final byte WRITE = 17;
    public static final byte ACK = 18;

    public static final byte EPFD = 20;
    public static final byte HEARTBEAT = 21;
    public static final byte HEARTBEAT_ACK = 22;


    public final long rid;
    public final byte comp;
    public final byte cmd;
    public final Serializable body;

    public NetMsg(TAddress src, TAddress dst, long rid, byte comp, byte cmd, Serializable body) {
        super(src, dst, Transport.TCP);
        this.rid = rid;
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
            case VIEW_REQUEST:
                return "VIEW_REQUEST";
            case GET:
                return "GET";
            case PUT:
                return "PUT";
            case GET_RESPONSE:
                return "GET_RESPONSE";
            case PUT_RESPONSE:
                return "PUT_RESPONSE";
            case READ:
                return "READ";
            case VALUE:
                return "VALUE";
            case WRITE:
                return "WRITE";
            case ACK:
                return "ACK";
            case HEARTBEAT:
                return "HEARTBEAT";
            case HEARTBEAT_ACK:
                return "HEARTBEAT_ACK";
            default:
                throw new RuntimeException("Unexpected type-byte in NetMsg");
        }
    }

    @Override
    public String toString() {
        return "{" + getSource() + ", " + getDestination() + ", " + rid + ", " + cmdString() + ", " + body + "}";
    }
}
