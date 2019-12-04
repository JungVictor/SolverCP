package structures;

import java.util.Iterator;
import java.util.function.Predicate;

public class UnorderedReversibleList implements Iterable<Integer> {

    private int capacity, save, pointer, tmp;
    private int[] list, pointers;
    private UnorderedReversibleListIterator iterator;

    /**************************
     *  CONSTRUCTORS
     **************************/

    public UnorderedReversibleList(int[] list, int capacity){
        this.capacity = capacity;
        this.save = 0;
        this.list = new int[list.length];
        System.arraycopy(list, 0, this.list, 0, list.length);
        this.pointers = new int[capacity];
        this.pointer = list.length - 1;
        this.iterator = new UnorderedReversibleListIterator();
    }

    public UnorderedReversibleList(int[] list){
        this(list, 20);
    }

    /**************************
     *  GETTERS
     **************************/

    public void save(){
        if(save+1 >= capacity) enlarge(capacity);
        pointers[save++] = pointer;
    }

    public void restore(){
        pointer = pointers[--save];
    }

    /**
     * Get the size of the list
     * @return
     */
    public int size(){
        return pointer+1;
    }

    /**
     * Get the value at a given index
     * @param index
     * @return
     */
    public int getValue(int index){
        return list[index];
    }

    public boolean isEmpty(){
        return size() == 0;
    }

    /**************************
     *  METHODS
     **************************/

    public void removeIf(Predicate<Integer> predicate){
        for(int v : this) if(predicate.test(v)) removeValue(v);
    }

    public String toString(){
        if(isEmpty()) return "{}";
        StringBuilder representation = new StringBuilder("{");
        for(int i = 0; i < pointer; i++) representation.append(getValue(i)).append(", ");
        return representation.toString() + getValue(pointer) + "}";
    }

    /**
     * Enlarge the capacity of the pointers
     * @param space
     */
    private void enlarge(int space){
        this.capacity += space;
        int[] new_pointers = new int[capacity];
        System.arraycopy(pointers, 0, new_pointers, 0, pointers.length);
        pointers = new_pointers;
    }

    /**
     * Swap 2 elements of the list
     * @param i1
     * @param i2
     */
    private void swap(int i1, int i2){
        tmp = list[i1];
        list[i1] = list[i2];
        list[i2] = tmp;
    }

    /**
     * Remove the last element of the list
     * @return
     */
    public int removeLast(){
        this.iterator.minus();
        return list[pointer--];
    }

    /**
     * Get the index of the element in the list
     * @param element
     * @return
     */
    public int indexOf(int element){
        for(int i = 0; i < size(); i++) if(getValue(i) == element) return i;
        return -1;
    }

    /**
     * Return true if the element is in the list, false otherwise
     * @param element
     * @return
     */
    public boolean contains(int element){
        tmp = indexOf(element);
        return 0 <= tmp && tmp <= pointer;
    }

    public int[] getList(){
        return this.list;
    }

    public void set(int index){
        swap(index, 0);
        pointer = 0;
    }

    public int removeIndex(int index){
        swap(index, pointer);
        return removeLast();
    }

    public int removeValue(int value){
        tmp = indexOf(value);
        if(tmp < 0) return -1;
        return removeIndex(tmp);
    }

    @Override
    public Iterator<Integer> iterator() {
        iterator.reset();
        return iterator;
    }

    private class UnorderedReversibleListIterator implements Iterator<Integer> {

        private int index = 0;

        private void reset(){
            index = 0;
        }

        private void minus(){
            if(index > 0) index--;
        }

        @Override
        public boolean hasNext() {
            return index <= pointer;
        }

        @Override
        public Integer next() {
            return getValue(index++);
        }
    }
}
