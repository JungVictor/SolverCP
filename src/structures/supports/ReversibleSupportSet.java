package structures.supports;

import structures.set.ReversibleSet;

import java.util.Collection;

public interface ReversibleSupportSet {

    void build();
    void setIndex(int index);

    ReversibleSet getSupports(int key);

    boolean remove(int key, int index);
    void removeAll(int key);

    void put(int key, int value);
    void addKey(int key);

    boolean isEmpty(int key);
    Collection<Integer> keySet();

    int indexOf(int key, int value);

}
