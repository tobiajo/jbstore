package se.kth.id2203.jbstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.jbstore.network.Msg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

public class Client extends ComponentDefinition {

        private static final Logger LOG = LoggerFactory.getLogger(Node.class);

        Positive<Network> net = requires(Network.class);
        Positive<Timer> timer = requires(Timer.class);

        private long time = 0;
        private final TAddress self;
        private final TAddress member;

        private TAddress[] view;

        public Client(Init init) {
            this.self = init.self;
            this.member = init.member;
        }

        Handler<Start> startHandler = new Handler<Start>() {
            @Override
            public void handle(Start start) {
                Msg msg = new Msg(self, member, ++time, Msg.GET_VIEW, null);
                trigger(msg, net);
                LOG.info("Sent({}): {}, {}, {}", time, msg.header.dst, msg.descString(), msg.body);
            }
        };

        Handler<Msg> msgHandler = new Handler<Msg>() {
            public void handle(Msg msg) {
                time = Math.max(time, msg.time) + 1;
                LOG.info("Received({}): {}, {}, {}", time, msg.header.src, msg.descString(), msg.body);

                switch (msg.desc){
                    case Msg.VALUE:
                        //TODO
                        break;

                    case  Msg.VIEW:
                        view = (TAddress[])msg.body;
                        Msg getMsg = new Msg(self, view[0],++time, Msg.GET, "wontfind");
                        trigger(getMsg, net);
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

        public static class Init extends se.sics.kompics.Init<Client> {

            public final TAddress self;
            public final TAddress member;

            public Init(TAddress self, TAddress member) {
                this.self = self;
                this.member = member;
            }
        }
    }
