package se.kth.id2203.jbstore.system.application.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.io.Serializable;

public class KVStoreValue implements KompicsEvent {

    public final TAddress src;
    public final Serializable value;

    public KVStoreValue(TAddress src, Serializable value) {
        this.src = src;
        this.value = value;
    }
}