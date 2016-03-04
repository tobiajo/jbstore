package se.kth.id2203.jbstore.system.membership;

import se.kth.id2203.jbstore.system.membership.event.*;
import se.kth.id2203.jbstore.system.network.event.NodeMsg;
import se.sics.kompics.PortType;

public class ViewSyncPort extends PortType {

    {
        indication(NodeMsg.class);
        indication(ViewSyncInit.class);

    }
}
