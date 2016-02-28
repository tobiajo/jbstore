package se.kth.id2203.jbstore.system.application.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.io.Serializable;

public class KVStoreWrite implements KompicsEvent {
    public final String key;
    public final TAddress src;
    public final Serializable value;

    public KVStoreWrite(String key, TAddress src, Serializable value) {
        this.key = key;
        this.src = src;
        this.value = value;
    }
}
