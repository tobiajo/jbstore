package se.kth.id2203.jbstore.system;

import se.sics.kompics.*;

public class InputGen extends ComponentDefinition {

    private final Positive<InputGenPort> inputPortPositive = requires(InputGenPort.class);

    private Handler<InputGenPort.Init> initHandler = new Handler<InputGenPort.Init>() {
        @Override
        public void handle(InputGenPort.Init event) {

            trigger(new InputGenPort.Request(InputGenPort.Request.Type.PUT, "Key0", "value0"), inputPortPositive);
            trigger(new InputGenPort.Request(InputGenPort.Request.Type.GET, "key0", null), inputPortPositive);
            trigger(new InputGenPort.Request(InputGenPort.Request.Type.HISTORY, null, null), inputPortPositive);
        }
    };

    {
        subscribe(initHandler, inputPortPositive);
    }
}
