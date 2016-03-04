package se.kth.id2203.jbstore.system.membership;

import se.kth.id2203.jbstore.system.application.KVStore;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDInit;
import se.kth.id2203.jbstore.system.network.NodePort;
import se.kth.id2203.jbstore.system.application.KVStorePort;
import se.kth.id2203.jbstore.system.application.event.KVStoreInit;
import se.kth.id2203.jbstore.system.failuredetector.EPFDPort;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDRestore;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDSuspect;
import se.kth.id2203.jbstore.system.membership.event.*;
import se.kth.id2203.jbstore.system.network.event.NodeMsg;
import se.kth.id2203.jbstore.system.network.event.NodeMsgSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.test.TAddress;

import java.io.Serializable;
import java.util.*;

public class ViewSync extends ComponentDefinition {

    private final Positive<NodePort> nodePortPositive = requires(NodePort.class);
    private final Positive<ViewSyncPort> viewSyncPortPositive = requires(ViewSyncPort.class);
    private final Negative<EPFDPort> epfdPortNegative = provides(EPFDPort.class);
    private final Negative<KVStorePort> kvStorePortNegative = provides(KVStorePort.class);

    private int id;
    private int n;
    private HashMap<Integer, TAddress> view;

    private Handler<ViewSyncInit> viewSyncInitHandler = new Handler<ViewSyncInit>() {
        @Override
        public void handle(ViewSyncInit event) {
            id = event.id;
            n = event.n;
            view = new HashMap<>();
            if (event.member == null) {
                view.put(id, event.self);
            } else {
                send(event.member, NodeMsg.JOIN, -1, id);
            }
        }
    };

    private Handler<NodeMsg> netMsgHandler = new Handler<NodeMsg>() {
        @Override
        public void handle(NodeMsg nodeMsg) {
            switch (nodeMsg.cmd) {
                case NodeMsg.JOIN:
                    view.put((Integer) nodeMsg.body, nodeMsg.getSource());
                    if (view.size() == n) {
                        send(new HashSet<TAddress>(view.values()), NodeMsg.VIEW, -1, view);
                    }
                    break;
                case NodeMsg.VIEW:
                    view = (HashMap<Integer, TAddress>) nodeMsg.body;
                    trigger(new EPFDInit(getNodesToMonitor()), epfdPortNegative);
                    trigger(new KVStoreInit(getReplicationGroup()), kvStorePortNegative);
                    break;
                case NodeMsg.VIEW_REQUEST:
                    send(nodeMsg.getSource(), NodeMsg.VIEW, nodeMsg.rid, view);
                    break;
            }
        }
    };

    private Handler<EPFDSuspect> epfdSuspectHandler = new Handler<EPFDSuspect>() {
        @Override
        public void handle(EPFDSuspect event) {
            System.out.println("Node" + id + ": epfdSuspectHandler called: " + event.p);
        }
    };

    private Handler<EPFDRestore> epfdRestoreHandler = new Handler<EPFDRestore>() {
        @Override
        public void handle(EPFDRestore event) {
            System.out.println("Node" + id + ": epfdRestoreHandler called: " + event.p);
        }
    };

    {
        subscribe(viewSyncInitHandler, viewSyncPortPositive);
        subscribe(netMsgHandler, viewSyncPortPositive);
        subscribe(epfdSuspectHandler, epfdPortNegative);
        subscribe(epfdRestoreHandler, epfdPortNegative);
    }

    private void send(TAddress dst, byte cmd, long rid, Serializable body) {
        trigger(new NodeMsgSend(dst, NodeMsg.VIEW_SYNC, cmd, rid, body), nodePortPositive);
    }

    private void send(Set<TAddress> dstGroup, byte cmd, long rid, Serializable body) {
        for (TAddress dst : dstGroup) {
            send(dst, cmd, rid, body);
        }
    }

    private HashSet<TAddress> getNodesToMonitor() {
        int leaderId = -1;
        HashSet<TAddress> nodesToMonitor = new HashSet<>();
        for (Integer nodeId : view.keySet()) {
            if (leaderId == -1 || leaderId > nodeId) {
                leaderId = nodeId;
            }
            if (id != nodeId) {
                nodesToMonitor.add(view.get(nodeId));
            }
        }
        if (leaderId == id) {
            System.out.println("Node" + id + ": nodesToMonitor:   " + nodesToMonitor);
            return nodesToMonitor;
        } else {
            nodesToMonitor.clear();
            nodesToMonitor.add(view.get(leaderId));
            System.out.println("Node" + id + ": nodesToMonitor:   " + nodesToMonitor);
            return nodesToMonitor;
        }
    }

    private HashSet<TAddress> getReplicationGroup() {
        HashSet<TAddress> replicationGroup = new HashSet<>();
        Iterator it = view.keySet().iterator(); // sorted!
        while ((int) it.next() != id);
        replicationGroup.add(view.get(id));
        for (int i = 0; i < KVStore.replicationDegree - 1; i++) {
            if (!it.hasNext()) {
                it = view.keySet().iterator(); // sorted!
            }
            replicationGroup.add(view.get(it.next()));
        }
        System.out.println("Node" + id + ": replicationGroup: " + replicationGroup);
        return replicationGroup;
    }
}
