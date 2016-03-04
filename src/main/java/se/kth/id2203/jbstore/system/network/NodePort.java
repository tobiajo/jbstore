package se.kth.id2203.jbstore.system.network;

import se.kth.id2203.jbstore.system.network.event.NodeMsgSend;
import se.sics.kompics.PortType;

public class NodePort extends PortType {

    {
        request(NodeMsgSend.class);
    }
}
