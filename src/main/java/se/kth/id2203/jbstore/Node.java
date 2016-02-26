package se.kth.id2203.jbstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.jbstore.application.KVStore;
import se.kth.id2203.jbstore.network.Msg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.io.Serializable;

public class Node extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(Node.class);

    Positive<Network> net = requires(Network.class);
    Positive<Timer> timer = requires(Timer.class);

    private long time = 0;
    private final TAddress self;
    private final TAddress member;

    private final KVStore store;

    private final int N = 5;
    private int seen = 0;
    private TAddress[] view = new TAddress[N];


    public Node(Init init) {
        this.self = init.self;
        this.member = init.member;
        this.store = new KVStore();
        view[0] = this.self;
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            if (member != null) {
                //Joining node
                Msg msg = new Msg(self, member, ++time, Msg.JOIN, "Let me in");
                trigger(msg, net);


                LOG.info("Sent({}): {}, {}, {}", time, msg.header.dst, msg.descString(), msg.body);
            } else {
                //Creator node
            }

        }
    };
    Handler<Msg> msgHandler = new Handler<Msg>() {
        public void handle(Msg msg) {
            time = Math.max(time, msg.time) + 1;
            LOG.info("Received({}): {}, {}, {}", time, msg.header.src, msg.descString(), msg.body);

            switch (msg.desc){
                case Msg.GET:
                    String getKey = (String)msg.body;
                    Serializable value = store.GET(getKey);
                    Msg response = new Msg(self, msg.getSource(),++time, Msg.VALUE, value);
                    trigger(response, net);
                    break;
                case Msg.PUT:
                    Serializable[] keyValue = (Serializable[])msg.body;
                    String putKey = (String) keyValue[0];
                    store.PUT(putKey, keyValue[1]);
                    break;
                case Msg.VALUE:
                    //TODO
                    break;
                case Msg.JOIN:
                    seen++;
                    view[seen] = msg.getSource();
                    if (seen == N-1){
                        for (TAddress member : view){
                            Msg viewMsg = new Msg(self, member, time, Msg.VIEW, view);
                            trigger(viewMsg, net);
                        }
                    }
                    break;
                case  Msg.VIEW:
                    view = (TAddress[])msg.body;
                    break;
                case Msg.GET_VIEW:
                    Msg viewMsg = new Msg(self, msg.getSource(), ++time, Msg.VIEW, view);
                    trigger(viewMsg, net);
                    break;

                default:
                    throw new RuntimeException("Unexpected message description found");

            }
        }
    };

    {
        subscribe(startHandler, control);
        subscribe(msgHandler, net);
    }

    public static class Init extends se.sics.kompics.Init<Node> {

        public final TAddress self;
        public final TAddress member;

        public Init(TAddress self, TAddress member) {
            this.self = self;
            this.member = member;
        }
    }
}
