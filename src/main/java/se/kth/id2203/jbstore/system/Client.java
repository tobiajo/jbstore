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

import java.io.Serializable;
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

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            send(member, NetMsg.VIEW_SYNC, NetMsg.GET_VIEW, null);
        }
    };

    private String testString = "Great success";

    private Handler<NetMsg> msgHandler = new Handler<NetMsg>() {
        @Override
        public void handle(NetMsg netMsg) {
            log.info("Rcvd", -1, netMsg.toString());
            switch (netMsg.cmd) {
                case NetMsg.VIEW:
                    System.out.println("Sends put");
                    send(member, NetMsg.KV_STORE, NetMsg.PUT, testString);
                    break;
                case NetMsg.ACK:
                    System.out.println("Sends get");
                    send(member, NetMsg.KV_STORE, NetMsg.GET, KVStore.getKey(testString));
                    break;
                case NetMsg.VALUE:
                    System.out.println(netMsg.body + " !!!");
            }
        }
    };

    private void send(TAddress dst, byte comp, byte cmd, Serializable body) {
        NetMsg netMsg = new NetMsg(self, dst, -1, comp, cmd, body);
        trigger(netMsg, networkPositive);
        log.info("Sent", -1, netMsg.toString());
    }

    public static class Init extends se.sics.kompics.Init<Client> {

        public final TAddress self;
        public final TAddress member;

        public Init(TAddress self, TAddress member) {
            this.self = self;
            this.member = member;
        }
    }
}
