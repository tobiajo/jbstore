package se.kth.id2203.jbstore.system.application;

import java.io.Serializable;
import java.util.HashMap;

import com.google.common.hash.Hashing;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;

public class KVStore extends ComponentDefinition {

    private HashMap<String, SequenceValueTuple> store = new HashMap<>();
    Negative<KVStorePort> kvn = provides(KVStorePort.class);

    long sequence = 0;
    long rid = 0;

    public KVStore() {
        subscribe(initHandler, kvn);
        subscribe(readHandler, kvn);
        subscribe(writeHandler, kvn);

    }

    Handler<KVStorePort.Init> initHandler = new Handler<KVStorePort.Init>(){
        public void handle(KVStorePort.Init init) {
            System.out.println("Init");
        }
    };

    Handler<KVStorePort.Read> readHandler = new Handler<KVStorePort.Read>(){
        public void handle(KVStorePort.Read read) {
            trigger(new KVStorePort.Value(read.src, store.get(read.key)), kvn);
        }
    };

    Handler<KVStorePort.Write> writeHandler = new Handler<KVStorePort.Write>(){
        public void handle(KVStorePort.Write write) {
            SequenceValueTuple toWrite = (SequenceValueTuple) write.value;
            long oldSeq = store.get(write.key).sequence;

            if (toWrite.sequence > oldSeq) {
                store.put(write.key, toWrite);
            }

            trigger(new KVStorePort.Ack(rid), kvn);

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
