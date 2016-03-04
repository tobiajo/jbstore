package se.kth.id2203.jbstore.system.application;

import se.kth.id2203.jbstore.system.application.event.*;
import se.kth.id2203.jbstore.system.network.event.NodeMsg;
import se.sics.kompics.PortType;

public class KVStorePort extends PortType {

    {
        indication(NodeMsg.class);
        indication(KVStoreInit.class);
    }
}
