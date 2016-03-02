package se.kth.id2203.jbstore.system.failuredetector;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.network.Network;

import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.test.TAddress;

public class FailureDetectorParent extends ComponentDefinition {


    Component FD;
    Component timer;
    Channel netCh;

    public FailureDetectorParent(Init init){
        FD = create(FailureDetector.class, new FailureDetector.Init(init.self, init.member));
        timer = create(JavaTimer.class, Init.NONE);
        netCh = connect(requires(Network.class), FD.getNegative(Network.class), Channel.TWO_WAY);

        connect(FD.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);

    }

    public static class Init extends se.sics.kompics.Init<FailureDetectorParent> {

        public final TAddress self;
        public final TAddress member;

        public Init(TAddress self, TAddress member) {
            this.self = self;
            this.member = member;
        }
    }
}
