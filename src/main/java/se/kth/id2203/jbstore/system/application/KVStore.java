package se.kth.id2203.jbstore.system.application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.hash.Hashing;

public class KVStore {

    private HashMap<String, Serializable> store = new HashMap<>();

    public void put(String key, Serializable value) {
        store.put(key, value);
    }

    public Serializable get(String key) {
        return store.get(key);
    }

    public void merge(Map<String, Serializable> toMerge){
        store.putAll(toMerge);
    }

    /**
     * Extract and remove key-value pairs with keys > fromHash and <= toHash.
     * @param fromHash
     * @param toHash
     * @return map containing extracted key-value pairs
     */
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


}
