package se.kth.id2203.jbstore.system.application;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PortType;
import se.sics.test.TAddress;

import java.io.Serializable;
import java.util.HashMap;

public class KVStorePort extends PortType {
    {
        request(Init.class);
        request(Read.class);
        request(Write.class);
        indication(Value.class);
    }

    public static class Init implements KompicsEvent {

        public final HashMap<Integer, TAddress> view;

        public Init(HashMap<Integer, TAddress> view) {
            this.view = view;
        }
    }

    public static class Read implements KompicsEvent {

        public final String key;
        public final TAddress src;

        public Read(String key, TAddress src) {
            this.key = key;
            this.src = src;
        }
    }
    public static class Write implements KompicsEvent {

        public final String key;
        public final TAddress src;
        public final Serializable value;

        public Write(String key, TAddress src, Serializable value) {
            this.key = key;
            this.src = src;
            this.value = value;
        }
    }
    public static class Value implements KompicsEvent {

        public final TAddress src;
        public final Serializable value;

        public Value(TAddress src, Serializable value) {
            this.src = src;
            this.value = value;
        }
    }
}
