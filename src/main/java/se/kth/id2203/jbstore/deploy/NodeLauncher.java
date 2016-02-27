package se.kth.id2203.jbstore.deploy;

import se.kth.id2203.jbstore.system.network.Msg;
import se.kth.id2203.jbstore.system.network.MsgSerializer;
import se.sics.kompics.Kompics;
import se.sics.kompics.network.netty.serialization.Serializers;
import se.sics.test.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeLauncher {

    static {
        // register
        Serializers.register(new NetSerializer(), "netS");
        Serializers.register(new MsgSerializer(), "msgS");
        // map
        Serializers.register(TAddress.class, "netS");
        Serializers.register(THeader.class, "netS");
        Serializers.register(Msg.class, "msgS");
    }

    public static void main(String[] args) {
        try {
            if (args.length == 2) { // start creator
                InetAddress ip = InetAddress.getByName(args[0]);
                int port = Integer.parseInt(args[1]);
                TAddress self = new TAddress(ip, port);
                Kompics.createAndStart(NodeHost.class, new NodeHost.Init(self, null, 0, 2), 1);
                System.out.println("Starting creator at " + self);
                // no shutdown this time...act like a server and keep running until externally exited
            } else if (args.length == 4) { // start joiner
                InetAddress myIp = InetAddress.getByName(args[0]);
                int myPort = Integer.parseInt(args[1]);
                TAddress self = new TAddress(myIp, myPort);
                InetAddress memberIp = InetAddress.getByName(args[2]);
                int memberPort = Integer.parseInt(args[3]);
                TAddress member = new TAddress(memberIp, memberPort);
                Kompics.createAndStart(NodeHost.class, new NodeHost.Init(self, member, 1, 2), 2);
                System.out.println("Starting joiner at" + self + " to " + member);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    System.exit(1);
                }
                Kompics.shutdown();
                System.exit(0);
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
