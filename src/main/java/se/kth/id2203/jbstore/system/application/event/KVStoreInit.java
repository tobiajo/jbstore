package se.kth.id2203.jbstore.system.application.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.util.HashSet;

public class KVStoreInit implements KompicsEvent {

    public final TAddress self;
    public final HashSet<TAddress> nodes;

    public KVStoreInit(TAddress self, HashSet<TAddress> nodes) {
        this.self = self;
        this.nodes = nodes;
    }
}
