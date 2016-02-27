package se.kth.id2203.jbstore.system.application;

import java.io.Serializable;
import java.util.HashMap;

public class KVStore {

    private HashMap<String, Serializable> store = new HashMap<>();

    public void put(String key, Serializable value) {
        store.put(key, value);
    }

    public Serializable get(String key) {
        return store.get(key);
    }
}
