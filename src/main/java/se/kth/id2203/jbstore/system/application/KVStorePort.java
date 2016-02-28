package se.kth.id2203.jbstore.system.application;

import se.kth.id2203.jbstore.system.application.event.*;
import se.sics.kompics.PortType;

public class KVStorePort extends PortType {
    public KVStorePort() {
        indication(KVStoreInit.class);
        indication(KVStoreRead.class);
        indication(KVStoreWrite.class);
        request(KVStoreValue.class);
        request(KVStoreAck.class);
    }
}
