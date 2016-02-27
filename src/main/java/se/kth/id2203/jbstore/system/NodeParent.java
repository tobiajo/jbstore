package se.kth.id2203.jbstore.system;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

public class NodeParent extends ComponentDefinition {

    public Component node;
    public Channel tmrCh;
    public Channel netCh;

    public NodeParent(Init init) {
        node = create(Node.class, new Node.Init(init.self, init.member, init.id, init.n));
        netCh = connect(node.getNegative(Network.class), requires(Network.class), Channel.TWO_WAY);
        tmrCh = connect(node.getNegative(Timer.class), requires(Timer.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<NodeParent> {

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
