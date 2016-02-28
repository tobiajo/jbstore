package se.kth.id2203.jbstore.system;

import se.kth.id2203.jbstore.system.application.*;
import se.kth.id2203.jbstore.system.application.event.KVStoreRead;
import se.kth.id2203.jbstore.system.application.event.KVStoreValue;
import se.kth.id2203.jbstore.system.application.event.KVStoreWrite;
import se.kth.id2203.jbstore.system.membership.ViewSyncPort;
import se.kth.id2203.jbstore.system.membership.event.*;
import se.kth.id2203.jbstore.system.network.Msg;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.util.HashMap;

public class Node extends ComponentDefinition {

    private Log log;

    private Negative<KVStorePort> kVStore = provides(KVStorePort.class);
    private Negative<ViewSyncPort> viewSync = provides(ViewSyncPort.class);
    private Positive<Network> network = requires(Network.class);
    private Positive<Timer> timer = requires(Timer.class);

    private TAddress self;
    private TAddress member;
    private int id;
    private int n;
    private long time = 0;

    public Node(Init init) {
        subscribe(valueHandler, kVStore);
        subscribe(viewSyncSendHandler, viewSync);
        subscribe(msgHandler, network);
        subscribe(startHandler, control);
        this.self = init.self;
        this.member = init.member;
        this.id = init.id;
        this.n = init.n;
        log = new Log("Node" + id);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            trigger(new ViewSyncInit(n), viewSync);
            if (member != null) {
                //Joining node
                Msg msg = new Msg(self, member, ++time, Msg.JOIN, id);
                trigger(msg, network);
                log.info("Sent", time, msg.toString());
            } else {
                //Creator node
                trigger(new ViewSyncJoin(id, self), viewSync);
            }
        }
    };

    Handler<Msg> msgHandler = new Handler<Msg>() {
        @Override
        public void handle(Msg msg) {
            time = Math.max(time, msg.time) + 1;
            log.info("Rcvd", time, msg.toString());

            switch (msg.desc) {
                case Msg.GET:
                    trigger(new KVStoreRead((String) msg.body, msg.getSource()), kVStore);
                    break;
                case Msg.PUT:
                    trigger(new KVStoreWrite((String) msg.body, msg.getSource(), msg.body), kVStore);
                    break;
                case Msg.JOIN:
                    trigger(new ViewSyncJoin((Integer) msg.body, msg.getSource()), viewSync);
                    break;
                case Msg.VIEW:
                    //trigger(new KVStoreInit((Set<Integer>) msg.body), kVStore);
                    trigger(new ViewSyncView((HashMap<Integer, TAddress>) msg.body), viewSync);
                    break;
                case Msg.GET_VIEW:
                    trigger(new ViewSyncGetView(msg.getSource()), viewSync);
                    break;
                case Msg.KV_STORE:
                    trigger(new ViewSyncDeliver(msg.body), viewSync);
                    break;
            }
        }
    };

    Handler<ViewSyncSend> viewSyncSendHandler = new Handler<ViewSyncSend>(){
        @Override
        public void handle(ViewSyncSend event) {
            Msg msg = new Msg(self, event.dst, ++time, event.desc, event.body);
            trigger(msg, network);
            log.info("Sent", ++time, msg.toString());
        }
    };

    Handler<KVStoreValue> valueHandler = new Handler<KVStoreValue>(){
        @Override
        public void handle(KVStoreValue value) {
            Msg msg = new Msg(self, value.src, ++time, Msg.VALUE, value.value);
            trigger(msg, network);
            log.info("Sent", time, msg.toString());
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
