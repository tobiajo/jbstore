package se.kth.id2203.jbstore.network;

import se.sics.kompics.network.Transport;
import se.sics.test.TAddress;
import se.sics.test.TMessage;

import java.io.Serializable;

public class Msg extends TMessage {

    public static final byte JOIN = 0;
    public static final byte VIEW = 1;

    public final long time;
    public final byte desc;
    public final Serializable body;

    public Msg(TAddress src, TAddress dst, long time, byte desc, Serializable body) {
        super(src, dst, Transport.TCP);
        this.time = time;
        this.desc = desc;
        this.body = body;
    }

    public String descString() {
        switch (desc) {
            case JOIN:
                return "JOIN";
            case VIEW:
                return "VIEW";
            default:
                return null;
        }
    }
}
