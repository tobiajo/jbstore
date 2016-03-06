package se.kth.id2203.jbstore.system;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class Util {
    public static LinkedList getSortedList(Collection collection) {
        LinkedList list = new LinkedList<>(collection);
        Collections.sort(list);
        return list;
    }
}
