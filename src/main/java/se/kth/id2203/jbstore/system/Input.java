package se.kth.id2203.jbstore.system;

import se.sics.kompics.*;

public class Input extends ComponentDefinition {

    Negative<ClientPort> cpn = provides(ClientPort.class);

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            trigger(new ClientPort.Request(ClientPort.Request.Type.PUT, "Key0", "value0"), cpn);
            trigger(new ClientPort.Request(ClientPort.Request.Type.GET, "key0", null), cpn);

            trigger(new ClientPort.Request(ClientPort.Request.Type.HISTORY, null, null), cpn);
        }
    };

    {
        subscribe(startHandler, control);
    }


}
