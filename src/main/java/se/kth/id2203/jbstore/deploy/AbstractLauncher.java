package se.kth.id2203.jbstore.deploy;

import se.kth.id2203.jbstore.system.node.core.event.NodeMsg;
import se.kth.id2203.jbstore.system.node.core.event.NodeMsgSerializer;
import se.sics.kompics.network.netty.serialization.Serializers;
import se.sics.test.NetSerializer;
import se.sics.test.TAddress;
import se.sics.test.THeader;

public abstract class AbstractLauncher {

    static {
        // register
        Serializers.register(new NetSerializer(), "netS");
        Serializers.register(new NodeMsgSerializer(), "msgS");
        // map
        Serializers.register(TAddress.class, "netS");
        Serializers.register(THeader.class, "netS");
        Serializers.register(NodeMsg.class, "msgS");
    }
}
