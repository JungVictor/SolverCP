package structures.set;

import java.util.Iterator;
import java.util.function.Predicate;

public interface ReversibleSet extends Iterable<Integer> {

    void save(int index);
    void restore(int index);
    int size();
    boolean isEmpty();

    int getValue(int index);
    int indexOf(int value);
    boolean contains(int value);

    void set(int index);
    int removeIndex(int index);
    int removeValue(int value);
    void removeAll();
    void removeIf(Predicate<Integer> predicate);

    int[] copy();

    Iterator<Integer> iterator();


}
