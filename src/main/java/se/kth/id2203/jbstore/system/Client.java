package se.kth.id2203.jbstore.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.jbstore.system.application.KVStore;
import se.kth.id2203.jbstore.system.network.event.NodeMsg;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.io.Serializable;
import java.util.HashMap;

public class Client extends ComponentDefinition {

    public final String testString = "Great success";
    private final Positive<Network> networkPositive = requires(Network.class);
    private final Positive<Timer> timerPositive = requires(Timer.class);

    private final TAddress self;
    private final TAddress member;
    private final Logger log;

    private int localRid;
    private HashMap<Integer, TAddress> view;

    public Client(Init init) {
        self = init.self;
        member = init.member;
        log = LoggerFactory.getLogger("ClntX");
    }

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            send(member, NodeMsg.VIEW_SYNC, NodeMsg.VIEW_REQUEST, null);
        }
    };

    private Handler<NodeMsg> msgHandler = new Handler<NodeMsg>() {
        @Override
        public void handle(NodeMsg nodeMsg) {
            log.info("Rcvd: " + nodeMsg.toString());
            switch (nodeMsg.cmd) {
                case NodeMsg.VIEW:
                    put(testString);
                    break;
                case NodeMsg.PUT_RESPONSE:
                    get(KVStore.getKey(testString));
                    get(KVStore.getKey(testString));
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

    private void send(TAddress dst, byte comp, byte cmd, Serializable body) {
        NodeMsg nodeMsg = new NodeMsg(self, dst, comp, cmd, ++localRid, body);
        trigger(nodeMsg, networkPositive);
        log.info("Sent: " + nodeMsg.toString());
    }

    private long put(Serializable value) {
        long key = KVStore.getKey(value);

        // TODO: calc where to go and write to August
        send(member, NodeMsg.KV_STORE, NodeMsg.PUT, value);

        return key;
    }

    private void get(long key) {
        send(member, NodeMsg.KV_STORE, NodeMsg.GET, key);
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
