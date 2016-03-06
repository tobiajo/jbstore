package se.kth.id2203.jbstore.system.node.sub.failuredetector.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

public class EPFDRestore implements KompicsEvent {

    public final TAddress p;

    public EPFDRestore(TAddress p) {
        this.p = p;
    }
}
