package se.kth.id2203.jbstore.system.node.sub.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import se.kth.id2203.jbstore.system.node.sub.SubComponent;
import se.kth.id2203.jbstore.system.node.sub.application.event.*;
import se.kth.id2203.jbstore.system.node.core.event.NodeMsg;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.test.TAddress;

public class KVStore extends SubComponent {

    public static final int REPLICATION_DEGREE = 3;
    private final Positive<KVStorePort> kvStorePortPositive = requires(KVStorePort.class);

    private HashMap<Integer, AtomicRegister> onars;

    private Handler<KVStoreInit> kvStoreInitHandler = new Handler<KVStoreInit>() {
        @Override
        public void handle(KVStoreInit event) {
            comp = NodeMsg.KV_STORE;
            onars = new HashMap<>();
            for (Map.Entry<Integer, HashSet<TAddress>> group : event.replicationGroups.entrySet()) {
                onars.put(group.getKey(), new AtomicRegister(group.getKey(), group.getValue()));
            }
        }
    };

    private Handler<NodeMsg> netMsgHandler = new Handler<NodeMsg>() {
        @Override
        public void handle(NodeMsg nodeMsg) {
            int groupId = nodeMsg.inst;
            if (onars.get(groupId) == null) {
                return;
            }
            switch (nodeMsg.cmd) {
                case NodeMsg.GET:
                    onars.get(groupId).get(nodeMsg);
                    break;
                case NodeMsg.READ:
                    onars.get(groupId).read(nodeMsg);
                    break;
                case NodeMsg.VALUE:
                    onars.get(groupId).value(nodeMsg);
                    break;
                case NodeMsg.PUT:
                    onars.get(groupId).put(nodeMsg);
                    break;
                case NodeMsg.WRITE:
                    onars.get(groupId).write(nodeMsg);
                    break;
                case NodeMsg.ACK:
                    onars.get(groupId).ack(nodeMsg);
                    break;
            }
        }
    };

    {
        subscribe(kvStoreInitHandler, kvStorePortPositive);
        subscribe(netMsgHandler, kvStorePortPositive);
    }

    private void readReturn(TAddress requester, long rid, int inst, Serializable value) {
        send(requester, rid, NodeMsg.GET_RESPONSE, inst, value);
    }

    private void writeReturn(TAddress requester, int inst, long rid) {
        send(requester, rid, NodeMsg.PUT_RESPONSE, inst, null);
    }

    // Algorithm 4.6: Read-Impose Write-Majority
    private class AtomicRegister {

        final int groupId;
        final HashSet<TAddress> group;

        long wts;
        HashMap<String, Pair<Long, Serializable>> keyTsVal;

        long rid;
        HashMap<Long, TAddress> ridRequester;
        HashMap<Long, Integer> ridAcks;
        HashMap<Long, Boolean> ridReading;
        HashMap<Long, HashMap<TAddress, Pair<Long, Serializable>>> ridReadlist;

        AtomicRegister(int groupId, HashSet<TAddress> group) {
            this.groupId = groupId;
            this.group = group;
            init();
        }

        void init() {
            wts = 0;
            keyTsVal = new HashMap<>();
            rid = 0;
            ridRequester = new HashMap<>();
            ridAcks = new HashMap<>();
            ridReading = new HashMap<>();
            ridReadlist = new HashMap<>();
        }

        void get(NodeMsg nodeMsg) {
            String key = (String) nodeMsg.body;
            //
            rid = rid + 1;
            ridRequester.put(rid, nodeMsg.getSource());
            ridAcks.put(rid, 0);
            ridReading.put(rid, true);
            ridReadlist.put(rid, new HashMap<>());
            bcast(group, nodeMsg.rid, NodeMsg.READ, groupId, Pair.of(rid, key));
        }

        void read(NodeMsg nodeMsg) {
            Pair<Long, String> rKey = (Pair<Long, String>) nodeMsg.body;
            long r = rKey.getLeft();
            String key = rKey.getRight();
            //
            send(nodeMsg.getSource(), nodeMsg.rid, NodeMsg.VALUE, groupId, Triple.of(r, key, keyTsVal.get(key)));
        }

        void value(NodeMsg nodeMsg) {
            Triple<Long, String, Pair<Long, Serializable>> rKeyTsprimeVprime = (Triple<Long, String, Pair<Long, Serializable>>) nodeMsg.body;
            long r = rKeyTsprimeVprime.getLeft();
            String key = rKeyTsprimeVprime.getMiddle();
            Pair<Long, Serializable> tsprimeVprime = rKeyTsprimeVprime.getRight();
            //
            ridReadlist.get(r).put(nodeMsg.getSource(), tsprimeVprime);
            if (ridReadlist.get(r).size() > group.size() / 2) {
                Pair<Long, Serializable> maxtsReadval = highest(r);
                ridReadlist.get(r).clear();
                bcast(group, nodeMsg.rid, NodeMsg.WRITE, groupId, Triple.of(r, key, maxtsReadval));
            }
        }

        Pair<Long, Serializable> highest(long r) {
            Pair<Long, Serializable> maxtsReadval = null;
            for (Pair<Long, Serializable> tsReadval : ridReadlist.get(r).values()) {
                if (maxtsReadval == null || tsReadval != null && maxtsReadval.getLeft() < tsReadval.getLeft()) {
                    maxtsReadval = tsReadval;
                }
            }
            return maxtsReadval;
        }

        void put(NodeMsg nodeMsg) {
            Pair<String, Serializable> keyV = (Pair<String, Serializable>) nodeMsg.body;
            String key = keyV.getLeft();
            Serializable v = keyV.getRight();
            //
            wts = wts + 1;
            rid = rid + 1;
            ridAcks.put(rid, 0);
            ridRequester.put(rid, nodeMsg.getSource());
            bcast(group, nodeMsg.rid, NodeMsg.WRITE, groupId, Triple.of(rid, key, Pair.of(wts, v)));
        }

        void write(NodeMsg nodeMsg) {
            Triple<Long, String, Pair<Long, Serializable>> rKeyTsprimeVprime = (Triple<Long, String, Pair<Long, Serializable>>) nodeMsg.body;
            long r = rKeyTsprimeVprime.getLeft();
            String key = rKeyTsprimeVprime.getMiddle();
            Pair<Long, Serializable> tsprimeVprime = rKeyTsprimeVprime.getRight();
            //
            if (keyTsVal.get(key) == null || tsprimeVprime.getLeft() > keyTsVal.get(key).getLeft()) {
                keyTsVal.put(key, tsprimeVprime);
            }
            send(nodeMsg.getSource(), nodeMsg.rid, NodeMsg.ACK, groupId, Pair.of(r, key));
        }

        void ack(NodeMsg nodeMsg) {
            Pair<Long, String> rKey = (Pair<Long, String>) nodeMsg.body;
            long r = rKey.getLeft();
            String key = rKey.getRight();
            //
            ridAcks.put(r, ridAcks.get(r) + 1);
            if (ridAcks.get(r) > group.size() / 2) {
                ridAcks.put(r, 0);
                if (ridReading.get(r) == Boolean.TRUE) {
                    ridReading.put(r, false);
                    Serializable val;
                    if (keyTsVal.get(key) == null) {
                        val = null;
                    } else {
                        val = keyTsVal.get(key).getRight();
                    }
                    readReturn(ridRequester.get(r), nodeMsg.rid, groupId, val);
                } else {
                    writeReturn(ridRequester.get(r), groupId, nodeMsg.rid);
                }
            }
        }
    }
}
