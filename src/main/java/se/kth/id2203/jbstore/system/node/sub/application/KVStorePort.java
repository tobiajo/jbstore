package se.kth.id2203.jbstore.system.node.sub.application;

import se.kth.id2203.jbstore.system.node.sub.application.event.*;
import se.kth.id2203.jbstore.system.node.core.event.NodeMsg;
import se.sics.kompics.PortType;

public class KVStorePort extends PortType {

    {
        indication(NodeMsg.class);
        indication(KVStoreInit.class);
    }
}
