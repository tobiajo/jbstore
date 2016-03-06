package se.kth.id2203.jbstore.system.node.sub.failuredetector;

import se.kth.id2203.jbstore.system.node.sub.failuredetector.event.EPFDInit;
import se.kth.id2203.jbstore.system.node.sub.failuredetector.event.EPFDRestore;
import se.kth.id2203.jbstore.system.node.sub.failuredetector.event.EPFDSuspect;
import se.kth.id2203.jbstore.system.node.core.event.NodeMsg;
import se.sics.kompics.PortType;

public class EPFDPort extends PortType {

    {
        indication(NodeMsg.class);
        indication(EPFDInit.class);
        request(EPFDSuspect.class);
        request(EPFDRestore.class);
    }
}
