package se.kth.id2203.jbstore.system;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

public class ClientParent extends ComponentDefinition {

    Component node;
    Channel tmrCh;
    Channel netCh;

    public ClientParent(Init init) {
        node = create(Client.class, new Client.Init(init.self, init.member));
        netCh = connect(node.getNegative(Network.class), requires(Network.class), Channel.TWO_WAY);
        tmrCh = connect(node.getNegative(Timer.class), requires(Timer.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<ClientParent> {

        public final TAddress self;
        public final TAddress member;

        public Init(TAddress self, TAddress member) {
            this.self = self;
            this.member = member;
        }
    }
}
