package se.kth.id2203.jbstore.system.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.jbstore.system.application.KVStorePort;
import se.kth.id2203.jbstore.system.failuredetector.EPFDPort;
import se.kth.id2203.jbstore.system.membership.ViewSyncPort;
import se.kth.id2203.jbstore.system.membership.event.*;
import se.kth.id2203.jbstore.system.network.event.NodeMsg;
import se.kth.id2203.jbstore.system.network.event.NodeMsgSend;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.test.TAddress;

public class Node extends ComponentDefinition {

    private final Positive<Network> networkPositive = requires(Network.class);
    private final Negative<NodePort> nodePortNegative = provides(NodePort.class);
    private final Negative<ViewSyncPort> viewSyncPortNegative = provides(ViewSyncPort.class);
    private final Negative<EPFDPort> epfdPortNegative = provides(EPFDPort.class);
    private final Negative<KVStorePort> kvStorePortNegative = provides(KVStorePort.class);

    private final TAddress self;
    private final TAddress member;
    private final int id;
    private final int n;
    private final Logger log;

    public Node(Init init) {
        self = init.self;
        member = init.member;
        id = init.id;
        n = init.n;
        log = LoggerFactory.getLogger("Node" + id);
    }

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            trigger(new ViewSyncInit(self, member, id, n), viewSyncPortNegative);
        }
    };

    private Handler<NodeMsg> netMsgDeliverHandler = new Handler<NodeMsg>() {
        @Override
        public void handle(NodeMsg nodeMsg) {
            log.info("Rcvd: " + nodeMsg.toString());
            switch (nodeMsg.comp) {
                case NodeMsg.VIEW_SYNC:
                    trigger(nodeMsg, viewSyncPortNegative);
                    break;
                case NodeMsg.EPFD:
                    trigger(nodeMsg, epfdPortNegative);
                    break;
                case NodeMsg.KV_STORE:
                    trigger(nodeMsg, kvStorePortNegative);
                    break;
            }
        }
    };

    private Handler<NodeMsgSend> netMsgSendHandler = new Handler<NodeMsgSend>() {
        @Override
        public void handle(NodeMsgSend event) {
            NodeMsg nodeMsg = new NodeMsg(self, event.dst, event.comp, event.cmd, event.rid, event.body);
            trigger(nodeMsg, networkPositive);
            log.info("Sent: " + nodeMsg.toString());
        }
    };

    {
        subscribe(startHandler, control);
        subscribe(netMsgDeliverHandler, networkPositive);
        subscribe(netMsgSendHandler, nodePortNegative);
    }

    public static class Init extends se.sics.kompics.Init<Node> {

        public final TAddress self;
        public final TAddress member;
        public final int id;
        public final int n;

        public Init(TAddress self, TAddress member, int id, int n) {
            this.self = self;
            this.member = member;
            this.id = id;
            this.n = n;
        }
    }
}
