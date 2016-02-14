package se.kth.id2203.jbstore;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.test.TAddress;

public class NodeParent extends ComponentDefinition {

    public NodeParent(Init init) {
        Component timer = create(JavaTimer.class, Init.NONE);
        Component network = create(NettyNetwork.class, new NettyInit(init.self));
        Component node = create(Node.class, new Node.Init(init.self, init.member));

        connect(node.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);

        connect(node.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<NodeParent> {

        public final TAddress self;
        public final TAddress member;

        public Init(TAddress self, TAddress member) {
            this.self = self;
            this.member = member;
        }
    }
}
