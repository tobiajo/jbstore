package se.kth.id2203.jbstore.system.node.sub.membership;

import se.kth.id2203.jbstore.system.node.sub.membership.event.*;
import se.kth.id2203.jbstore.system.node.core.event.NodeMsg;
import se.sics.kompics.PortType;

public class ViewSyncPort extends PortType {

    {
        indication(NodeMsg.class);
        indication(ViewSyncInit.class);

    }
}
