package se.kth.id2203.jbstore.system.membership.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

public class ViewSyncGetView implements KompicsEvent {

    public final TAddress src;

    public ViewSyncGetView(TAddress src) {
        this.src = src;
    }
}
