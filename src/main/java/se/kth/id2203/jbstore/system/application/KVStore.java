package se.kth.id2203.jbstore.system.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.hash.Hashing;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;

public class KVStore extends ComponentDefinition {

    private HashMap<String, Serializable> store = new HashMap<>();
    Negative<KVStorePort> kvn = provides(KVStorePort.class);

    public KVStore() {
        subscribe(getHandler, kvn);
        subscribe(putHandler, kvn);
    }

    public void merge(Map<String, Serializable> toMerge){
        store.putAll(toMerge);
    }

    public Map<String, Serializable> split(long fromHash, long toHash){
        HashMap<String, Serializable> split = new HashMap<>();

        Iterator<String> keyIt = store.keySet().iterator();
        while (keyIt.hasNext()){
            String key = keyIt.next();
            long hash = hashString(key);
            if (fromHash < hash && hash <= toHash){
                split.put(key, store.remove(key));
            }
        }
        return split;
    }


    public static long hashString(String key){
        return Hashing.murmur3_128().hashBytes(key.getBytes()).asLong();
    }

    Handler<KVStorePort.Read> getHandler = new Handler<KVStorePort.Read>(){
        public void handle(KVStorePort.Read read) {
            trigger(new KVStorePort.Value(read.src, store.get(read.key)), kvn);
        }
    };

    Handler<KVStorePort.Write> putHandler = new Handler<KVStorePort.Write>(){
        public void handle(KVStorePort.Write write) {
            store.put(write.key, write.value);
        }
    };
}
