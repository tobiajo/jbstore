package se.kth.id2203.jbstore.system.application;

import java.io.Serializable;
import java.util.HashMap;

import com.google.common.hash.Hashing;
import se.kth.id2203.jbstore.system.application.event.*;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;

public class KVStore extends ComponentDefinition {

    private Positive<KVStorePort> node = requires(KVStorePort.class);

    private HashMap<String, SequenceValueTuple> store = new HashMap<>();
    long sequence = 0;
    long rid = 0;

    public KVStore() {
        subscribe(initHandler, node);
        subscribe(readHandler, node);
        subscribe(writeHandler, node);
    }

    Handler<KVStoreInit> initHandler = new Handler<KVStoreInit>(){
        @Override
        public void handle(KVStoreInit event) {
            System.out.println("===>" + event.view);
        }
    };

    Handler<KVStoreRead> readHandler = new Handler<KVStoreRead>(){
        @Override
        public void handle(KVStoreRead read) {
            trigger(new KVStoreValue(read.src, store.get(read.key)), node);
        }
    };

    Handler<KVStoreWrite> writeHandler = new Handler<KVStoreWrite>(){
        @Override
        public void handle(KVStoreWrite write) {
            SequenceValueTuple toWrite = (SequenceValueTuple) write.value;
            long oldSeq = store.get(write.key).sequence;

            if (toWrite.sequence > oldSeq) {
                store.put(write.key, toWrite);
            }

            trigger(new KVStoreAck(rid), node);

        }
    };

    private class SequenceValueTuple implements Serializable{
        public final long sequence;
        public final Serializable value;

        public SequenceValueTuple(long sequence, Serializable value){
            this.sequence = sequence;
            this.value = value;
        }
    }
    public static long hashString(String key){
        return Hashing.murmur3_128().hashBytes(key.getBytes()).asLong();
    }
}
