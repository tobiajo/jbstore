package se.kth.id2203.jbstore.system.node.sub.failuredetector.event;

import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.util.HashSet;


public class EPFDInit implements KompicsEvent {

    public final HashSet<TAddress> nodesToMonitor;

    public EPFDInit(HashSet<TAddress> nodesToMonitor) {
        this.nodesToMonitor = nodesToMonitor;
    }
}
