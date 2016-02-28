package se.kth.id2203.jbstore.system.application.event;

import se.sics.kompics.KompicsEvent;

public class KVStoreAck implements KompicsEvent {
    public final long rid;

    public KVStoreAck(long rid) {
        this.rid = rid;
    }
}
