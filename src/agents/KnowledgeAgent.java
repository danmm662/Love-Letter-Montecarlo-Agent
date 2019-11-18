package agents;

import java.util.Random;

import loveletter.*;

/**
 * An interface for representing an agent in the game Love Letter
 * All agent's must have a 0 parameter constructor
 * */
public class KnowledgeAgent implements Agent {

  private Random rand;
  private State current;
  private int myIndex;

  //0 place default constructor
  public KnowledgeAgent(){
    rand  = new Random();
  }

  /**
   * Reports the agents name
   * */
  public String toString(){return "Know It All.";}


  /**
   * Method called at the start of a round
   * @param start the starting state of the round
   **/
  public void newRound(State start){
    current = start;
    myIndex = current.getPlayerIndex();
  }

  /**
   * Method called when any agent performs an action. 
   * @param act the action an agent performs
   * @param results the state of play the agent is able to observe.
   * **/
  public void see(Action act, State results){
    current = results;
  }

  /**
   * Perform an action after drawing a card from the deck
   * @param c the card drawn from the deck
   * @return the action the agent chooses to perform
   * @throws IllegalActionException when the Action produced is not legal.
   * */
  public Action playCard(Card c) {

    Card c1 = c;
    Card c2 = current.getCard(myIndex);

    int W = -1;
    for (int i = 0; i < 4; i++) {
        if (i != myIndex) {
            if (current.score(i) > W) 
                W = i;
        }
    }

    int D = current.deckSize();

    Card[] known = new Card[4];

    for (int i = 0; i < known.length; i++) {
        if(i != myIndex){
            known[i] = current.getCard(i);
        }
    }

    boolean[] eliminated = new boolean[4];

    for (int i = 0; i < eliminated.length; i++) {
        if(current.eliminated(i)) {
            eliminated[i] = true;
        }
        else{
            eliminated[i] = false;
        }
    }
    
    Card[] unseen = current.unseenCards();
    Card L = findMostLikelyCard(unseen);

    Action act = heuristic(c1, c2, W, D, known, eliminated, L);
    System.out.println(act);

    return act;
  }

private Action heuristic(Card c1, Card c2, int W, int D, Card[] known, boolean[] eliminated, Card L) {
    
    Action act = null;

    try {
    if (c1 == Card.COUNTESS && (c2 == Card.PRINCE || c2 == Card.KING || c2 == Card.PRINCESS)) {
        act = Action.playCountess(myIndex);
    }
    else if (c2 == Card.COUNTESS && (c1 == Card.PRINCE || c1 == Card.KING || c1 == Card.PRINCESS)) {
        act = Action.playCountess(myIndex);
    }
    else if (known[W] != null && (c1 == Card.GUARD || c2 == Card.GUARD)) {
        act = Action.playGuard(myIndex, W, known[W]);
    }
    else if ( ( (known[0] != null) || (known[1] != null) || (known[2] != null) || (known[3] != null)) && ((c1 == Card.GUARD) || (c2 == Card.GUARD)) ) {
        if(myIndex != 0 && known[0] != null){
            act = Action.playGuard(myIndex, 0, known[0]);
        }
        else if(myIndex != 1 && known[1] != null){
            act = Action.playGuard(myIndex, 1, known[1]);
        }
        else if(myIndex != 2 && known[2] != null){
            act = Action.playGuard(myIndex, 2, known[2]);
        }
        else if(myIndex != 3 && known[3] != null){
            act = Action.playGuard(myIndex, 3, known[3]);
        }
    }
    else if ( ((myIndex != 0 && known[0] == Card.PRINCESS) || (myIndex != 1 && known[1] == Card.PRINCESS) || (myIndex != 2 && known[2] == Card.PRINCESS) || (myIndex != 3 && known[3] == Card.PRINCESS)) && ((c1 == Card.PRINCE) || (c2 == Card.PRINCE)) ) {
        if(myIndex != 0 && known[0] != Card.PRINCESS){
            act = Action.playPrince(myIndex, 0);
        }
        else if(myIndex != 1 && known[1] != Card.PRINCESS){
            act = Action.playPrince(myIndex, 1);
        }
        else if(myIndex != 2 && known[2] != Card.PRINCESS){
            act = Action.playPrince(myIndex, 2);
        }
        else if(myIndex != 3 && known[3] != Card.PRINCESS){
            act = Action.playPrince(myIndex, 3);
        }
    }
    else if ( (c1 == Card.BARON && (c2 == Card.PRINCE || c2 == Card.KING || c2 == Card.COUNTESS || c2 == Card.PRINCESS)) || (c2 == Card.BARON && (c1 == Card.PRINCE || c1 == Card.KING || c1 == Card.COUNTESS || c1 == Card.PRINCESS)) ) {
        if(!eliminated[W]){
            act = Action.playBaron(myIndex, W);
        }
        else {
            for (int i = 0; i < eliminated.length; i++) {
                if(!eliminated[i] && i != myIndex){
                    act = Action.playBaron(myIndex, i);
                }
            }
        }
    }
    else if ( ((myIndex != 0 && known[0] == Card.COUNTESS || known[0] == Card.KING) || (myIndex != 1 && known[1] == Card.COUNTESS || known[1] == Card.KING) || (myIndex != 2 && known[2] == Card.COUNTESS || known[2] == Card.KING) || (myIndex != 3 && known[3] == Card.COUNTESS || known[3] == Card.KING)) && ((c1 == Card.PRINCE) || (c2 == Card.PRINCE)) ) {
        if(myIndex != 0 && known[0] == Card.COUNTESS || known[0] == Card.KING){
            act = Action.playPrince(myIndex, 0);
        }
        else if(myIndex != 1 && known[1] == Card.COUNTESS || known[1] == Card.KING){
            act = Action.playPrince(myIndex, 1);
        }
        else if(myIndex != 2 && known[2] == Card.COUNTESS || known[2] == Card.KING){
            act = Action.playPrince(myIndex, 2);
        }
        else if(myIndex != 3 && known[3] == Card.COUNTESS || known[3] == Card.KING){
            act = Action.playPrince(myIndex, 3);
        }
    }
    else if ( (D < 8) && ((myIndex != 0 && known[0] == Card.COUNTESS || known[0] == Card.PRINCESS) || (myIndex != 1 && known[1] == Card.COUNTESS || known[1] == Card.PRINCESS) || (myIndex != 2 && known[2] == Card.COUNTESS || known[2] == Card.PRINCESS) || (myIndex != 3 && known[3] == Card.COUNTESS || known[3] == Card.PRINCESS)) && ((c1 == Card.KING) || (c2 == Card.KING)) ) {
        if(myIndex != 0 && known[0] == Card.COUNTESS || known[0] == Card.KING){
            act = Action.playKing(myIndex, 0);
        }
        else if(myIndex != 1 && known[1] == Card.COUNTESS || known[1] == Card.KING){
            act = Action.playKing(myIndex, 1);
        }
        else if(myIndex != 2 && known[2] == Card.COUNTESS || known[2] == Card.KING){
            act = Action.playKing(myIndex, 2);
        }
        else if(myIndex != 3 && known[3] == Card.COUNTESS || known[3] == Card.KING){
            act = Action.playKing(myIndex, 3);
        }
    }
    else if(c1 == Card.PRIEST || c2 == Card.PRIEST) {
        if(!eliminated[W]){
            act = Action.playPriest(myIndex, W);
        }
        else {
            for (int i = 0; i < eliminated.length; i++) {
                if(!eliminated[i] && i != myIndex){
                    act = Action.playPriest(myIndex, i);
                }
            }
        }
    }
    else if(c1 == Card.HANDMAID || c2 == Card.HANDMAID) {
        act = Action.playHandmaid(myIndex);        
    }
    else if(c1 == Card.GUARD || c2 == Card.GUARD){
        if(!eliminated[W]){
            act = Action.playGuard(myIndex, W, L);
        }
        else {
            for (int i = 0; i < eliminated.length; i++) {
                if(!eliminated[i] && i != myIndex){
                    act = Action.playGuard(myIndex, i, L);
                }
            }
        }
    }
    else if (c1 == Card.PRINCE || c2 == Card.PRINCE) {
        if(!eliminated[W]){
            act = Action.playPrince(myIndex, W);
        }
        else {
            for (int i = 0; i < eliminated.length; i++) {
                if(!eliminated[i] && i != myIndex){
                    act = Action.playPrince(myIndex, i);
                }
            }
        }
    }
    else if (c1 == Card.KING || c2 == Card.KING) {
        if(!eliminated[W]){
            act = Action.playKing(myIndex, W);
        }
        else {
            for (int i = 0; i < eliminated.length; i++) {
                if(!eliminated[i] && i != myIndex){
                    act = Action.playKing(myIndex, i);
                }
            }
        }
    }
    else{
        System.out.println("Random card played");
        act = playRandomCard(c1);
    }

    } catch(IllegalActionException e){
        System.out.println("Illegal move performed :" + act);
        act = playRandomCard(c1);
    }
    
    if(act == null) {
        act = playRandomCard(c1);
    }

    return act;

}

/**
   * Perform an action after drawing a card from the deck
   * 
   * @param c the card drawn from the deck
   * @return the action the agent chooses to perform
   * @throws IllegalActionException when the Action produced is not legal.
   */
  public Action playRandomCard(Card c) {
    Action act = null;
    Card play;
    while (!current.legalAction(act, c)) {
      if (rand.nextDouble() < 0.5)
        play = c;
      else
        play = current.getCard(myIndex);
      int target = rand.nextInt(current.numPlayers());
      try {
        switch (play) {
        case GUARD:
          act = Action.playGuard(myIndex, target, Card.values()[rand.nextInt(7) + 1]);
          break;
        case PRIEST:
          act = Action.playPriest(myIndex, target);
          break;
        case BARON:
          act = Action.playBaron(myIndex, target);
          break;
        case HANDMAID:
          act = Action.playHandmaid(myIndex);
          break;
        case PRINCE:
          act = Action.playPrince(myIndex, target);
          break;
        case KING:
          act = Action.playKing(myIndex, target);
          break;
        case COUNTESS:
          act = Action.playCountess(myIndex);
          break;
        default:
          act = null;// never play princess
        }
      } catch (IllegalActionException e) {
        /* do nothing */}
    }
    return act;
  }

  private Card findMostLikelyCard(Card[] unseen) {

    if(unseen[0] == null){
        return Card.PRINCESS;
    }
      
    int max = 1;
    int counter = 1;
    Card c = unseen[0];
    
    Card maxCard = c;
    
    for(int i = 1; i < unseen.length; i++){
        if(unseen[i] != c) {
            if(counter > max){
                max = counter;
                maxCard = c;
            }
            c = unseen[i];
        }
        else{
            counter++;
        }    
    }
    
    if(counter > max){
        max = counter;
        maxCard = c;
    }
    

    return maxCard;

  }







}


