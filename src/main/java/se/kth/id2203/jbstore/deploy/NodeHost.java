package se.kth.id2203.jbstore.deploy;

import se.kth.id2203.jbstore.system.NodeParent;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

public class NodeHost extends NodeParent {

    public NodeHost(Init init) {
        super(init);
        disconnect(netCh);
        disconnect(tmrCh);

        Component net = create(NettyNetwork.class, new NettyInit(init.self));
        Component tmr = create(JavaTimer.class, Init.NONE);
        connect(node.getNegative(Network.class), net.getPositive(Network.class), Channel.TWO_WAY);
        connect(node.getNegative(Timer.class), tmr.getPositive(Timer.class), Channel.TWO_WAY);
    }
}
