package se.kth.id2203.jbstore.deploy;

import se.kth.id2203.jbstore.system.NodeParent;
import se.sics.kompics.Kompics;
import se.sics.test.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeLauncher extends AbstractLauncher {

    public static void main(String[] args) {
        try {
            if (args.length == 5) { // start creator
                TAddress self = new TAddress(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
                int id = Integer.parseInt(args[2]);
                int n = Integer.parseInt(args[3]);
                System.out.println("Node" + id + ": starting (creator) at " + self + " for " + args[4] + " s");
                Kompics.createAndStart(NodeParent.class, new NodeParent.Init(true, self, null, id, n));
                try {
                    Thread.sleep(Integer.parseInt(args[4]) * 1000);
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                    System.exit(1);
                }
                System.out.println("Node" + id + ": shutting down (creator) at " + self);
                Kompics.shutdown();
                System.exit(0);
            } else if (args.length == 7) { // start joiner
                TAddress self = new TAddress(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
                TAddress member = new TAddress(InetAddress.getByName(args[2]), Integer.parseInt(args[3]));
                int id = Integer.parseInt(args[4]);
                int n = Integer.parseInt(args[5]);
                System.out.println("Node" + id + ": starting (joiner) at " + self + " for " + args[6] + " s");
                Kompics.createAndStart(NodeParent.class, new NodeParent.Init(true, self, member, id, n));
                try {
                    Thread.sleep(Integer.parseInt(args[6]) * 1000);
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                    System.exit(1);
                }
                System.out.println("Node" + id + ": shutting down (joiner) at " + self);
                Kompics.shutdown();
                System.exit(0);
            } else {
                System.err.println("Invalid number of parameters");
                System.exit(1);
            }

        } catch (UnknownHostException ex) {
            System.err.println(ex);
            System.exit(1);
        }
    }
}
