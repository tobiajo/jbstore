package se.kth.id2203.jbstore.system.membership;

import se.kth.id2203.jbstore.system.application.KVStoreMsg;
import se.kth.id2203.jbstore.system.application.KVStorePort;
import se.kth.id2203.jbstore.system.application.event.KVStoreInit;
import se.kth.id2203.jbstore.system.membership.event.*;
import se.kth.id2203.jbstore.system.network.Msg;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.test.TAddress;

import java.util.HashMap;
import java.util.Map;

public class ViewSync extends ComponentDefinition {

    private Negative<KVStorePort> kVStore = provides(KVStorePort.class);
    private Positive<ViewSyncPort> node = requires(ViewSyncPort.class);
    private HashMap<Integer, TAddress> view = new HashMap<>();
    private int n = 5;

    public ViewSync() {
        //subscribe(kVStoreMsgHandler, kVStore);
        subscribe(deliverHandler, node);
        subscribe(getViewHandler, node);
        subscribe(initHandler, node);
        subscribe(joinHandler, node);
        subscribe(viewHandler, node);
    }

    Handler<KVStoreMsg> kVStoreMsgHandler = new Handler<KVStoreMsg>(){
        @Override
        public void handle(KVStoreMsg event) {
            trigger(new ViewSyncSend(view.get(event.dst), Msg.KV_STORE, null), node);
        }
    };

    Handler<ViewSyncDeliver> deliverHandler = new Handler<ViewSyncDeliver>(){
        @Override
        public void handle(ViewSyncDeliver event) {
            //trigger((KVStoreMsg) event.msgBody, kVStore);
        }
    };

    Handler<ViewSyncGetView> getViewHandler = new Handler<ViewSyncGetView>(){
        @Override
        public void handle(ViewSyncGetView event) {
            trigger(new ViewSyncSend(event.src, Msg.VIEW, view), node);
        }
    };


    Handler<ViewSyncInit> initHandler = new Handler<ViewSyncInit>(){
        @Override
        public void handle(ViewSyncInit event) {
            n = event.n;
        }
    };

    Handler<ViewSyncJoin> joinHandler = new Handler<ViewSyncJoin>(){
        @Override
        public void handle(ViewSyncJoin event) {
            view.put(event.id, event.src);
            if (view.size() == n) {
                System.out.println("Recived all join");
                for (Map.Entry<Integer, TAddress> entry : view.entrySet()) {
                    trigger(new ViewSyncSend(entry.getValue(), Msg.VIEW, view), node);
                }
            }
        }
    };

    Handler<ViewSyncView> viewHandler = new Handler<ViewSyncView>(){
        @Override
        public void handle(ViewSyncView event) {
            view = event.view;
            trigger(new KVStoreInit(view.keySet()), kVStore);
        }
    };
}
