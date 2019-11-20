package variables;

import java.util.ArrayList;
import java.util.Random;

public class Domain {

    ArrayList<Integer> domain;

    public String toString(){
        if(domain.isEmpty()) return "{}";
        String res = "{";
        for(int i = 0; i < domain.size()-1; i++) res += domain.get(i) + ", ";
        return res + domain.get(domain.size()-1) + "}";
    }

    public Domain copy(){
        Domain copy = new Domain();
        copy.add(this.domain);
        return copy;
    }

    public Domain(){
        this.domain = new ArrayList<>();
    }

    public Domain(int v){
        this();
        add(v);
    }
    public Domain(int[] values){
        this();
        add(values);
    }
    public Domain(int lb, int ub){
        this();
        add(lb, ub);
    }

    public int getValue(int index){
        return this.domain.get(index);
    }

    public int size(){
        return this.domain.size();
    }

    public boolean isSet(){
        return this.domain.size() == 1;
    }

    /**
     * Add a value to the domain
     * @param a
     */
    public void add(int a){
        this.domain.add(a);
    }

    /**
     * Add values to the domain
     * @param values
     */
    public void add(int[] values){
        for(int v : values) add(v);
    }

    public void add(ArrayList<Integer> values){
        this.domain.addAll(values);
    }

    /**
     * Add all values from the interval [lb, ub]
     * @param lb lower bound
     * @param ub upper bound
     */
    public void add(int lb, int ub){
        for(int i = lb; i < ub+1; i++) add(i);
    }

    /**
     * Check if an element is in the domain
     * @param a element
     * @return True if a is in the domain, false otherwise
     */
    public boolean contains(int a){
        return this.domain.contains(a);
    }

    /**
     * Remove an element from the domain
     * @param a element
     * @return False if the domain is empty, true otherwise.
     */
    public boolean remove(int a){
        int index = getIndex(a);
        if(index >= 0) this.domain.remove(index);
        return !this.domain.isEmpty();
    }

    public int getIndex(int v){
        return domain.indexOf(v);
    }

    /**
     * Check if the domain is empty
     * @return True if the domain is empty, false otherwise
     */
    public boolean isEmpty(){
        return this.domain.isEmpty();
    }

    public ArrayList<Integer> getValues() {
        return domain;
    }
}
