package se.kth.id2203.jbstore.system.membership.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

public class ViewSyncInit implements KompicsEvent {

    public final TAddress self;
    public final TAddress member;
    public final int selfId;
    public final int n;

    public ViewSyncInit(TAddress self, TAddress member, int selfId, int n) {
        this.self = self;
        this.member = member;
        this.selfId = selfId;
        this.n = n;
    }
}
