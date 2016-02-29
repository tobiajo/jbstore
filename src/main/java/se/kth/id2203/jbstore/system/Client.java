package se.kth.id2203.jbstore.system;

import se.kth.id2203.jbstore.system.application.KVStore;
import se.kth.id2203.jbstore.system.network.NetMsg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.TAddress;

import java.util.HashMap;

public class Client extends ComponentDefinition {

    private final Log log;
    private final Positive<Network> networkPositive = requires(Network.class);
    private final Positive<Timer> timerPositive = requires(Timer.class);
    private final TAddress self;
    private final TAddress member;

    private HashMap<Integer, TAddress> view;

    public Client(Init init) {
        this.self = init.self;
        this.member = init.member;
        log = new Log("Clnt0");
        subscribe(startHandler, control);
        subscribe(msgHandler, networkPositive);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            NetMsg netMsg = new NetMsg(self, member, -1, NetMsg.VIEW_SYNC, NetMsg.GET_VIEW, null);
            trigger(netMsg, networkPositive);
            log.info("Sent", -1, netMsg.toString());
        }
    };

    String string = "Great success";

    Handler<NetMsg> msgHandler = new Handler<NetMsg>() {
        public void handle(NetMsg netMsg) {
            log.info("Rcvd", -1, netMsg.toString());

            switch (netMsg.cmd) {
                case NetMsg.VIEW:
                    System.out.println("Sends put");
                    trigger(new NetMsg(self, member, -1, NetMsg.KV_STORE, NetMsg.PUT, string), networkPositive);
                    break;
                case NetMsg.ACK:
                    System.out.println("Sends get");
                    trigger(new NetMsg(self, member, -1, NetMsg.KV_STORE, NetMsg.GET, KVStore.getHash(string)), networkPositive);
                    break;
                case NetMsg.VALUE:
                    System.out.println(netMsg.body + " !!!");
            }
        }
    };


    public static class Init extends se.sics.kompics.Init<Client> {

        public final TAddress self;
        public final TAddress member;

        public Init(TAddress self, TAddress member) {
            this.self = self;
            this.member = member;
        }
    }
}
