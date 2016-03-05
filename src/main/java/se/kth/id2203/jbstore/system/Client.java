package se.kth.id2203.jbstore.system;

import com.google.common.hash.Hashing;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.jbstore.system.application.KVStore;
import se.kth.id2203.jbstore.system.network.event.NodeMsg;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Client extends ComponentDefinition {

    private final Positive<Network> networkPositive = requires(Network.class);
    private final Positive<Timer> timerPositive = requires(Timer.class);
    private final Random rnd = new Random();

    private final TAddress self;
    private final TAddress member;
    private final Logger log;

    private int localRid;
    private HashMap<Integer, TAddress> view;

    public Client(Init init) {
        this.self = init.self;
        this.member = init.member;
        log = LoggerFactory.getLogger("ClntX");
    }

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            viewRequest();
        }
    };

    private Handler<NodeMsg> msgHandler = new Handler<NodeMsg>() {
        @Override
        public void handle(NodeMsg nodeMsg) {
            log.info("Rcvd: " + nodeMsg.toString());
            switch (nodeMsg.cmd) {
                case NodeMsg.VIEW:
                    view = (HashMap<Integer, TAddress>) nodeMsg.body;
                    put("key", "secret cat");
                    put("key2", "secret cat2");
                    break;
                case NodeMsg.PUT_RESPONSE:
                    get("key");
                    get("key2");
                    break;
                case NodeMsg.GET_RESPONSE:
                    break;
            }
        }
    };

    {
        subscribe(startHandler, control);
        subscribe(msgHandler, networkPositive);
    }

    private void viewRequest() {
        send(member, NodeMsg.VIEW_SYNC, NodeMsg.VIEW_REQUEST, -1, null);
    }

    private void put(String key, Serializable value) {
        int groupId = getGroupId(key);
        int nodeId = getRndNode(groupId);
        put(nodeId, groupId, key, value);
    }

    private void get(String key) {
        int groupId = getGroupId(key);
        int nodeId = getRndNode(groupId);
        get(nodeId, groupId, key);
    }

    private int getGroupId(String key) {
        long range = Long.MAX_VALUE / view.size() * 2 + 1;
        return (int) (getHash(key) / range + view.size() / 2);
    }

    private int getRndNode(int groupId) {
        ArrayList<Integer> group = getReplicationGroup(groupId);
        return group.get(rnd.nextInt(group.size()));
    }

    private ArrayList<Integer> getReplicationGroup(int nodeId) {
        ArrayList<Integer> replicationGroup = new ArrayList<>();
        replicationGroup.add(nodeId);
        Iterator it = view.keySet().iterator(); // sorted!
        while ((int) it.next() != nodeId) ;
        for (int i = 0; i < KVStore.REPLICATION_DEGREE - 1; i++) {
            if (!it.hasNext()) {
                it = view.keySet().iterator(); // sorted!
            }
            replicationGroup.add((Integer) it.next());
        }
        return replicationGroup;
    }

    private long getHash(Serializable value) {
        return Hashing.murmur3_128().hashBytes(SerializationUtils.serialize(value)).asLong();
    }

    private void put(int nodeId, int groupId, String key, Serializable value) {
        send(view.get(nodeId), NodeMsg.KV_STORE, NodeMsg.PUT, groupId, Pair.of(key, value));
    }

    private void get(int nodeId, int groupId, String key) {
        send(view.get(nodeId), NodeMsg.KV_STORE, NodeMsg.GET, groupId, key);
    }

    private void send(TAddress dst, byte comp, byte cmd, int inst, Serializable body) {
        NodeMsg nodeMsg = new NodeMsg(self, dst, ++localRid, comp, cmd, inst, body);
        trigger(nodeMsg, networkPositive);
        log.info("Sent: " + nodeMsg.toString());
    }

    public static class Init extends se.sics.kompics.Init<Client> {

        public final TAddress self;
        public final TAddress member;

        public Init(TAddress self, TAddress member) {
            this.self = self;
            this.member = member;
        }
    }
}
