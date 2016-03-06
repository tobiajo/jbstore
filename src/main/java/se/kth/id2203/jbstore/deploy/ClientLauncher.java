package se.kth.id2203.jbstore.deploy;

import se.kth.id2203.jbstore.system.ClientParent;
import se.sics.kompics.Kompics;
import se.sics.test.TAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientLauncher extends AbstractLauncher {

    public static void main(String[] args) {
        try {
            if (args.length == 4) {
                TAddress self = new TAddress(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
                TAddress member = new TAddress(InetAddress.getByName(args[2]), Integer.parseInt(args[3]));
                System.out.println("Starting client at " + self + " for 10 s");
                Kompics.createAndStart(ClientParent.class, new ClientParent.Init(true, self, member));
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                    System.exit(1);
                }
                System.out.println("Shutting down client at " + self);
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
