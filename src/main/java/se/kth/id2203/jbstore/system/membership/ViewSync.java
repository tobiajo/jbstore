package se.kth.id2203.jbstore.system.membership;

import se.kth.id2203.jbstore.system.application.KVStorePort;
import se.kth.id2203.jbstore.system.application.event.KVStoreInit;
import se.kth.id2203.jbstore.system.failuredetector.EPFDPort;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDInit;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDRestore;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDSuspect;
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
    private final Positive<EPFDPort> epfdPortPositive = requires(EPFDPort.class);

    private TAddress self;
    private TAddress member;
    private int selfId;
    private int n;
    private HashMap<Integer, TAddress> view;

    public ViewSync() {
        subscribe(netMsgHandler, viewSyncPortPositive);
        subscribe(viewSyncInitHandler, viewSyncPortPositive);
        subscribe(epfdSuspectHandler, epfdPortPositive);
        subscribe(epfdRestoreHandler, epfdPortPositive);
    }

    private Handler<ViewSyncInit> viewSyncInitHandler = new Handler<ViewSyncInit>() {
        @Override
        public void handle(ViewSyncInit viewSyncInit) {
            self = viewSyncInit.self;
            member = viewSyncInit.member;
            selfId = viewSyncInit.selfId;
            n = viewSyncInit.n;
            view = new HashMap<>();
            if (member == null) {
                // Creator node
                view.put(selfId, self);
            } else {
                // Joiner node
                send(member, -1, NetMsg.JOIN, selfId);
            }
            System.out.println(self + ": viewSyncInitHandler called");
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
                    trigger(new EPFDInit(self, getNodesToMonitor()), epfdPortPositive);
                    break;
                case NetMsg.VIEW_REQUEST:
                    send(netMsg.getSource(), netMsg.rid, NetMsg.VIEW, view);
                    break;
            }
        }
    };

    private Handler<EPFDSuspect> epfdSuspectHandler = new Handler<EPFDSuspect>() {
        @Override
        public void handle(EPFDSuspect epfdSuspect) {
            System.out.println(self + ": epfdSuspectHandler called");
        }
    };

    private Handler<EPFDRestore> epfdRestoreHandler = new Handler<EPFDRestore>() {
        @Override
        public void handle(EPFDRestore epfdRestore) {
            System.out.println(self + ": epfdRestoreHandler called");
        }
    };

    private HashSet<TAddress> getNodesToMonitor() {
        int leaderId = -1;
        HashSet<TAddress> nodesToMonitor = new HashSet<>();
        for (Integer nodeId : view.keySet()) {
            if (leaderId == -1 || leaderId > nodeId) {
                leaderId = nodeId;
            }
            if (selfId != nodeId) {
                nodesToMonitor.add(view.get(nodeId));
            }
        }
        if (leaderId == selfId) {
            return nodesToMonitor;
        } else {
            nodesToMonitor.clear();
            nodesToMonitor.add(view.get(leaderId));
            return nodesToMonitor;
        }
    }

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
