package variables;

import constraint.Constraint;

import java.util.ArrayList;

public class Variable implements Comparable<Variable>{

    // Domaine de la variable : toutes les valeurs possibles pour cette variable
    private Domain[] domain;
    private Domain originalDomain;

    // Delta de la variable : toutes les valeurs retirées du domaine entre deux filtrages
    private int depth = -1;
    private Delta delta;

    // Propagation
    private Propagation propagation;

    // Contraintes où apparait la variable
    private ArrayList<Constraint> constraints;

    // Valeur déjà testées lors du choix
    private ArrayList<Integer> tested;

    public String toString_(){
        String dom = "DOMAIN = " + domain[depth].toString() + "; DELTA = " + delta.toString();
        return dom;
    }

    public Variable(Domain domain, Propagation propagation){
        this.originalDomain = domain;
        this.delta = new Delta();
        this.propagation = propagation;
        this.constraints = new ArrayList<>();
        this.tested = new ArrayList<>();
    }

    /**
     * Check if the value is in the domain.
     * @param a value to be checked.
     * @return True if the value is in the domain, false otherwise.
     */
    public boolean isInDomain(int a){
        return this.domain[depth].contains(a);
    }

    /**
     * Check if the domain is empty.
     * @return True if the domain is empty, false otherwise.
     */
    public boolean isDomainEmpty(){
        return this.domain[depth].isEmpty();
    }


    /**
     * Get the number of value remaining in the domain
     * @return
     */
    public int getDomainSize(){
        return this.getDomain().size();
    }

    /**
     * Get the values remaining in the domain
     * @return
     */
    public ArrayList<Integer> getDomainValues(){
        return this.getDomain().getValues();
    }

    /**
     * Get the value at the given index in the domain
     * @param index
     * @return
     */
    public int getDomainValue(int index){
        return this.getDomain().getValue(index);
    }

    /**
     * Get values removed from the domain during the current iteration
     * @return
     */
    public ArrayList<Integer> getDeltaValues(){
        return this.delta.getDelta();
    }

    /**
     * Check if the delta is empty.
     * @return True if the delta is empty, false otherwise.
     */
    public boolean isDeltaEmpty(){
        return this.delta.isEmpty();
    }

    /**
     * Remove a value from the domain and add it to the delta.
     * @param a value to be removed.
     * @return True of the domain is empty after the call, false otherwise.
     */
    public boolean removeValue(int a){
        if(isInDomain(a)){
            this.domain[depth].remove(a);
            this.delta.add(a);
            propagation.add(this);
        }
        return isDomainEmpty();
    }

    /**
     * Get the number of constraint for this variable
     * @return
     */
    public int getConstraintNumber(){
        return this.constraints.size();
    }

    public void reset(){
        if(depth == 0) this.domain[depth] = this.originalDomain.copy();
        else this.domain[depth] = this.domain[depth - 1].copy();
    }

    public void resetChoices(){
        this.tested = new ArrayList<>();
    }

    /**
     * Reset the delta.
     */
    public void resetDelta(){
        this.delta.reset();
    }

    public void setDepth(int depth){
        this.depth = depth;
        this.reset();
        if(!getDomain().isSet()) for(Constraint c : constraints) c.setIndex(depth);
    }

    public Delta getDelta(){
        return this.delta;
    }

    /**
     * Add a constraint to the variable.
     * @param constraint
     */
    public void addConstraint(Constraint constraint){
        this.constraints.add(constraint);
    }

    /**
     * Get all the constraints where the variable appears.
     * @return
     */
    public ArrayList<Constraint> getConstraints(){
        return this.constraints;
    }

    public Domain getDomain() {
        if(depth < 0) return originalDomain;
        return domain[depth];
    }

    public void initDomains(int N){
        depth = 0;
        this.domain = new Domain[N];
        this.domain[0] = originalDomain.copy();
    }

    public void set(int index){
        ArrayList<Integer> values = new ArrayList<>(this.domain[depth].getValues());
        for(int i = 0; i < values.size(); i++) if(i != index) removeValue(values.get(i));
    }

    public boolean setFirst(){
        int index = getFirstIndex();
        if(index < 0) return false;
        tested.add(this.domain[depth].getValue(index));
        set(index);
        return true;
    }


    private int getFirstIndex(){
        for(int i = 0; i < this.domain[depth].size(); i++){
            if(!tested.contains(this.domain[depth].getValue(i))) return i;
        }
        return -1;
    }

    /**
     * Compare the domain size and the number of constraint of variable.
     * Order first by domain size, then by number of constraints.
     * @param o
     * @return
     */
    @Override
    public int compareTo(Variable o) {
        return compareToDomainSize(o) == -1 ? -1 :
                compareToDomainSize(o) == 0 ? compareToConstraintNumber(o) : 1;
    }

    /**
     * Compare the size of the domain of the variables.
     * @param o
     * @return -1 if smaller, 0 if equal, 1 if bigger
     */
    private int compareToDomainSize(Variable o){
        return this.getDomainSize() < o.getDomainSize() ? -1 :
                this.getDomainSize() == o.getDomainSize() ? 0 : 1;
    }

    /**
     * Compare the number of constraint on the variables.
     * @param o
     * @return -1 if bigger, 0 if equal, 1 if smaller
     */
    private int compareToConstraintNumber(Variable o){
        return this.getConstraintNumber() > o.getConstraintNumber() ? -1 :
                this.getConstraintNumber() == o.getConstraintNumber() ? 0 : 1;
    }
}
