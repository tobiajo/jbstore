package se.kth.id2203.jbstore.system;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PortType;

import java.io.Serializable;

public class ClientPort extends PortType {

    {
        indication(Request.class);
    }

    public static class Request implements KompicsEvent{
        public enum Type {GET, PUT, HISTORY}

        public final Type type;
        public final String key;
        public final Serializable value;

        public Request(Type type, String key, Serializable value){
            this.type = type;
            this.key = key;
            this.value = value;
        }
    }
}
