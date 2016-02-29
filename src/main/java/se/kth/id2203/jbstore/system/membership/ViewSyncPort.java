package se.kth.id2203.jbstore.system.membership;

import se.kth.id2203.jbstore.system.membership.event.*;
import se.kth.id2203.jbstore.system.network.NetMsg;
import se.sics.kompics.PortType;

public class ViewSyncPort extends PortType {
    public ViewSyncPort() {
        indication(ViewSyncInit.class);
        indication(NetMsg.class);
        request(NetMsg.class);
    }
}
