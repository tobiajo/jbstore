package se.kth.id2203.jbstore.system.application.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

public class KVStoreRead implements KompicsEvent {

    public final String key;
    public final TAddress src;

    public KVStoreRead(String key, TAddress src) {
        this.key = key;
        this.src = src;
    }
}
