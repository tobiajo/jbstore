package se.kth.id2203.jbstore.system.membership.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

public class ViewSyncInit implements KompicsEvent {

    public final int n;

    public ViewSyncInit(int n) {
        this.n = n;
    }
}
