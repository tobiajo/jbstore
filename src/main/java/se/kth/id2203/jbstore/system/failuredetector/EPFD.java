package se.kth.id2203.jbstore.system.failuredetector;

import se.kth.id2203.jbstore.system.failuredetector.event.EPFDInit;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDRestore;
import se.kth.id2203.jbstore.system.failuredetector.event.EPFDSuspect;
import se.kth.id2203.jbstore.system.network.NetMsg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.util.HashSet;

public class EPFD extends ComponentDefinition {

    private final Negative<EPFDPort> epfdPortNegative = provides(EPFDPort.class);
    private final Positive<Timer> timerPositive = requires(Timer.class);

    private TAddress self;
    private HashSet<TAddress> nodesToMonitor;

    public EPFD() {
        subscribe(netMsgHandler, epfdPortNegative);
        subscribe(epfdInitHandler, epfdPortNegative);
    }

    private Handler<EPFDInit> epfdInitHandler = new Handler<EPFDInit>() {
        @Override
        public void handle(EPFDInit epfdInit) {
            self = epfdInit.self;
            nodesToMonitor = epfdInit.nodesToMonitor;
            System.out.println(self + ": epfdInitHandler called: " + nodesToMonitor);
            trigger(new EPFDSuspect(), epfdPortNegative);
            trigger(new EPFDRestore(), epfdPortNegative);
        }
    };

    private Handler<NetMsg> netMsgHandler = new Handler<NetMsg>() {
        @Override
        public void handle(NetMsg netMsg) {
            switch (netMsg.cmd) {
                case NetMsg.VIEW:
                    break;
            }
        }
    };
}
