package se.kth.id2203.jbstore.system.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.hash.Hashing;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import se.kth.id2203.jbstore.system.network.NodePort;
import se.kth.id2203.jbstore.system.application.event.*;
import se.kth.id2203.jbstore.system.network.event.NodeMsg;
import se.kth.id2203.jbstore.system.network.event.NodeMsgSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.test.TAddress;

public class KVStore extends ComponentDefinition {

    public static final int replicationDegree = 3;
    private final Positive<NodePort> nodePortPositive = requires(NodePort.class);
    private final Positive<KVStorePort> kvStorePortPositive = requires(KVStorePort.class);

    private HashMap<Long, Serializable> kvStore;
    private AtomicRegister onar;

    public static long getKey(Serializable value) {
        return Hashing.murmur3_128().hashBytes(SerializationUtils.serialize(value)).asLong();
    }

    private Handler<KVStoreInit> kvStoreInitHandler = new Handler<KVStoreInit>() {
        @Override
        public void handle(KVStoreInit event) {
            kvStore = new HashMap<>();
            onar = new AtomicRegister(event.replicationGroup);
            onar.init();
        }
    };

    private Handler<NodeMsg> netMsgHandler = new Handler<NodeMsg>() {
        @Override
        public void handle(NodeMsg nodeMsg) {
            if (onar == null) {
                return;
            }
            switch (nodeMsg.cmd) {
                case NodeMsg.GET:
                    onar.get(nodeMsg);
                    break;
                case NodeMsg.READ:
                    onar.read(nodeMsg);
                    break;
                case NodeMsg.VALUE:
                    onar.value(nodeMsg);
                    break;
                case NodeMsg.PUT:
                    onar.put(nodeMsg);
                    break;
                case NodeMsg.WRITE:
                    onar.write(nodeMsg);
                    break;
                case NodeMsg.ACK:
                    onar.ack(nodeMsg);
                    break;
            }
        }
    };

    {
        subscribe(kvStoreInitHandler, kvStorePortPositive);
        subscribe(netMsgHandler, kvStorePortPositive);
    }

    private void send(TAddress dst, byte cmd, long rid, Serializable body) {
        trigger(new NodeMsgSend(dst, NodeMsg.KV_STORE, cmd, rid, body), nodePortPositive);
    }

    private void send(Set<TAddress> dstGroup, byte cmd, long rid, Serializable body) {
        for (TAddress dst : dstGroup) {
            send(dst, cmd, rid, body);
        }
    }

    private void getCommit(TAddress requester, long rid, long key) {
        send(requester, NodeMsg.GET_RESPONSE, rid, kvStore.get(key));
    }

    private void putCommit(TAddress requester, long rid, Serializable value) {
        long hash = getKey(value);
        kvStore.put(hash, value);
        send(requester, NodeMsg.PUT_RESPONSE, rid, hash);
    }

    // Algorithm 4.6: Read-Impose Write-Majority (part 1, read)
    private class AtomicRegister {

        final HashSet<TAddress> replicationGroup;

        int rid;
        HashMap<Integer, TAddress> ridRequester;

        int ts;
        Serializable val; // value
        int wts;

        HashMap<Integer, Integer> ridAcks;
        HashMap<Integer, HashMap<TAddress, Pair<Integer, Serializable>>> ridReadlist;
        HashMap<Integer, Long> ridReadval; // key
        HashMap<Integer, Boolean> ridReading;

        AtomicRegister(HashSet<TAddress> replicationGroup) {
            this.replicationGroup = replicationGroup;
        }

        void init() {
            ts = 0;
            val = null;
            wts = 0;
            rid = 0;
            ridAcks = new HashMap<>();
            ridReadlist = new HashMap<>();
            ridReadval = new HashMap<>();
            ridReading = new HashMap<>();
            ridRequester = new HashMap<>();
        }

        void get(NodeMsg nodeMsg) {
            rid = rid + 1;
            ridAcks.put(rid, 0);
            ridReadlist.put(rid, new HashMap<>());
            ridReadval.put(rid, (Long) nodeMsg.body);
            ridReading.put(rid, true);
            ridRequester.put(rid, nodeMsg.getSource());
            send(replicationGroup, NodeMsg.READ, nodeMsg.rid, rid);
        }

        void read(NodeMsg nodeMsg) {
            int r = (int) nodeMsg.body;
            send(nodeMsg.getSource(), NodeMsg.VALUE, nodeMsg.rid, Triple.of(r, ts, val));
        }

        void value(NodeMsg nodeMsg) {
            Triple<Integer, Integer, Serializable> rTsprimeVprime = (Triple<Integer, Integer, Serializable>) nodeMsg.body;
            int r = rTsprimeVprime.getLeft();
            int tsprime = rTsprimeVprime.getMiddle();
            Serializable vprime = rTsprimeVprime.getRight();
            ridReadlist.get(r).put(nodeMsg.getSource(), Pair.of(tsprime, vprime));
            if (ridReadlist.get(r).size() > replicationGroup.size() / 2) {
                Pair<Integer, Serializable> maxtsReadval = highest(r);
                ridReadlist.get(r).clear();
                send(replicationGroup, NodeMsg.WRITE, nodeMsg.rid, Triple.of(r, maxtsReadval.getLeft(), maxtsReadval.getRight()));
            }
        }

        Pair<Integer, Serializable> highest(int r) {
            Pair<Integer, Serializable> maxtsReadval = Pair.of(-1, null);
            for (Map.Entry<TAddress, Pair<Integer, Serializable>> entry : ridReadlist.get(r).entrySet()) {
                Pair<Integer, Serializable> tsReadval = entry.getValue();
                if (entry.getValue().getLeft() > maxtsReadval.getLeft()) {
                    maxtsReadval = tsReadval;
                }
            }
            return maxtsReadval;
        }

        void put(NodeMsg nodeMsg) {
            Serializable v = nodeMsg.body;
            rid = rid + 1;
            wts = wts + 1;
            ridAcks.put(rid, 0);
            ridRequester.put(rid, nodeMsg.getSource());
            send(replicationGroup, NodeMsg.WRITE, nodeMsg.rid, Triple.of(rid, wts, v));
        }

        void write(NodeMsg nodeMsg) {
            Triple<Integer, Integer, Serializable> rTsprimeVprime = (Triple<Integer, Integer, Serializable>) nodeMsg.body;
            int r = rTsprimeVprime.getLeft();
            int tsprime = rTsprimeVprime.getMiddle();
            Serializable vprime = rTsprimeVprime.getRight();
            if (tsprime > ts) {
                ts = tsprime;
                val = vprime;
            }
            send(nodeMsg.getSource(), NodeMsg.ACK, nodeMsg.rid, r);
        }

        void ack(NodeMsg nodeMsg) {
            int r = (int) nodeMsg.body;
            ridAcks.put(r, ridAcks.get(r) + 1);
            if (ridAcks.get(r) > replicationGroup.size() / 2) {
                ridAcks.put(r, 0);
                if (ridReading.get(r) == Boolean.TRUE) {
                    ridReading.put(r, false);
                    getCommit(ridRequester.get(r), nodeMsg.rid, ridReadval.get(r));
                } else {
                    putCommit(ridRequester.get(r), nodeMsg.rid, val);
                }
            }
        }
    }
}
