package se.kth.id2203.jbstore.system.membership.event;


import se.sics.kompics.KompicsEvent;
import se.sics.test.TAddress;

import java.util.HashMap;

public class ViewSyncView implements KompicsEvent {

    public final HashMap<Integer, TAddress> view;

    public ViewSyncView(HashMap<Integer, TAddress> view) {
        this.view = view;
    }
}
