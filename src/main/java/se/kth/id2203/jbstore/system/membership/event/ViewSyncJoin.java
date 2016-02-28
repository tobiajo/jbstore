package se.kth.id2203.jbstore.system.membership.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

public class ViewSyncJoin implements KompicsEvent {

    public final int id;
    public final TAddress src;

    public ViewSyncJoin(int id, TAddress src) {
        this.id = id;
        this.src = src;
    }
}
