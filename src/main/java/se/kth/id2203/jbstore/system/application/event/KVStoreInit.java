package se.kth.id2203.jbstore.system.application.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.util.HashSet;

public class KVStoreInit implements KompicsEvent {

    public final HashSet<TAddress> replicationGroup;

    public KVStoreInit(HashSet<TAddress> replicationGroup) {
        this.replicationGroup = replicationGroup;
    }
}
