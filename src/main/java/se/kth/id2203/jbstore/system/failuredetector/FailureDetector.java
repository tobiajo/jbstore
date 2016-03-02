package se.kth.id2203.jbstore.system.failuredetector;

import se.kth.id2203.jbstore.system.network.NetMsg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Start;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.util.HashMap;
import java.util.Map;

public class FailureDetector extends ComponentDefinition {

    Negative<FDPort> fdn = provides(FDPort.class);
    Negative<Timer> tn = provides(Timer.class);

    private final TAddress self;
    private final TAddress member;
    private Map<Integer, TAddress> view;

    public FailureDetector(Init init) {
        this.self = init.self;
        this.member = init.member;

        subscribe(startHandler, control);
        subscribe(netMsgHandler, fdn);
    }

    //Needs to send VIEW_REQUEST
    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            trigger(new NetMsg(self, member, 0, NetMsg.VIEW_SYNC, NetMsg.VIEW_REQUEST, null), fdn);
        }
    };

    private Handler<NetMsg> netMsgHandler = new Handler<NetMsg>() {
        @Override
        public void handle(NetMsg netMsg) {
            switch (netMsg.cmd) {
                case NetMsg.VIEW:
                    view = (HashMap<Integer, TAddress>) netMsg.body;
                    //Start timer for heartbeats TODO
                    break;
            }
        }
    };

    public static class Init extends se.sics.kompics.Init<FailureDetector> {

        public final TAddress self;
        public final TAddress member;

        public Init(TAddress self, TAddress member) {
            this.self = self;
            this.member = member;
        }
    }

}
