package se.kth.id2203.jbstore.system.network.event;

import se.sics.kompics.network.Transport;
import se.sics.test.TAddress;
import se.sics.test.TMessage;

import java.io.Serializable;

public class NodeMsg extends TMessage {

    public static final byte VIEW_SYNC = 0;
    public static final byte JOIN = 1;
    public static final byte VIEW = 2;
    public static final byte VIEW_REQUEST = 3;

    public static final byte EPFD = 10;
    public static final byte HEARTBEAT_REQUEST = 11;
    public static final byte HEARTBEAT_REPLY = 12;

    public static final byte KV_STORE = 20;
    public static final byte GET = 21;
    public static final byte PUT = 22;
    public static final byte GET_RESPONSE = 23;
    public static final byte PUT_RESPONSE = 24;
    public static final byte READ = 25;
    public static final byte VALUE = 26;
    public static final byte WRITE = 27;
    public static final byte ACK = 28;

    public final long rid;
    public final byte comp;
    public final byte cmd;
    public final int inst;
    public final Serializable body;

    public NodeMsg(TAddress src, TAddress dst, long rid, byte comp, byte cmd, int inst, Serializable body) {
        super(src, dst, Transport.TCP);
        this.rid = rid;
        this.comp = comp;
        this.cmd = cmd;
        this.inst = inst;
        this.body = body;
    }

    public String getCompString() {
        switch (comp) {
            case VIEW_SYNC:
                return "VIEW_SYNC";
            case EPFD:
                return "EPFD";
            case KV_STORE:
                return "KV_STORE";
            default:
                return null;
        }
    }

    public String getCmdString() {
        switch (cmd) {
            case JOIN:
                return "JOIN";
            case VIEW:
                return "VIEW";
            case VIEW_REQUEST:
                return "VIEW_REQUEST";
            case HEARTBEAT_REQUEST:
                return "HEARTBEAT_REQUEST";
            case HEARTBEAT_REPLY:
                return "HEARTBEAT_REPLY";
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
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "{" + getSource() + ", " + getDestination() + ", " + rid+ ", " + getCompString() + ", " +getCmdString() + ", " + inst + ", " +  body + "}";
    }
}
