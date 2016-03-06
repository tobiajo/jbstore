package se.kth.id2203.jbstore.system.node.sub.membership;

import se.kth.id2203.jbstore.Util;
import se.kth.id2203.jbstore.system.node.sub.SubComponent;
import se.kth.id2203.jbstore.system.node.sub.application.KVStore;
import se.kth.id2203.jbstore.system.node.sub.application.KVStorePort;
import se.kth.id2203.jbstore.system.node.sub.application.event.KVStoreInit;
import se.kth.id2203.jbstore.system.node.sub.failuredetector.EPFDPort;
import se.kth.id2203.jbstore.system.node.sub.failuredetector.event.EPFDInit;
import se.kth.id2203.jbstore.system.node.sub.failuredetector.event.EPFDRestore;
import se.kth.id2203.jbstore.system.node.sub.failuredetector.event.EPFDSuspect;
import se.kth.id2203.jbstore.system.node.sub.membership.event.*;
import se.kth.id2203.jbstore.system.node.core.event.NodeMsg;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.test.TAddress;

import java.util.*;

public class ViewSync extends SubComponent {

    private final Positive<ViewSyncPort> viewSyncPortPositive = requires(ViewSyncPort.class);
    private final Negative<EPFDPort> epfdPortNegative = provides(EPFDPort.class);
    private final Negative<KVStorePort> kvStorePortNegative = provides(KVStorePort.class);

    public int id;
    private int n;
    private HashMap<Integer, TAddress> view;

    private Handler<ViewSyncInit> viewSyncInitHandler = new Handler<ViewSyncInit>() {
        @Override
        public void handle(ViewSyncInit event) {
            comp = NodeMsg.VIEW_SYNC;
            id = event.id;
            n = event.n;
            view = new HashMap<>();
            if (event.member == null) {
                view.put(id, event.self);
            } else {
                send(event.member, 0, NodeMsg.JOIN, 0, id);
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
                        bcast(new HashSet<TAddress>(view.values()), 0, NodeMsg.VIEW, 0, view);
                    }
                    break;
                case NodeMsg.VIEW:
                    view = (HashMap<Integer, TAddress>) nodeMsg.body;
                    trigger(new EPFDInit(getNodesToMonitor()), epfdPortNegative);
                    trigger(new KVStoreInit(getReplicationGroups()), kvStorePortNegative);
                    break;
                case NodeMsg.VIEW_REQUEST:
                    send(nodeMsg.getSource(), nodeMsg.rid, NodeMsg.VIEW, 0, view);
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
            System.out.println("Node" + id + ": nodesToMonitor:    " + nodesToMonitor);
            return nodesToMonitor;
        } else {
            nodesToMonitor.clear();
            nodesToMonitor.add(view.get(leaderId));
            System.out.println("Node" + id + ": nodesToMonitor:    " + nodesToMonitor);
            return nodesToMonitor;
        }
    }

    private HashMap<Integer, HashSet<TAddress>> getReplicationGroups() {
        HashMap<Integer, HashSet<TAddress>> replicationGroups = new HashMap<>();
        replicationGroups.put(id, getReplicationGroup(id));
        ListIterator<Integer> it = Util.getSortedList(view.keySet()).listIterator();
        while (it.next() != id) ;
        for (int i = 0; i <= KVStore.REPLICATION_DEGREE - 1; i++) {
            if (!it.hasPrevious()) {
                it = Util.getSortedList(view.keySet()).listIterator();
                while (it.hasNext()) it.next();
                int lastNodeId = it.previous();
                //System.out.println(id + " " + lastNodeId + "(last)");
                replicationGroups.put(lastNodeId, getReplicationGroup(lastNodeId));
            } else {
                int prevNodeId = it.previous();
                //System.out.println(id + " " + prevNodeId + "(prev)");
                replicationGroups.put(prevNodeId, getReplicationGroup(prevNodeId));
            }
        }
        for (int groupId : replicationGroups.keySet()) {
            System.out.println("Node" + id + ": replicationGroup" + groupId + ": " + replicationGroups.get(groupId));
        }
        return replicationGroups;
    }

    private HashSet<TAddress> getReplicationGroup(int nodeId) {
        HashSet<TAddress> replicationGroup = new HashSet<>();
        replicationGroup.add(view.get(nodeId));
        ListIterator<Integer> it = Util.getSortedList(view.keySet()).listIterator();
        while (it.next() != nodeId) ;
        for (int i = 0; i < KVStore.REPLICATION_DEGREE - 1; i++) {
            if (!it.hasNext()) {
                it = Util.getSortedList(view.keySet()).listIterator();
            }
            replicationGroup.add(view.get(it.next()));
        }
        return replicationGroup;
    }
}
