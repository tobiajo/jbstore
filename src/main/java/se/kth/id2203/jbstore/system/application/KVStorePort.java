package se.kth.id2203.jbstore.system.application;

import se.kth.id2203.jbstore.system.application.event.*;
import se.kth.id2203.jbstore.system.network.NetMsg;
import se.sics.kompics.PortType;

public class KVStorePort extends PortType {
    public KVStorePort() {
        indication(KVStoreInit.class);
        indication(NetMsg.class);
        request(NetMsg.class);
    }
}
