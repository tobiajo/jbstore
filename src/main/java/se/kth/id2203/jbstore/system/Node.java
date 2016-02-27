package se.kth.id2203.jbstore.system;

import se.kth.id2203.jbstore.system.application.KVStore;
import se.kth.id2203.jbstore.system.application.KVStorePort;
import se.kth.id2203.jbstore.system.network.Msg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.util.HashMap;
import java.util.Map;

public class Node extends ComponentDefinition {

    private final Log log;
    private final Positive<Network> net = requires(Network.class);
    private final Positive<Timer> timer = requires(Timer.class);
    Positive<KVStorePort> kvp = requires(KVStorePort.class);
    private final KVStore store = new KVStore();
    private final TAddress self;
    private final TAddress member;
    private final int id;
    private final int n;
    private HashMap<Integer, TAddress> view;

    private long time = 0;

    public Node(Init init) {
        subscribe(startHandler, control);
        subscribe(msgHandler, net);
        subscribe(valueHandler, kvp);
        this.self = init.self;
        this.member = init.member;
        this.id = init.id;
        this.n = init.n;
        log = new Log("Node" + id);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            if (member != null) {
                //Joining node
                Msg msg = new Msg(self, member, ++time, Msg.JOIN, id);
                trigger(msg, net);
                log.info("Sent", time, msg.toString());
            } else {
                //Creator node
                view = new HashMap<>();
                view.put(id, self);
            }

        }
    };

    Handler<Msg> msgHandler = new Handler<Msg>() {
        public void handle(Msg msg) {
            Msg response;
            time = Math.max(time, msg.time) + 1;
            log.info("Rcvd", time, msg.toString());

            switch (msg.desc) {
                case Msg.GET:
                    trigger(new KVStorePort.Read((String) msg.body, msg.getSource()), kvp);
                    break;
                case Msg.PUT:
                    trigger(new KVStorePort.Write((String) msg.body, msg.getSource(), msg.body), kvp);
                    break;
                case Msg.JOIN:
                    view.put((Integer) msg.body, msg.getSource());
                    if (view.size() == n) {
                        for (Map.Entry<Integer, TAddress> entry : view.entrySet()) {
                            response = new Msg(self, entry.getValue(), time, Msg.VIEW, view);
                            trigger(response, net);
                            log.info("Sent", time, response.toString());
                        }
                    }
                    break;
                case Msg.VIEW:
                    view = (HashMap<Integer, TAddress>) msg.body;
                    trigger(new KVStorePort.Init(view), kvp);
                    break;
                case Msg.GET_VIEW:
                    response = new Msg(self, msg.getSource(), ++time, Msg.VIEW, view);
                    trigger(response, net);
                    log.info("Sent", time, response.toString());
                    break;
            }
        }
    };

    Handler<KVStorePort.Value> valueHandler = new Handler<KVStorePort.Value>(){
        public void handle(KVStorePort.Value value) {
            Msg response = new Msg(self, value.src, ++time, Msg.VALUE, value.value);
            trigger(response, net);
            log.info("Sent", time, response.toString());
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
