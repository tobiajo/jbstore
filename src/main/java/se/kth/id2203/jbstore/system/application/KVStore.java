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

    Handler<KVStoreInit> kvStoreInitHandler = new Handler<KVStoreInit>() {
        @Override
        public void handle(KVStoreInit kvStoreInit) {
            self = kvStoreInit.self;
            replicationGroup = kvStoreInit.nodes;
            kvStore = new HashMap<>();
            init();
        }
    };

    Handler<NetMsg> netMsgHandler = new Handler<NetMsg>() {
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

    TAddress writer;
    TAddress reader;

    int ts;
    Serializable val;
    int wts;
    int acks;
    int rid;
    HashMap<TAddress, Pair<Integer, Serializable>> readlist;
    Serializable readval;
    Serializable writeval;
    boolean reading;

    private void init() {
        ts = 0;
        val = null;
        wts = 0;
        acks = 0;
        rid = 0;
        readlist = new HashMap<>();
        readval = null;
        reading = false;
    }

    private void get(NetMsg netMsg) {
        reader = netMsg.getSource();
        rid = rid + 1;
        acks = 0;
        readlist = new HashMap<>();
        readval = netMsg.body;
        reading = true;
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
            readlist.put(netMsg.getSource(), Pair.of(tsPrim, vPrim));
            if (readlist.size() > replicationGroup.size() / 2) {
                Pair<Integer, Serializable> maxtsReadval = highest();
                readlist = new HashMap<>();
                broadcast(NetMsg.WRITE, Triple.of(rid, maxtsReadval.getLeft(), maxtsReadval.getRight()));
            }
        }
    }

    private Pair<Integer, Serializable> highest() {
        Pair<Integer, Serializable> maxtsReadval = Pair.of(-1, null);
        for (Map.Entry<TAddress, Pair<Integer, Serializable>> entry : readlist.entrySet()) {
            Pair<Integer, Serializable> tsReadval = entry.getValue();
            if (entry.getValue().getLeft() > maxtsReadval.getLeft()) {
                maxtsReadval = tsReadval;
            }
        }
        return maxtsReadval;
    }

    private void put(NetMsg netMsg) {
        writer = netMsg.getSource();
        Serializable v = netMsg.body;
        rid = rid + 1;
        wts = wts + 1;
        acks = 0;
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
        if (r == rid) {
            acks = acks + 1;
            if (acks > replicationGroup.size() / 2) {
                acks = 0;
                if (reading == true) {
                    reading = false;
                    send(reader, NetMsg.VALUE, kvStore.get(readval));
                    System.out.println("Read return");
                } else {
                    long hash = getHash(val);
                    kvStore.put(hash, val);
                    send(writer, NetMsg.ACK, hash);
                    System.out.println("Write return");
                }
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

    public static long getHash(Serializable value) {
        return Hashing.murmur3_128().hashBytes(SerializationUtils.serialize(value)).asLong();
    }
}
