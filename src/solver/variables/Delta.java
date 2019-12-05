package solver.variables;

import java.util.Iterator;

public class Delta implements Iterable<Integer> {

    private int[] delta;
    private int size;
    private DeltaIterator iterator = new DeltaIterator();

    public String toString(){
        if(isEmpty()) return "{}";
        StringBuilder res = new StringBuilder("{");
        for(int i = 0; i < size-1; i++) res.append(delta[i]).append(", ");
        return res.toString() + delta[size-1] + "}";
    }

    public Delta copy(){
        Delta copy = new Delta(delta.length);
        System.arraycopy(delta, 0, copy.delta, 0, delta.length);
        copy.size = size;
        return copy;
    }

    public Delta(int capacity){
        delta = new int[capacity];
        size = 0;
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public void reset(){
        size = 0;
    }

    public void add(int a){
        delta[size++] = a;
    }

    @Override
    public Iterator<Integer> iterator() {
        iterator.index = 0;
        return iterator;
    }

    private class DeltaIterator implements Iterator<Integer>{

        private int index = 0;

        @Override
        public boolean hasNext() {
            return index <= size;
        }

        @Override
        public Integer next() {
            return delta[index++];
        }
    }
}
