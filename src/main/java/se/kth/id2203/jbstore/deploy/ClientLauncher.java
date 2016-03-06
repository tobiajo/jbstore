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
                Kompics.createAndStart(ClientParent.class, new ClientParent.Init(true, self, member));
                System.out.println("Starting client at " + self);
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
