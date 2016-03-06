package se.kth.id2203.jbstore.system.node.core;

import se.kth.id2203.jbstore.system.node.core.event.NodeMsgBcast;
import se.kth.id2203.jbstore.system.node.core.event.NodeMsgSend;
import se.sics.kompics.PortType;

public class NodePort extends PortType {

    {
        request(NodeMsgSend.class);
        request(NodeMsgBcast.class);
    }
}
