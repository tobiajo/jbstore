package se.kth.id2203.jbstore.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.jbstore.system.application.KVStorePort;
import se.kth.id2203.jbstore.system.membership.ViewSyncPort;
import se.kth.id2203.jbstore.system.membership.event.*;
import se.kth.id2203.jbstore.system.network.NetMsg;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

public class Node extends ComponentDefinition {

    private final Negative<KVStorePort> kvStorePortNegative = provides(KVStorePort.class);
    private final Negative<ViewSyncPort> viewSyncPortNegative = provides(ViewSyncPort.class);
    private final Positive<Network> networkPositive = requires(Network.class);
    private final Positive<Timer> timerPositive = requires(Timer.class);

    private final TAddress self;
    private final TAddress member;
    private final int id;
    private final int n;
    private final Logger log;

    public Node(Init init) {
        subscribe(netMsgOutHandler, kvStorePortNegative);
        subscribe(netMsgOutHandler, viewSyncPortNegative);
        subscribe(netMsgInHandler, networkPositive);
        subscribe(startHandler, control);
        this.self = init.self;
        this.member = init.member;
        this.id = init.id;
        this.n = init.n;
        log = LoggerFactory.getLogger("Node" + id);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            trigger(new ViewSyncInit(self, member, id, n), viewSyncPortNegative);
        }
    };

    Handler<NetMsg> netMsgInHandler = new Handler<NetMsg>() {
        @Override
        public void handle(NetMsg netMsg) {
            log.info("Rcvd: " + netMsg.toString());
            switch (netMsg.comp) {
                case NetMsg.VIEW_SYNC:
                    trigger(netMsg, viewSyncPortNegative);
                    break;
                case NetMsg.KV_STORE:
                    trigger(netMsg, kvStorePortNegative);
                    break;
            }
        }
    };

    Handler<NetMsg> netMsgOutHandler = new Handler<NetMsg>() {
        @Override
        public void handle(NetMsg netMsg) {
            trigger(netMsg, networkPositive);
            log.info("Sent: " + netMsg.toString());
        }
    };

    public static class Init extends se.sics.kompics.Init<Node> {

        public final TAddress self;
        public final TAddress member;
        public final int id;
        public final int n;

        public Init(TAddress self, TAddress member, int id, int n) {
            this.self = self;
            this.member = member;
            this.id = id;
            this.n = n;
        }
    }
}
