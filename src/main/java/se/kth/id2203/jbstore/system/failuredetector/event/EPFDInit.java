package se.kth.id2203.jbstore.system.failuredetector.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.util.HashSet;

public class EPFDInit implements KompicsEvent {

    public final TAddress self;
    public final HashSet<TAddress> nodesToMonitor;

    public EPFDInit(TAddress self, HashSet<TAddress> nodesToMonitor) {
        this.self = self;
        this.nodesToMonitor = nodesToMonitor;
    }
}
