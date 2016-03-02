package se.kth.id2203.jbstore.system.failuredetector;

import se.kth.id2203.jbstore.system.network.NetMsg;
import se.sics.kompics.PortType;

public class FDPort extends PortType {
    public FDPort() {
        indication(NetMsg.class);
        request(NetMsg.class);
    }
}
