package se.kth.id2203.jbstore.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Positive<Network> networkPositive = requires(Network.class);
    private final Positive<Timer> timerPositive = requires(Timer.class);

    private final TAddress self;
    private final TAddress member;
    private final Logger log;

    private int localRid;
    private HashMap<Integer, TAddress> view;

    public Client(Init init) {
        subscribe(startHandler, control);
        subscribe(msgHandler, networkPositive);
        this.self = init.self;
        this.member = init.member;
        log = LoggerFactory.getLogger("ClntX");
    }

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            //request(member, NetMsg.VIEW_SYNC, NetMsg.VIEW_REQUEST, null);
        }
    };

    private String testString = "Great success";

    private Handler<NetMsg> msgHandler = new Handler<NetMsg>() {
        @Override
        public void handle(NetMsg netMsg) {
            log.info("Rcvd: " + netMsg.toString());
            switch (netMsg.cmd) {
                case NetMsg.VIEW:
                    request(member, NetMsg.KV_STORE, NetMsg.PUT, testString);
                    break;
                case NetMsg.PUT_RESPONSE:
                    request(member, NetMsg.KV_STORE, NetMsg.GET, KVStore.getKey(testString));
                    request(member, NetMsg.KV_STORE, NetMsg.GET, KVStore.getKey(testString));
                    break;
                case NetMsg.GET_RESPONSE:
                    break;
            }
        }
    };

    private void request(TAddress dst, byte comp, byte cmd, Serializable body) {
        NetMsg netMsg = new NetMsg(self, dst, ++localRid, comp, cmd, body);
        trigger(netMsg, networkPositive);
        log.info("Sent: " + netMsg.toString());
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
