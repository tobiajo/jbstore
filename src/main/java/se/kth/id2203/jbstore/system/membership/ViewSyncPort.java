package se.kth.id2203.jbstore.system.membership;

import se.kth.id2203.jbstore.system.membership.event.*;
import se.sics.kompics.PortType;

public class ViewSyncPort extends PortType {
    public ViewSyncPort() {
        indication(ViewSyncGetView.class);
        indication(ViewSyncInit.class);
        indication(ViewSyncJoin.class);
        indication(ViewSyncView.class);
        request(ViewSyncSend.class);
    }
}
