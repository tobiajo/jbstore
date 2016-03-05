package se.kth.id2203.jbstore.system.network.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.io.Serializable;

public class NodeMsgSend implements KompicsEvent {

    public final TAddress dst;
    public final long rid;
    public final byte comp;
    public final byte cmd;
    public final int inst;
    public final Serializable body;

    public NodeMsgSend(TAddress dst, long rid, byte comp, byte cmd, int inst, Serializable body) {
        this.dst = dst;
        this.rid = rid;
        this.comp = comp;
        this.cmd = cmd;
        this.inst = inst;
        this.body = body;
    }
}
