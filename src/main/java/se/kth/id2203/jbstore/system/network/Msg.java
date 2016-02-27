package se.kth.id2203.jbstore.system.network;

import se.sics.kompics.network.Transport;
import se.sics.test.TAddress;
import se.sics.test.TMessage;

import java.io.Serializable;

import org.slf4j.Logger;

public class Msg extends TMessage {

    public static final byte JOIN = 0;
    public static final byte VIEW = 1;
    public static final byte GET = 2;
    public static final byte PUT = 3;
    public static final byte VALUE = 4;
    public static final byte GET_VIEW = 5;

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
            case GET:
                return "GET";
            case PUT:
                return "PUT";
            case VALUE:
                return "VALUE";
            case GET_VIEW:
                return "GET_VIEW";
            default:
                return null;
        }
    }

    public void log(Logger logger, String event) {
        switch (event) {
            case "Sent":
                logger.info("Sent({}) to {}:\t{}, {}", time, header.dst, descString(), body);
                break;
            case "Rcvd":
                logger.info("Rcvd({}) fr {}:\t{}, {}", time, header.src, descString(), body);
                break;
        }
    }
}
