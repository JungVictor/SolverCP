package model.variables;

import model.constraint.expressions.Expression;

import java.util.ArrayList;
import java.util.LinkedList;

public class Domain {

    private ArrayList<Integer> domain;
    private LinkedList<Integer> removed;
    private ArrayList<Integer> toPop;
    private int index, maxIndex;

    public String toString(){
        if(domain.isEmpty()) return "{}";
        StringBuilder res = new StringBuilder("{");
        for(int i = 0; i < domain.size()-1; i++) res.append(domain.get(i)).append(", ");
        return res.toString() + domain.get(domain.size()-1) + "}";
    }

    public Domain copy(){
        Domain copy = new Domain();
        copy.add(this.domain);
        return copy;
    }

    public Domain(){
        this.domain = new ArrayList<>();
        this.removed = new LinkedList<>();
        this.toPop = new ArrayList<>();
        this.index = -1;
        this.maxIndex = -1;
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

    public boolean filter(Expression expression){
        domain.removeIf(n -> !expression.eval(n));

        return !domain.isEmpty();
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

    public void restore(int index) {
        int nPop = 0;
        for (int i = this.index; i >= index; i--) {
            nPop += this.toPop.get(i);
            this.toPop.set(i, 0);
        }
        for (int i = 0; i < nPop; i++) domain.add(removed.pop());
    }

    public void setIndex(int index){
        if(index > maxIndex){
            maxIndex = index;
            toPop.add(0);
        } else if (this.index < index) toPop.set(index, 0);
        else restore(index);
        this.index = index;
    }

    /**
     * Remove an element from the domain
     * @param a element
     * @return False if the domain is empty, true otherwise.
     */
    public boolean remove(int a){
        int index = getIndex(a);
        if(index >= 0) {
            this.domain.remove(index);
            this.removed.push(a);
            this.toPop.set(this.index, this.toPop.get(this.index)+1);
        }
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
