package agents;

import java.util.Random;
import java.io.*;
import java.util.*;

import loveletter.*;

/**
 * An interface for representing an agent in the game Love Letter All agent's
 * must have a 0 parameter constructor
 */
public class MCTSAgent implements Agent {

  private Random rand;
  private State current;
  private int myIndex;

  // 0 place default constructor
  public MCTSAgent() {
    rand = new Random();
  }

  /**
   * Reports the agents name
   */
  public String toString() {
    return "Monte Moit";
  }

  /**
   * Method called at the start of a round
   * 
   * @param start the starting state of the round
   **/
  public void newRound(State start) {
    current = start;
    myIndex = current.getPlayerIndex();
  }

  /**
   * Method called when any agent performs an action.
   * 
   * @param act     the action an agent performs
   * @param results the state of play the agent is able to observe.
   **/
  public void see(Action act, State results) {
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

    MonteCarlo(c);

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

  public void testState(Card topCard) {

    MyRandomAgent[] agents = { new MyRandomAgent(), new MyRandomAgent(), new MyRandomAgent(), new MyRandomAgent() };
    Card[] remainingCards = current.unseenCards();
    Card inHand = current.getCard(myIndex);

    boolean[] eliminated = new boolean[current.numPlayers()];
    for (int i = 0; i < eliminated.length; i++) {
      if (current.eliminated(i)) {
        eliminated[i] = true;
      }
    }

    int topIndex = 16 - current.deckSize();

    Card[][] discards = new Card[agents.length][16];

    // Iterator to traverse the list
    int cntr = 0;
    for (int j = 0; j < discards.length; j++) {

      Iterator iterator = current.getDiscards(j);
      while (iterator.hasNext()) {
        discards[j][cntr++] = (Card) iterator.next();
      }

      cntr = 0; // reset
    }

    boolean[] handmaid = new boolean[agents.length];
    for (int i = 0; i < handmaid.length; i++) {
      handmaid[i] = current.handmaid(i);
    }

    MyState s = new MyState(rand, agents, remainingCards, topCard, inHand, myIndex, eliminated, topIndex, discards,
        handmaid);

  }

  /**
   * Apply the Monti Carlo Algorithm in order to make the best move in the current
   * position
   * 
   * @param c card picked up
   * @return card the agent should play given the position
   */
  public Card MonteCarlo(Card c) {

    // Need to set up the root node outside of the for loops

    Node rootNode = new Node();

    Node child1 = new Node();
    Node child2 = new Node();

    // Set up the child nodes for the root node
    // They are both currently lead nodes, don't need to store anything in them at
    // this stage
    // Need to make sure over all of the for loops, that number of visits and score
    // are maintained

    rootNode.setChild1(child1);
    rootNode.setChild2(child2);

    child1.setParent(rootNode);
    child2.setParent(rootNode);

    // GET INFORMATION FOR DETERMINISATION

    MyState[] playerStates = new MyState[4];

    MyRandomAgent[] agents = { new MyRandomAgent(), new MyRandomAgent(), new MyRandomAgent(), new MyRandomAgent() };
    Card[] remainingCards = current.unseenCards();
    Card inHand = current.getCard(myIndex);
    // eliminated info
    boolean[] eliminated = new boolean[current.numPlayers()];
    for (int i = 0; i < eliminated.length; i++) {
      if (current.eliminated(i)) {
        eliminated[i] = true;
      }
    }

    int topIndex = 16 - current.deckSize();
    // discards info
    Card[][] discards = new Card[agents.length][16];

    // Iterator to traverse the list
    int cntr = 0;
    for (int j = 0; j < discards.length; j++) {

      Iterator iterator = current.getDiscards(j);
      while (iterator.hasNext()) {
        discards[j][cntr++] = (Card) iterator.next();
      }

      cntr = 0; // reset
    }
    // handmaid info
    boolean[] handmaid = new boolean[agents.length];
    for (int i = 0; i < handmaid.length; i++) {
      handmaid[i] = current.handmaid(i);
    }

    // Overarching for-loop
    // 100 different decks will be generated by this loop
    for (int i = 0; i < 100; i++) {

      MyState s = new MyState(rand, agents, remainingCards, c, inHand, myIndex, eliminated, topIndex, discards,
          handmaid);

      // set player states for each random agent
      try {
        for (int j = 0; j < agents.length; j++) {
          playerStates[j] = s.playerState(j);
          agents[j].newRound(playerStates[j]);
        }
      } catch (IllegalActionException e) {
        System.out.println("Illegal action");
      }

      rootNode.setState(s);

      // Draw card from deck, then play the card just drawn from the deck
      try {
        Card topCard = s.drawCard();
      } catch (IllegalActionException e) {
        System.out.println("Illegal action");
      }
      System.out.println("------BEGIN EXPAND ROOT NODE------");
      expandRoot(rootNode, agents, c);
      System.out.println("------END EXPAND ROOT NODE------");

      for (int j = 0; j < 20; j++) {
        Node currentNode = rootNode;

        // Go down the tree until a leaf node has been found
        while (!currentNode.isLeaf()) {

          // Sets the current node to its child with the lowest UCB1 value
          currentNode = UCB1(currentNode);

        }

        // MyState gameState = new MyState(currentNode.getState());

        // EXPANSION PHASE OF
        // MONTECARLO---------------------------------------------------------------------------------------------------------------------------------

        // Check if the node has been visited before
        // If it has been visited, expand the node and set currentNode to one of the
        // newly generated children
        int score = 0;
        if (!currentNode.getIsTerminal()) {
          if (currentNode.getVisits() != 0) {
            // Need to expand the node here           
            expand(currentNode, agents);
            // Select one of the children as the new node
            currentNode = currentNode.getFirstChild();
          }

          // ROLLOUT PHASE OF
          // MONTECARLO------------------------------------------------------------------------------------------------------------------------

          score = myRollout(currentNode);

        }
        // BACKPROPAGATION PHASE OF
        // MONTECARLO-----------------------------------------------------------------------------------------------------------------

        currentNode.incrementScore(score);
        currentNode.incrementVisits(1);
        // Travels up the tree, increasing the score and visits for each node until it
        // reaches the root node
        while (currentNode.getParent() != null) {
          if (score == 1) {
            currentNode.getParent().incrementScore(1);
          }
          currentNode.getParent().incrementVisits(1);

          currentNode = currentNode.getParent();
        }
      }
    }

    //Calculate which of the two leaf nodes from the main root node are better
    double ratio1 = (double) child1.getScore() / (double) child1.getVisits();
    double ratio2 = (double) child2.getScore() / (double) child2.getVisits();

    if (ratio1 < ratio2)
      return c;
    else
      return current.getCard(myIndex);

  }

  /**
   * 
   * @param n
   * @return
   */
  public int myRollout(Node n) {
    MyState gameState = new MyState(n.getState());

    // create agents to participate in the rollout
    MyRandomAgent[] agents = new MyRandomAgent[4];
    for (int i = 0; i < agents.length; i++) {
      agents[i] = new MyRandomAgent();
    }

    MyState[] playerStates = new MyState[4];
    try {
      for (int i = 0; i < playerStates.length; i++) {
        playerStates[i] = gameState.playerState(i);
        agents[i].newRound(playerStates[i]);
      }

      // WHILE ROUND IS NOT OVER && AGENT NOT ELIMINATED
      while (!gameState.roundOver() && !gameState.eliminated(myIndex)) {

        Card topCard = gameState.drawCard();
        System.out.println("Player " + gameState.nextPlayer() + " draws the " + topCard);
        Action act = agents[gameState.nextPlayer()].playCard(topCard);
        try {
          System.out.println(gameState.update(act, topCard));
        } catch (IllegalActionException e) {
          // System.out.println("ILLEGAL ACTION PERFORMED BY PLAYER " + agents[gameState.nextPlayer()] + "("
          //     + gameState.nextPlayer() + ")\nRandom Move Substituted");
          // randomAgent.newRound(gameState.playerState(gameState.nextPlayer()));
          // act = randomAgent.playCard(topCard);
          // gameState.update(act, topCard);
          System.exit(1);
        }
        for (int p = 0; p < agents.length; p++)
          agents[p].see(act, playerStates[p]);
      }
      // System.out.println("End of Rollout, scores are:");
      // for (int i = 0; i < agents.length; i++) {
      // System.out.println("Player " + i + ": " + gameState.score(i));
      // }
      return gameState.score(myIndex);
    } catch (IllegalActionException e) {
      System.out.println("IllegalActionException IN ROLLOUT");
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * expand a node n
   * 
   * @param n
   * @param topCard
   */
  private void expand(Node n, MyRandomAgent[] agents) {

    MyState gameState = new MyState(n.getState());
    MyState[] playerStates = new MyState[4];
    
    Node child1 = new Node();
    Node child2 = new Node();
    
    n.setChild1(child1);
    n.setChild2(child2);

    child1.setParent(n);
    child2.setParent(n);    
    
    Card topCard = null;

    try {
        for (int j = 0; j < agents.length; j++) {
          playerStates[j] = gameState.playerState(j);
          agents[j].newRound(playerStates[j]);
        }
      } catch (IllegalActionException e) {
        System.out.println("Illegal action");
      }

    try {
      topCard = gameState.drawCard();
    } catch (IllegalActionException e) {
      System.out.println("Illegal action");
      System.exit(1);
    }
    if (topCard == Card.PRINCESS) {

      Action a = agents[gameState.nextPlayer()].playSpecificCard(gameState.getCard(gameState.nextPlayer()));

      // Update the gameState, then deep copy it into a child node
      try {
        gameState.update(a, topCard);
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      child1.setState(gameState);
      checkIfTerminal(child1);

      child2.incrementVisits(10000000);

    } else if (gameState.getCard(gameState.nextPlayer()) == Card.PRINCESS) {
      Action a = agents[gameState.nextPlayer()].playSpecificCard(topCard);

      // Update the gameState, then deep copy it into a child node
      try {
        gameState.update(a, topCard);
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      child1.setState(gameState);
      checkIfTerminal(child1);

      child2.incrementVisits(10000000);

    } else if (topCard == Card.COUNTESS && (gameState.getCard(gameState.nextPlayer()) == Card.PRINCE
        || gameState.getCard(gameState.nextPlayer()) == Card.KING)) {
      Action a = agents[gameState.nextPlayer()].playSpecificCard(topCard);

      // Update the gameState, then deep copy it into a child node
      try {
        gameState.update(a, topCard);
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      child1.setState(gameState);
      checkIfTerminal(child1);

      child2.incrementVisits(10000000);
    } else if (gameState.getCard(gameState.nextPlayer()) == Card.COUNTESS
        && (topCard == Card.PRINCE || topCard == Card.KING)) {
      Action a = agents[gameState.nextPlayer()].playSpecificCard(gameState.getCard(gameState.nextPlayer()));

      // Update the gameState, then deep copy it into a child node
      try {
        gameState.update(a, topCard);
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      child1.setState(gameState);
      checkIfTerminal(child1);

      child2.incrementVisits(10000000);
    } else {

      Action act = agents[gameState.nextPlayer()].playSpecificCard(topCard);

      // Update the gameState, then deep copy it into a child node
      try {
        gameState.update(act, topCard);
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      child1.setState(gameState);
      checkIfTerminal(child1);

      // Reset gameState to its inital state from rootNode
      gameState = new MyState(n.getState());
      
      //Need to reset the game states for all the players too
      try {
        for (int j = 0; j < agents.length; j++) {
          playerStates[j] = gameState.playerState(j);
          agents[j].newRound(playerStates[j]);
        }
      } catch (IllegalActionException e) {
        System.out.println("Illegal action");
      }
      

      try {
        topCard = gameState.drawCard();
      } catch (IllegalActionException e) {
        System.out.println("Illegal action");
      }
      act = agents[gameState.nextPlayer()].playSpecificCard(gameState.getCard(gameState.nextPlayer()));

      try {
        System.out.println(gameState.update(act, topCard));
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      // Setup child2
      child2.setState(gameState);
      checkIfTerminal(child2);
    }
  }

  /**
   * expand a node n
   * 
   * @param n
   * @param topCard
   */
  private void expandRoot(Node n, MyRandomAgent[] agents, Card topCard) {

    MyState gameState = new MyState(n.getState());
    Node child1 = n.getFirstChild();
    Node child2 = n.getSecondChild();

    if (topCard == Card.PRINCESS) {

      Action a = agents[gameState.nextPlayer()].playSpecificCard(gameState.getCard(gameState.nextPlayer()));

      // Update the gameState, then deep copy it into a child node
      try {
        gameState.update(a, topCard);
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      child1.setState(gameState);
      checkIfTerminal(child1);

      child2.incrementVisits(10000000);

    } else if (gameState.getCard(gameState.nextPlayer()) == Card.PRINCESS) {
      Action a = agents[gameState.nextPlayer()].playSpecificCard(topCard);

      // Update the gameState, then deep copy it into a child node
      try {
        gameState.update(a, topCard);
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      child1.setState(gameState);
      checkIfTerminal(child1);

      child2.incrementVisits(10000000);

    } else if (topCard == Card.COUNTESS && (gameState.getCard(gameState.nextPlayer()) == Card.PRINCE
        || gameState.getCard(gameState.nextPlayer()) == Card.KING)) {
      Action a = agents[gameState.nextPlayer()].playSpecificCard(topCard);

      // Update the gameState, then deep copy it into a child node
      try {
        gameState.update(a, topCard);
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      child1.setState(gameState);
      checkIfTerminal(child1);

      child2.incrementVisits(10000000);
    } else if (gameState.getCard(gameState.nextPlayer()) == Card.COUNTESS
        && (topCard == Card.PRINCE || topCard == Card.KING)) {
      Action a = agents[gameState.nextPlayer()].playSpecificCard(gameState.getCard(gameState.nextPlayer()));

      // Update the gameState, then deep copy it into a child node
      try {
        gameState.update(a, topCard);
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      child1.setState(gameState);
      checkIfTerminal(child1);

      child2.incrementVisits(10000000);
    } else {

      Action act = agents[gameState.nextPlayer()].playSpecificCard(topCard);

      // Update the gameState, then deep copy it into a child node
      try {
        gameState.update(act, topCard);
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      child1.setState(gameState);
      checkIfTerminal(child1);

      // Reset gameState to its inital state from rootNode
      gameState = new MyState(n.getState());

      try {
        topCard = gameState.drawCard();
      } catch (IllegalActionException e) {
        System.out.println("Illegal action");
      }
      act = agents[gameState.nextPlayer()].playSpecificCard(gameState.getCard(gameState.nextPlayer()));

      try {
        System.out.println(gameState.update(act, topCard));
      } catch (IllegalActionException e) {
        System.out.println("Update didnt work");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException l) {
          System.out.println(l);
        }
        System.exit(1);
      }

      // Setup child2
      child2.setState(gameState);
      checkIfTerminal(child2);
    }
  }

  /**
   * Checks if node n is a terminal node
   * 
   * @param n
   */
  private void checkIfTerminal(Node n) {

    MyState s = n.getState();

    if (s.roundOver()) {
      n.setIsTerminal(true);

      if (s.score(myIndex) == 1) {
        n.incrementScore(1);
      }

      return;
    }

    if (s.eliminated(myIndex)) {
      n.setIsTerminal(true);
      return;
    }

  }

  /**
   * Returns the child that has the lowest UCB1 value
   * 
   * 
   */
  public Node UCB1(Node parent) {

    Node child1 = parent.getFirstChild();
    Node child2 = parent.getSecondChild();

    // Checking if visits for either of the nodes is 0, which would make UCB1
    // infinite
    if (child1.getVisits() == 0) {
      return child1;
    } else if (child2.getVisits() == 0) {
      return child2;
    }

    double score1 = ((double) child1.getScore() / (double) child1.getVisits())
        + 2 * 20.0 * Math.sqrt((Math.log(parent.getVisits()) / child1.getVisits()));
    double score2 = ((double) child2.getScore() / (double) child2.getVisits())
        + 2 * 20.0 * Math.sqrt((Math.log(parent.getVisits()) / child2.getVisits()));

    // System.out.println("UCB1 for child 1: " + score1);
    // System.out.println("UCB1 for child 2: " + score2);

    if (score1 >= score2) {
      return child1;
    } else {
      return child2;
    }
  }

}
