package se.kth.id2203.jbstore.system.node.sub.failuredetector.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

public class EPFDSuspect implements KompicsEvent {

    public final TAddress p;

    public EPFDSuspect(TAddress p) {
        this.p = p;
    }
}
