package se.kth.id2203.jbstore.system.membership;

import se.kth.id2203.jbstore.system.application.KVStorePort;
import se.kth.id2203.jbstore.system.application.event.KVStoreInit;
import se.kth.id2203.jbstore.system.membership.event.*;
import se.kth.id2203.jbstore.system.network.NetMsg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.test.TAddress;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ViewSync extends ComponentDefinition {

    private final Negative<KVStorePort> kvStorePortNegative = provides(KVStorePort.class);
    private final Positive<ViewSyncPort> viewSyncPortPositive = requires(ViewSyncPort.class);

    private TAddress self;
    private TAddress member;
    private int id;
    private int n;
    private HashMap<Integer, TAddress> view;

    public ViewSync() {
        subscribe(netMsgHandler, viewSyncPortPositive);
        subscribe(viewSyncInitHandler, viewSyncPortPositive);
    }

    private Handler<ViewSyncInit> viewSyncInitHandler = new Handler<ViewSyncInit>() {
        @Override
        public void handle(ViewSyncInit viewSyncInit) {
            self = viewSyncInit.self;
            member = viewSyncInit.member;
            id = viewSyncInit.id;
            n = viewSyncInit.n;
            view = new HashMap<>();
            if (member == null) {
                // Creator node
                view.put(id, self);
            } else {
                // Joiner node
                send(member, -1, NetMsg.JOIN, id);
            }
        }
    };

    private Handler<NetMsg> netMsgHandler = new Handler<NetMsg>() {
        @Override
        public void handle(NetMsg netMsg) {
            switch (netMsg.cmd) {
                case NetMsg.JOIN:
                    view.put((Integer) netMsg.body, netMsg.getSource());
                    if (view.size() == n) {
                        broadcast(NetMsg.VIEW, view);
                    }
                    break;
                case NetMsg.VIEW:
                    view = (HashMap<Integer, TAddress>) netMsg.body;
                    HashSet<TAddress> nodes = new HashSet<>();
                    for (Map.Entry<Integer, TAddress> entry : view.entrySet()) {
                        nodes.add(entry.getValue());
                    }
                    trigger(new KVStoreInit(self, nodes), kvStorePortNegative);
                    break;
                case NetMsg.VIEW_REQUEST:
                    send(netMsg.getSource(), netMsg.rid, NetMsg.VIEW, view);
                    break;
            }
        }
    };

    private void send(TAddress dst, long rid, byte cmd, Serializable body) {
        NetMsg viewSyncMsg = new NetMsg(self, dst, rid, NetMsg.VIEW_SYNC, cmd, body);
        trigger(viewSyncMsg, viewSyncPortPositive);
    }

    private void broadcast(byte cmd, Serializable body) {
        for (Integer key : view.keySet()) {
            send(view.get(key), -1, cmd, body);
        }
    }
}
