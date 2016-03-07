package se.kth.id2203.jbstore.system;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.test.TAddress;

public class ClientParent extends ComponentDefinition {

    public ClientParent(Init init) {

        Component node = create(Client.class, new Client.Init(init.self, init.member));

        if (init.deploy) {
            connect(node.getNegative(Network.class), create(NettyNetwork.class, new NettyInit(init.self)).getPositive(Network.class), Channel.TWO_WAY);
            connect(node.getNegative(Timer.class), create(JavaTimer.class, Init.NONE).getPositive(Timer.class), Channel.TWO_WAY);
        } else {
            connect(node.getNegative(Network.class), requires(Network.class), Channel.TWO_WAY);
            connect(node.getNegative(Timer.class), requires(Timer.class), Channel.TWO_WAY);
        }


    }

    public static class Init extends se.sics.kompics.Init<ClientParent> {

        public final boolean deploy;
        public final TAddress self;
        public final TAddress member;

        public Init(boolean deploy, TAddress self, TAddress member) {
            this.deploy = deploy;
            this.self = self;
            this.member = member;
        }
    }
}
