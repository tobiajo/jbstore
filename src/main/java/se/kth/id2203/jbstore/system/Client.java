package se.kth.id2203.jbstore.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.jbstore.system.network.Msg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.util.HashMap;

public class Client extends ComponentDefinition {

    private final Logger logger;
    private final Positive<Network> net = requires(Network.class);
    private final Positive<Timer> timer = requires(Timer.class);
    private final TAddress self;
    private final TAddress member;
    private long time = 0;

    private HashMap<Integer, TAddress> view;

    public Client(Init init) {
        this.self = init.self;
        this.member = init.member;
        logger = LoggerFactory.getLogger("Clnt0");
        subscribe(startHandler, control);
        subscribe(msgHandler, net);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            Msg msg = new Msg(self, member, ++time, Msg.GET_VIEW, null);
            trigger(msg, net);
            msg.log(logger, "Sent");
        }
    };

    Handler<Msg> msgHandler = new Handler<Msg>() {
        public void handle(Msg msg) {
            time = Math.max(time, msg.time) + 1;
            msg.log(logger, "Rcvd");

            switch (msg.desc) {
                case Msg.VALUE:
                    //TODO
                    break;

                case Msg.VIEW:
                    view = (HashMap<Integer, TAddress>) msg.body;
                    Msg getMsg = new Msg(self, view.get(1), ++time, Msg.GET, "wontfind");
                    trigger(getMsg, net);
                    break;
            }
        }
    };


    public static class Init extends se.sics.kompics.Init<Client> {

        public final TAddress self;
        public final TAddress member;

        public Init(TAddress self, TAddress member) {
            this.self = self;
            this.member = member;
        }
    }
}
