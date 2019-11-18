package agents;

import loveletter.*;

//Represents a node for MCTS of Love Letter
public class Node {

    private Node parent;
    private Node child1;
    private Node child2;
    private int visits;
    private int score;
    private boolean isTerminal;
    private MyState nodeState;    // State for this node, generated already from the unseen cards and methods in myState.java

    /**
     * Constructor for node, needs a card in hand, card just drawn from deck, the unseen cards and the player index to be constructed
     * 
     */
    public Node(){
        parent = null;
        child1 = null;
        child2 = null;
        visits = 0;
        score = 0;
        nodeState = null;
        isTerminal = false;
    }

    /**--------------------------------------------------------------------------------------------------------------
    GETTER METHODS
    ---------------------------------------------------------------------------------------------------------------*/
    public boolean getIsTerminal() {
        return isTerminal;
    }
    public int getVisits(){
        return visits;
    }

    public int getScore(){
        return score;
    }

    public Node getParent(){
        return parent;
    }

    public Node getFirstChild(){
        return child1;
    }

    public Node getSecondChild(){
        return child2;
    }

    public MyState getState(){
        return nodeState;
    }

    /**
     * Returns true if the node is a leaf node
     */
    public boolean isLeaf(){
        return ( (child1 == null) && (child2 == null) );
    }

    /**--------------------------------------------------------------------------------------------------------------
    SETTER METHODS
    ---------------------------------------------------------------------------------------------------------------*/
    public void setIsTerminal(boolean flag) {
        isTerminal = flag;
    }
    public void setParent(Node p){
        parent = p;
    }

    public void setChild1(Node c){
        child1 = c;
    }

    public void setChild2(Node c){
        child2 = c;
    }

    public void incrementVisits(int v){
        visits += v;
    }

    public void incrementScore(int s){
        score += s;
    }

    public void setState(MyState s){
        nodeState = new MyState(s);
    }

}