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

    private Positive<KVStorePort> kvStorePortPositive = requires(KVStorePort.class);

    private TAddress self;
    private HashSet<TAddress> replicationGroup;
    private HashMap<Long, Serializable> kvStore;

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
            init();
        }
    };

    private Handler<NetMsg> netMsgHandler = new Handler<NetMsg>() {
        @Override
        public void handle(NetMsg netMsg) {
            switch (netMsg.cmd) {
                case NetMsg.GET:
                    get(netMsg);
                    break;
                case NetMsg.READ:
                    read(netMsg);
                    break;
                case NetMsg.VALUE:
                    value(netMsg);
                    break;
                case NetMsg.PUT:
                    put(netMsg);
                    break;
                case NetMsg.WRITE:
                    write(netMsg);
                    break;
                case NetMsg.ACK:
                    ack(netMsg);
                    break;
            }
        }
    };

    private int ts;
    private Serializable val;
    private int wts;
    private TAddress writer;

    private int rid;
    private HashMap<Integer, Integer> acks;
    private HashMap<Integer, HashMap<TAddress, Pair<Integer, Serializable>>> readlist;
    private HashMap<Integer, Long> readval;
    private HashMap<Integer, Boolean> reading;
    private HashMap<Integer, TAddress> reader;

    private void init() {
        ts = 0;
        val = null;
        wts = 0;
        rid = 0;
        acks = new HashMap<>();
        readlist = new HashMap<>();
        readval = new HashMap<>();
        reading = new HashMap<>();
        reader = new HashMap<>();
    }

    private void get(NetMsg netMsg) {
        rid = rid + 1;
        acks.put(rid, 0);
        readlist.put(rid, new HashMap<>());
        readval.put(rid, (Long) netMsg.body);
        reading.put(rid, true);
        reader.put(rid, netMsg.getSource());
        broadcast(NetMsg.READ, rid);
    }

    private void read(NetMsg netMsg) {
        int r = (int) netMsg.body;
        send(netMsg.getSource(), NetMsg.VALUE, Triple.of(r, ts, val));
    }

    private void value(NetMsg netMsg) {
        Triple<Integer, Integer, Serializable> rTsV = (Triple<Integer, Integer, Serializable>) netMsg.body;
        int r = rTsV.getLeft();
        int tsPrim = rTsV.getMiddle();
        Serializable vPrim = rTsV.getRight();
        if (r == rid) {
            readlist.get(r).put(netMsg.getSource(), Pair.of(tsPrim, vPrim));
            if (readlist.get(r).size() > replicationGroup.size() / 2) {
                Pair<Integer, Serializable> maxtsReadval = highest(r);
                readlist.get(r).clear();
                broadcast(NetMsg.WRITE, Triple.of(rid, maxtsReadval.getLeft(), maxtsReadval.getRight()));
            }
        }
    }

    private Pair<Integer, Serializable> highest(int r) {
        Pair<Integer, Serializable> maxtsReadval = Pair.of(-1, null);
        for (Map.Entry<TAddress, Pair<Integer, Serializable>> entry : readlist.get(r).entrySet()) {
            Pair<Integer, Serializable> tsReadval = entry.getValue();
            if (entry.getValue().getLeft() > maxtsReadval.getLeft()) {
                maxtsReadval = tsReadval;
            }
        }
        return maxtsReadval;
    }

    private void put(NetMsg netMsg) {
        Serializable v = netMsg.body;
        rid = rid + 1;
        wts = wts + 1;
        acks.put(rid, 0);
        writer = netMsg.getSource();
        broadcast(NetMsg.WRITE, Triple.of(rid, wts, v));
    }

    private void write(NetMsg netMsg) {
        Triple<Integer, Integer, Serializable> rTsV = (Triple<Integer, Integer, Serializable>) netMsg.body;
        int r = rTsV.getLeft();
        int tsPrim = rTsV.getMiddle();
        Serializable vPrim = rTsV.getRight();
        if (tsPrim > ts) {
            ts = tsPrim;
            val = vPrim;
        }
        send(netMsg.getSource(), NetMsg.ACK, r);
    }

    private void ack(NetMsg netMsg) {
        int r = (int) netMsg.body;
        acks.put(r, acks.get(r) + 1);
        if (acks.get(r) > replicationGroup.size() / 2) {
            acks.put(r, 0);
            if (reading.get(r) == Boolean.TRUE) {
                reading.put(r, false);
                send(reader.get(r), NetMsg.VALUE, kvStore.get(readval.get(r)));
                System.out.println("Read return");
            } else {
                long hash = getKey(val);
                kvStore.put(hash, val);
                send(writer, NetMsg.ACK, hash);
                System.out.println("Write return");
            }
        }
    }

    private void send(TAddress dst, byte cmd, Serializable body) {
        NetMsg netMsg = new NetMsg(self, dst, -1, NetMsg.KV_STORE, cmd, body);
        trigger(netMsg, kvStorePortPositive);
    }

    private void broadcast(byte cmd, Serializable body) {
        for (TAddress node : replicationGroup) {
            send(node, cmd, body);
        }
    }
}
