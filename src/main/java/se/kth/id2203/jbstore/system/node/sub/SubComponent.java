package se.kth.id2203.jbstore.system.node.sub;

import se.kth.id2203.jbstore.system.node.core.NodePort;
import se.kth.id2203.jbstore.system.node.core.event.NodeMsgBcast;
import se.kth.id2203.jbstore.system.node.core.event.NodeMsgSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.test.TAddress;

import java.io.Serializable;
import java.util.Set;

public abstract class SubComponent extends ComponentDefinition {

    private final Positive<NodePort> nodePortPositive = requires(NodePort.class);
    protected byte comp;

    protected void send(TAddress dst, long rid, byte cmd, int inst, Serializable body) {
        trigger(new NodeMsgSend(dst, rid, comp, cmd, inst, body), nodePortPositive);
    }

    protected void bcast(Set<TAddress> dstGroup, long rid, byte cmd, int inst, Serializable body) {
        trigger(new NodeMsgBcast(dstGroup, rid, comp, cmd, inst, body), nodePortPositive);
    }
}
