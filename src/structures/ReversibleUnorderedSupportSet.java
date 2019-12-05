package structures;

import java.util.*;

public class ReversibleUnorderedSupportSet {

    private HashMap<Integer, ReversibleUnorderedSet> supports;
    private HashMap<Integer, ArrayList<Integer>> tmp;

    private int index = -1;

    public ReversibleUnorderedSupportSet(){
        this.supports = new HashMap<>();
        this.tmp = new HashMap<>();
    }

    public void build(){
        for(int key : tmp.keySet()){
            supports.put(key, new ReversibleUnorderedSet(tmp.get(key)));
            tmp.put(key, null);
        }
        setIndex(0);
        tmp = null;
    }

    private void save(int key, int index){
        supports.get(key).save(index);
    }

    private void save(int index){
        for(int key : supports.keySet()) save(key, index);
    }

    private void restore(int key, int index){
        supports.get(key).restore(index);
    }

    private void restore(int index){
        for(int key : supports.keySet()) restore(key, index);
    }

    public void setIndex(int index) {
        if (this.index < index) save(index);
        else restore(index);
        this.index = index;
    }

    public ReversibleUnorderedSet getSupports(int key){
        return this.supports.get(key);
    }

    public boolean remove(int key, int i){
        supports.get(key).removeIndex(i);
        return isEmpty(key);
    }

    public void removeAll(int key){
        supports.get(key).removeAll();
    }

    public void addKey(int key){
        tmp.put(key, new ArrayList<>());
    }

    public void put(int key, int value){
        try {
            tmp.get(key).add(value);
        } catch (NullPointerException e){
            // skip
        }
    }

    public boolean isEmpty(int key){
        return supports.get(key).isEmpty();
    }

    public Collection<Integer> keySet(){
        return this.supports.keySet();
    }

    public int indexOf(int key, int value){
        try {
            return this.supports.get(key).indexOf(value);
        } catch (NullPointerException e){
            return -1;
        }
    }

}
