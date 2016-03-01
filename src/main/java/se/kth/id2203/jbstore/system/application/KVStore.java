package se.kth.id2203.jbstore.system.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.common.hash.Hashing;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import se.kth.id2203.jbstore.system.application.event.*;
import se.kth.id2203.jbstore.system.network.NetMsg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.test.TAddress;

public class KVStore extends ComponentDefinition {

    private final Positive<KVStorePort> kvStorePortPositive = requires(KVStorePort.class);

    private TAddress self;
    private HashSet<TAddress> replicationGroup;
    private HashMap<Long, Serializable> kvStore;
    private AtomicRegister onar;

    public KVStore() {
        subscribe(kvStoreInitHandler, kvStorePortPositive);
        subscribe(netMsgHandler, kvStorePortPositive);
    }

    public static long getKey(Serializable value) {
        return Hashing.murmur3_128().hashBytes(SerializationUtils.serialize(value)).asLong();
    }

    private Handler<KVStoreInit> kvStoreInitHandler = new Handler<KVStoreInit>() {
        @Override
        public void handle(KVStoreInit kvStoreInit) {
            self = kvStoreInit.self;
            replicationGroup = kvStoreInit.nodes;
            kvStore = new HashMap<>();
            onar = new AtomicRegister(); // Fail-Silent Algorithm: Read-Impose Write-Majority (1, N)
            onar.init();
        }
    };

    private Handler<NetMsg> netMsgHandler = new Handler<NetMsg>() {
        @Override
        public void handle(NetMsg netMsg) {
            switch (netMsg.cmd) {
                case NetMsg.GET:
                    onar.get(netMsg);
                    break;
                case NetMsg.READ:
                    onar.read(netMsg);
                    break;
                case NetMsg.VALUE:
                    onar.value(netMsg);
                    break;
                case NetMsg.PUT:
                    onar.put(netMsg);
                    break;
                case NetMsg.WRITE:
                    onar.write(netMsg);
                    break;
                case NetMsg.ACK:
                    onar.ack(netMsg);
                    break;
            }
        }
    };

    private void getCommit(TAddress requester, long id, long key) {
        send(requester, id, NetMsg.GET_RESPONSE, kvStore.get(key));
    }

    private void putCommit(TAddress requester, long id, Serializable value) {
        long hash = getKey(value);
        kvStore.put(hash, value);
        send(requester, id, NetMsg.PUT_RESPONSE, hash);
    }

    private void send(TAddress dst, long rid, byte cmd, Serializable body) {
        NetMsg netMsg = new NetMsg(self, dst, rid, NetMsg.KV_STORE, cmd, body);
        trigger(netMsg, kvStorePortPositive);
    }

    private void broadcast(byte cmd, long rid, Serializable body) {
        for (TAddress node : replicationGroup) {
            send(node, rid, cmd, body);
        }
    }

    private class AtomicRegister {

        int rid;
        HashMap<Integer, TAddress> ridRequester;

        int ts;
        Serializable val; // value
        int wts;

        HashMap<Integer, Integer> ridAcks;
        HashMap<Integer, HashMap<TAddress, Pair<Integer, Serializable>>> ridReadlist;
        HashMap<Integer, Long> ridReadval; // key
        HashMap<Integer, Boolean> ridReading;

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

        void get(NetMsg netMsg) {
            rid = rid + 1;
            ridAcks.put(rid, 0);
            ridReadlist.put(rid, new HashMap<>());
            ridReadval.put(rid, (Long) netMsg.body);
            ridReading.put(rid, true);
            ridRequester.put(rid, netMsg.getSource());
            broadcast(NetMsg.READ, netMsg.rid, rid);
        }

        void read(NetMsg netMsg) {
            int r = (int) netMsg.body;
            send(netMsg.getSource(), netMsg.rid, NetMsg.VALUE, Triple.of(r, ts, val));
        }

        void value(NetMsg netMsg) {
            Triple<Integer, Integer, Serializable> rTsprimeVprime = (Triple<Integer, Integer, Serializable>) netMsg.body;
            int r = rTsprimeVprime.getLeft();
            int tsprime = rTsprimeVprime.getMiddle();
            Serializable vprime = rTsprimeVprime.getRight();
            ridReadlist.get(r).put(netMsg.getSource(), Pair.of(tsprime, vprime));
            if (ridReadlist.get(r).size() > replicationGroup.size() / 2) {
                Pair<Integer, Serializable> maxtsReadval = highest(r);
                ridReadlist.get(r).clear();
                broadcast(NetMsg.WRITE, netMsg.rid, Triple.of(r, maxtsReadval.getLeft(), maxtsReadval.getRight()));
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

        void put(NetMsg netMsg) {
            Serializable v = netMsg.body;
            rid = rid + 1;
            wts = wts + 1;
            ridAcks.put(rid, 0);
            ridRequester.put(rid, netMsg.getSource());
            broadcast(NetMsg.WRITE, netMsg.rid, Triple.of(rid, wts, v));
        }

        void write(NetMsg netMsg) {
            Triple<Integer, Integer, Serializable> rTsprimeVprime = (Triple<Integer, Integer, Serializable>) netMsg.body;
            int r = rTsprimeVprime.getLeft();
            int tsprime = rTsprimeVprime.getMiddle();
            Serializable vprime = rTsprimeVprime.getRight();
            if (tsprime > ts) {
                ts = tsprime;
                val = vprime;
            }
            send(netMsg.getSource(), netMsg.rid, NetMsg.ACK, r);
        }

        void ack(NetMsg netMsg) {
            int r = (int) netMsg.body;
            ridAcks.put(r, ridAcks.get(r) + 1);
            if (ridAcks.get(r) > replicationGroup.size() / 2) {
                ridAcks.put(r, 0);
                if (ridReading.get(r) == Boolean.TRUE) {
                    ridReading.put(r, false);
                    getCommit(ridRequester.get(r), netMsg.rid, ridReadval.get(r));
                } else {
                    putCommit(ridRequester.get(r), netMsg.rid, val);
                }
            }
        }
    }
}
