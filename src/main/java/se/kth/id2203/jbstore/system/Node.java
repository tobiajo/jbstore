package se.kth.id2203.jbstore.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.jbstore.system.application.KVStore;
import se.kth.id2203.jbstore.system.network.Msg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Node extends ComponentDefinition {

    private final Log log;
    private final Positive<Network> net = requires(Network.class);
    private final Positive<Timer> timer = requires(Timer.class);
    private final KVStore store = new KVStore();
    private final TAddress self;
    private final TAddress member;
    private final int id;
    private final int n;
    private HashMap<Integer, TAddress> view;

    private long time = 0;
    private int seen = 0;


    public Node(Init init) {
        this.self = init.self;
        this.member = init.member;
        this.id = init.id;
        this.n = init.n;
        log = new Log("Node" + id);
        subscribe(startHandler, control);
        subscribe(msgHandler, net);
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
                    String getKey = (String) msg.body;
                    Serializable value = store.get(getKey);
                    response = new Msg(self, msg.getSource(), ++time, Msg.VALUE, value);
                    trigger(response, net);
                    log.info("Sent", time, response.toString());
                    break;
                case Msg.PUT:
                    Serializable[] keyValue = (Serializable[]) msg.body;
                    String putKey = (String) keyValue[0];
                    store.put(putKey, keyValue[1]);
                    break;
                case Msg.VALUE:
                    //TODO
                    break;
                case Msg.JOIN:
                    seen++;
                    view.put((Integer) msg.body, msg.getSource());
                    //view[seen] = msg.getSource();
                    if (seen == n - 1) {
                        for (Map.Entry<Integer, TAddress> entry : view.entrySet()) {
                            response = new Msg(self, entry.getValue(), time, Msg.VIEW, view);
                            trigger(response, net);
                            log.info("Sent", time, response.toString());
                        }
                    }
                    break;
                case Msg.VIEW:
                    view = (HashMap<Integer, TAddress>) msg.body;
                    break;
                case Msg.GET_VIEW:
                    response = new Msg(self, msg.getSource(), ++time, Msg.VIEW, view);
                    trigger(response, net);
                    log.info("Sent", time, response.toString());
                    break;
            }
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
