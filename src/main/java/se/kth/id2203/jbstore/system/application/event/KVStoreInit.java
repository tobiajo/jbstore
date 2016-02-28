package se.kth.id2203.jbstore.system.application.event;

import se.sics.kompics.KompicsEvent;
import java.util.Set;

public class KVStoreInit implements KompicsEvent {

    public final Set<Integer> view;

    public KVStoreInit(Set<Integer> view) {
        this.view = view;
    }
}
