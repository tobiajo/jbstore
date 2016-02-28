package se.kth.id2203.jbstore.system.application;

import se.sics.kompics.KompicsEvent;

public class KVStoreMsg implements KompicsEvent {

    public final int dst;

    public KVStoreMsg(int dst) {
        this.dst = dst;
    }
}
