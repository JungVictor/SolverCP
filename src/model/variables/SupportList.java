package model.variables;

import java.util.*;

public class SupportList {

    private HashMap<Integer, ArrayList<Integer>> supports;
    private HashMap<Integer, LinkedList<Integer>> delta;
    private HashMap<Integer, ArrayList<Integer>> toPop;

    private int index = -1;
    private int maxIndex = -1;

    public SupportList(){
        this.supports = new HashMap<>();
        this.delta = new HashMap<>();
        this.toPop = new HashMap<>();
    }

    public void setIndex(int index) {
        if(index > maxIndex){
            maxIndex = index;
            for(int key : supports.keySet()) toPop.get(key).add(0);
        } else if (this.index < index){
            for(int key : supports.keySet()) toPop.get(key).set(index, 0);
        } else restore(index);
        this.index = index;
    }

    public void restore(int index){
        for(int key : supports.keySet()){
            int nPop = 0;
            for(int i = this.index; i >= index; i--) {
                nPop += this.toPop.get(key).get(i);
                this.toPop.get(key).set(i, 0);
            }
            for(int i = 0; i < nPop; i++) supports.get(key).add(delta.get(key).pop());
        }
    }

    public Collection<Integer> getSupports(int key){
        return new ArrayList<>(this.supports.get(key));
    }

    public boolean remove(int key, int i){
        delta.get(key).push(supports.get(key).remove(i));
        this.toPop.get(key).set(index, this.toPop.get(key).get(index) + 1);
        return isEmpty(key);
    }

    public void removeAll(int key){
        for(int value : supports.get(key)) delta.get(key).push(value);
        this.toPop.get(key).set(index, this.toPop.get(key).get(index) + supports.get(key).size());
        supports.put(key, new ArrayList<>());
    }

    public void addKey(int key){
        delta.put(key, new LinkedList<>());
        supports.put(key, new ArrayList<>());
        toPop.put(key, new ArrayList<>());
    }

    public void put(int key, int value){
        supports.get(key).add(value);
    }

    public boolean isEmpty(int key){
        return supports.get(key).isEmpty();
    }

    public Collection<Integer> keySet(){
        return this.supports.keySet();
    }

    public int indexOf(int key, int value){
        return this.supports.get(key).indexOf(value);
    }

}
