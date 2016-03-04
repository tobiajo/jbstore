package se.kth.id2203.jbstore.system.network.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.io.Serializable;

public class NodeMsgSend implements KompicsEvent {

    public final TAddress dst;
    public final byte comp;
    public final byte cmd;
    public final long rid;
    public final Serializable body;

    public NodeMsgSend(TAddress dst, byte comp, byte cmd, long rid, Serializable body) {
        this.dst = dst;
        this.comp = comp;
        this.cmd = cmd;
        this.rid = rid;
        this.body = body;
    }
}
