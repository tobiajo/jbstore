package se.kth.id2203.jbstore.system.membership.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.io.Serializable;

public class ViewSyncSend implements KompicsEvent {

    public final TAddress dst;
    public final byte desc;
    public final Serializable body;

    public ViewSyncSend(TAddress dst, byte desc, Serializable body) {
        this.dst = dst;
        this.desc = desc;
        this.body = body;
    }
}
