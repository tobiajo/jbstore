package se.kth.id2203.jbstore.system;

import se.kth.id2203.jbstore.system.node.sub.application.KVStore;
import se.kth.id2203.jbstore.system.node.sub.application.KVStorePort;
import se.kth.id2203.jbstore.system.node.sub.failuredetector.EPFD;
import se.kth.id2203.jbstore.system.node.sub.failuredetector.EPFDPort;
import se.kth.id2203.jbstore.system.node.sub.membership.ViewSync;
import se.kth.id2203.jbstore.system.node.sub.membership.ViewSyncPort;
import se.kth.id2203.jbstore.system.node.core.Node;
import se.kth.id2203.jbstore.system.node.core.NodePort;
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

        Component node = create(Node.class, new Node.Init(init.self, init.member, init.id, init.n));
        if (init.deploy) {
            connect(node.getNegative(Network.class), create(NettyNetwork.class, new NettyInit(init.self)).getPositive(Network.class), Channel.TWO_WAY);
        } else {
            connect(node.getNegative(Network.class), requires(Network.class), Channel.TWO_WAY);                         // Node <-> Network
        }

        Component viewSync = create(ViewSync.class, se.sics.kompics.Init.NONE);
        connect(viewSync.getNegative(ViewSyncPort.class), node.getPositive(ViewSyncPort.class), Channel.ONE_WAY_NEG);   // Node --> ViewSync
        connect(viewSync.getNegative(NodePort.class), node.getPositive(NodePort.class), Channel.ONE_WAY_POS);           // Node <-- ViewSync

        Component epfd = create(EPFD.class, se.sics.kompics.Init.NONE);
        if (init.deploy) {
            connect(epfd.getNegative(Timer.class), create(JavaTimer.class, Init.NONE).getPositive(Timer.class), Channel.TWO_WAY);
        } else {
            connect(epfd.getNegative(Timer.class), requires(Timer.class), Channel.TWO_WAY);                             // EPDF <-> Timer
        }
        connect(epfd.getNegative(EPFDPort.class), node.getPositive(EPFDPort.class), Channel.ONE_WAY_NEG);               // EPDF <-- Node
        connect(epfd.getNegative(NodePort.class), node.getPositive(NodePort.class), Channel.ONE_WAY_POS);               // EPDF --> Node
        connect(epfd.getNegative(EPFDPort.class), viewSync.getPositive(EPFDPort.class), Channel.TWO_WAY);               // EPDF <-> ViewSync

        Component kvStore = create(KVStore.class, se.sics.kompics.Init.NONE);
        connect(kvStore.getNegative(KVStorePort.class), node.getPositive(KVStorePort.class), Channel.ONE_WAY_NEG);      // KVStore <-- Node
        connect(kvStore.getNegative(NodePort.class), node.getPositive(NodePort.class), Channel.ONE_WAY_POS);            // KVStore --> Node
        connect(kvStore.getNegative(KVStorePort.class), viewSync.getPositive(KVStorePort.class), Channel.ONE_WAY_NEG);  // KVStore <-- ViewSync
    }

    public static class Init extends se.sics.kompics.Init<NodeParent> {

        public final boolean deploy;
        public final TAddress self;
        public final TAddress member;
        public final int id;
        public final int n;

        public Init(boolean deploy, TAddress self, TAddress member, int id, int n) {
            this.deploy = deploy;
            this.self = self;
            this.member = member;
            this.id = id;
            this.n = n;
        }
    }
}
