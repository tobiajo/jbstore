package se.kth.id2203.jbstore.system.node.core.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.io.Serializable;
import java.util.Set;

public class NodeMsgBcast implements KompicsEvent {

    public final Set<TAddress> dstGroup;
    public final long rid;
    public final byte comp;
    public final byte cmd;
    public final int inst;
    public final Serializable body;

    public NodeMsgBcast(Set<TAddress> dstGroup, long rid, byte comp, byte cmd, int inst, Serializable body) {
        this.dstGroup = dstGroup;
        this.rid = rid;
        this.comp = comp;
        this.cmd = cmd;
        this.inst = inst;
        this.body = body;
    }
}
