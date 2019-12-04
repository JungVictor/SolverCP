package model.variables;

import model.constraint.expressions.Expression;
import structures.UnorderedReversibleList;

public class Domain {

    private UnorderedReversibleList domain;
    private int index = 0;

    public String toString(){
        return domain.toString();
    }

    public Domain copy(){
        Domain copy = new Domain(this.domain.getList());
        return copy;
    }

    public void set(int index){
        this.domain.set(index);
    }

    public Domain(int[] values){
        this.domain = new UnorderedReversibleList(values);
        this.domain.save();
    }

    public Domain(int lb, int ub){
        int[] dom = new int[ub-lb+1];
        for(int i = 0; i < dom.length; i++) dom[i] = lb+i;
        this.domain = new UnorderedReversibleList(dom);
        this.domain.save();
    }

    public boolean filter(Expression expression){
        domain.removeIf(n -> !expression.eval(n));
        return !domain.isEmpty();
    }

    public int getValue(int index){
        return this.domain.getValue(index);
    }

    public int size(){
        return this.domain.size();
    }

    public boolean isSet(){
        return this.domain.size() == 1;
    }


    /**
     * Check if an element is in the domain
     * @param a element
     * @return True if a is in the domain, false otherwise
     */
    public boolean contains(int a){
        return this.domain.contains(a);
    }

    public void restore() {
        domain.restore();
    }

    public void setIndex(int index){
        if(this.index < index) domain.save();
        else {
            restore();
            if(this.index == index) domain.save();
        }
        this.index = index;
    }

    /**
     * Remove an element from the domain
     * @param element
     * @return False if the domain is empty, true otherwise.
     */
    public boolean remove(int element){
        this.domain.removeValue(element);
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

    public UnorderedReversibleList getValues() {
        return domain;
    }
}
