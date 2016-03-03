package se.kth.id2203.jbstore.system.failuredetector;

import se.kth.id2203.jbstore.system.failuredetector.event.EPFDInit;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDRestore;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDSuspect;
import se.kth.id2203.jbstore.system.network.NetMsg;
import se.sics.kompics.PortType;

public class EPFDPort extends PortType {
    public EPFDPort() {
        indication(EPFDSuspect.class);
        indication(EPFDRestore.class);
        indication(NetMsg.class);
        request(EPFDInit.class);
        request(NetMsg.class);
    }
}
