package se.kth.id2203.jbstore.deploy;

import se.kth.id2203.jbstore.system.NodeParent;
import se.kth.id2203.jbstore.system.network.event.NodeMsg;
import se.kth.id2203.jbstore.system.network.event.NodeMsgSerializer;
import se.sics.kompics.Kompics;
import se.sics.kompics.network.netty.serialization.Serializers;
import se.sics.test.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeLauncher {

    static {
        // register
        Serializers.register(new NetSerializer(), "netS");
        Serializers.register(new NodeMsgSerializer(), "msgS");
        // map
        Serializers.register(TAddress.class, "netS");
        Serializers.register(THeader.class, "netS");
        Serializers.register(NodeMsg.class, "msgS");
    }

    public static void main(String[] args) {
        try {
            if (args.length == 2) { // start creator
                TAddress self = new TAddress(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
                Kompics.createAndStart(NodeParent.class, new NodeParent.Init(true, self, null, 0, 2));
                System.out.println("Starting creator at " + self);
            } else if (args.length == 4) { // start joiner
                TAddress self = new TAddress(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
                TAddress member = new TAddress(InetAddress.getByName(args[2]), Integer.parseInt(args[3]));
                Kompics.createAndStart(NodeParent.class, new NodeParent.Init(true, self, member, 1, 2));
            } else {
                System.err.println("Invalid number of parameters (2 for creator, 4 for joiner)");
                System.exit(1);
            }

        } catch (UnknownHostException ex) {
            System.err.println(ex);
            System.exit(1);
        }
    }
}
