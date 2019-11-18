package agents;

import java.util.Random;
import loveletter.*;

/**
 * An interface for representing an agent in the game Love Letter All agent's
 * must have a 0 parameter constructor
 */
public class MyRandomAgent {

  private Random rand;
  private MyState current;
  private int myIndex;

  // 0 place default constructor
  public MyRandomAgent() {
    rand = new Random();
  }

  /**
   * Reports the agents name
   */
  public String toString() {
    return "myRando";
  }

  /**
   * Method called at the start of a round
   * 
   * @param start the starting state of the round
   **/
  public void newRound(MyState start) {
    current = start;
    myIndex = current.getPlayerIndex();
  }

  /**
   * Method called when any agent performs an action.
   * 
   * @param act     the action an agent performs
   * @param results the state of play the agent is able to observe.
   **/
  public void see(Action act, MyState results) {
    current = results;
  }

  /**
   * Perform an action after drawing a card from the deck
   * 
   * @param c the card drawn from the deck
   * @return the action the agent chooses to perform
   * @throws IllegalActionException when the Action produced is not legal.
   */
  public Action playCard(Card c) {
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

  public Action playSpecificCard(Card c) {

    Action act = null;
    int target = rand.nextInt(current.numPlayers());

    while (!current.legalAction(act, c)) {
      try {
        target = rand.nextInt(current.numPlayers());
        switch (c) {
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
          System.out.println("PRINCESS ATTEMPTED TO BE PLAYED");
          act = null;// never play princess
        }
      } catch (IllegalActionException e) {
        System.out.println("Card that agent tried to play: " + c.toString());
        System.out.println("Target was: " + target);
        System.out.println("Random agent tried to play illegal action");
      }
    }

    return act;

  }

}
