package se.kth.id2203.jbstore.system.membership.event;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

public class ViewSyncDeliver implements KompicsEvent {

    public final Serializable msgBody;

    public ViewSyncDeliver(Serializable msgBody) {
        this.msgBody = msgBody;
    }
}
