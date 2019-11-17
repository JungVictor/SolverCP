package variables;

import java.util.ArrayList;

public class Delta {

    ArrayList<Integer> delta;

    public String toString(){
        if(delta.isEmpty()) return "{}";
        String res = "{";
        for(int i = 0; i < delta.size()-1; i++) res += delta.get(i) + ", ";
        return res + delta.get(delta.size()-1) + "}";
    }

    public Delta copy(){
        Delta copy = new Delta();
        for(int v : delta) copy.add(v);
        return copy;
    }

    public Delta(){
        delta = new ArrayList<>();
    }

    public boolean isEmpty(){
        return delta.isEmpty();
    }

    public void reset(){
        delta = new ArrayList<>();
    }

    public ArrayList<Integer> getDelta(){
        return this.delta;
    }

    public void add(int a){
        delta.add(a);
    }

}
