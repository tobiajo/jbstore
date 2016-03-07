package se.kth.id2203.jbstore.system;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.jbstore.Util;
import se.kth.id2203.jbstore.system.node.sub.application.KVStore;
import se.kth.id2203.jbstore.system.node.core.event.NodeMsg;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.io.Serializable;
import java.util.*;

public class Client extends ComponentDefinition {

    private List<TimeStamp> history = new ArrayList<>();
    private final Negative<InputGenPort> inputPortNegative = provides(InputGenPort.class);

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
                    recordEvent("got view");
                    trigger(new InputGenPort.Init(), inputPortNegative);
                    break;
                case NodeMsg.PUT_RESPONSE:
                    recordEvent("PUT_RESPONSE: " + nodeMsg.body);
                    break;
                case NodeMsg.GET_RESPONSE:
                    recordEvent("GET_RESPONSE: " + nodeMsg.body);
                    break;
            }
        }
    };

    private Handler<InputGenPort.Request> inputHandler = new Handler<InputGenPort.Request>() {
        @Override
        public void handle(InputGenPort.Request request) {
            switch (request.type) {
                case GET:
                    System.out.println("boo");
                    get(request.key);
                    break;
                case PUT:
                    System.out.println("boo1");
                    //put(request.key, request.value);
                    break;
                case HISTORY:
                    System.out.println("boo2");
                    /*for (TimeStamp time : history){
                        log.info(time.toString());
                    }*/
                    break;
            }
        }
    };

    {
        subscribe(startHandler, control);
        subscribe(msgHandler, networkPositive);
        subscribe(inputHandler, inputPortNegative);
    }

    // Client interface

    private void viewRequest() {
        send(member, NodeMsg.VIEW_SYNC, NodeMsg.VIEW_REQUEST, 0, null);
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

    // Helper methods

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

    private int getGroupId(String key) {
        long range = Long.MAX_VALUE / view.size() * 2 + 1;
        return (int) (Util.getHash(key) / range + view.size() / 2);
    }

    private int getRndNode(int groupId) {
        LinkedList<Integer> group = getReplicationGroup(groupId);
        return group.get(rnd.nextInt(group.size()));
    }

    private LinkedList<Integer> getReplicationGroup(int nodeId) {
        LinkedList<Integer> group = new LinkedList<>();
        group.add(nodeId);
        ListIterator<Integer> it = Util.getSortedList(view.keySet()).listIterator();
        while (it.next() != nodeId) ;
        for (int i = 0; i < KVStore.REPLICATION_DEGREE - 1; i++) {
            if (!it.hasNext()) {
                it = Util.getSortedList(view.keySet()).listIterator();
            }
            group.add(it.next());
        }
        return group;
    }

    // Static nested class <- you don't say ;D

    public static class Init extends se.sics.kompics.Init<Client> {

        public final TAddress self;
        public final TAddress member;

        public Init(TAddress self, TAddress member) {
            this.self = self;
            this.member = member;
        }
    }

    private void recordEvent(String event) {
        this.history.add(new TimeStamp(new Date(System.nanoTime()), event));
    }

    private static class TimeStamp {
        public final Date date;
        public final String event;

        public TimeStamp(Date date, String event) {
            this.date = date;
            this.event = event;
        }

        @Override
        public String toString() {
            return this.date.toString() + ": " + this.event;
        }

    }
}
