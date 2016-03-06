package se.kth.id2203.jbstore.system.node.sub.application.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.util.HashMap;
import java.util.HashSet;

public class KVStoreInit implements KompicsEvent {

    public final HashMap<Integer, HashSet<TAddress>> replicationGroups;

    public KVStoreInit(HashMap<Integer, HashSet<TAddress>> replicationGroups) {
        this.replicationGroups = replicationGroups;
    }
}
