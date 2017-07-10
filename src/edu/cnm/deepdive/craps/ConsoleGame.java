/**
 * 
 */
package edu.cnm.deepdive.craps;

import java.util.Arrays;

/**
 * @author natedaag
 *
 */
public class ConsoleGame {

  /**
   * @param args
   */
  public static void main(String[] args) {
    StateMachine croupier = new StateMachine();
    croupier.setDisplay((roll) -> {
      System.out.printf("Roll: %s%n", Arrays.toString(roll));
    });
    
    
    croupier.setPlayable((wins, losses) -> {
      String input = System.console().readLine(
          "%d wins, %d losses. Play again? (y/n)", wins, losses);
      return (input.toLowerCase().charAt(0) == 'y');      
    });
    croupier.setContinuable((wins, losses, point) -> {
      String input = System.console().readLine(
          "Point is %d, Continue play? (y/n)", wins, losses);
      return (input.toLowerCase().charAt(0) == 'y');            
    });
    croupier.play();
  }

}