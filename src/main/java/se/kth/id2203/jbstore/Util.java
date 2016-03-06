package se.kth.id2203.jbstore;

import com.google.common.hash.Hashing;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class Util {

    public static long getHash(Serializable serializable) {
        return Hashing.murmur3_128().hashBytes(SerializationUtils.serialize(serializable)).asLong();
    }

    public static LinkedList getSortedList(Collection collection) {
        LinkedList list = new LinkedList<>(collection);
        Collections.sort(list);
        return list;
    }
}
