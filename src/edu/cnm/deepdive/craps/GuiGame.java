package edu.cnm.deepdive.craps;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * 
 * @author 
 */
public class GuiGame
    implements StateMachine.Playable, StateMachine.Continuable, StateMachine.Display {

  private static final String WINS_FORMAT =  "Wins = %d";
  private static final String LOSSES_FORMAT = "Losses = %d";
  private static final String POINT_FORMAT = "Point = %d";
  private static final String ROLL_FORMAT = "Roll = %d";
  
  private ImageIcon[] dieFaces;
  private JButton roll1;
  private JButton roll2;
  private JButton play;
  private JButton stop;
  private JLabel wins;
  private JLabel losses;
  private JLabel point;
  private JLabel roll;
  
  private boolean uiSetup = false;
  private boolean playClicked = false;
  private boolean stopClicked = false;
  
  private Random rng = new Random();
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GuiGame game = new GuiGame();
    SwingUtilities.invokeLater(() -> game.createAndShowGui());
    game.play();
    //shutdown
    System.exit(0);
  }

  private static ImageIcon createImageIcon(String path) {
    URL imgURL = GuiGame.class.getClassLoader().getResource(path);
    return new ImageIcon(imgURL);
  }

  private void createAndShowGui() {
    JFrame frame = new JFrame("Simple Time-Wasting Craps Game");
    JPanel dicePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JPanel textPanel = new JPanel(new GridLayout(1, 4));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    dieFaces = new ImageIcon[6];
    for (int i = 0; i < 6; i++) {
      dieFaces[i] = createImageIcon(String.format("images/%d.png", i + 1));
    }
    roll1 = new JButton(dieFaces[5]);
    roll2 = new JButton(dieFaces[5]);
    roll1.setEnabled(false);
    roll2.setEnabled(false);
    dicePanel.add(roll1);
    dicePanel.add(roll2);
    play = new JButton("Roll!");
    stop = new JButton("Stop!");
    disableButtons(); 
    play.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        disableButtons();
        resumePlay();
      }      
    });
    stop.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        disableButtons();
        stopPlay();
      }      
    });
    buttonPanel.add(play);
    buttonPanel.add(stop);
    wins = new JLabel(String.format(WINS_FORMAT,  0));
    losses = new JLabel(String.format(LOSSES_FORMAT,  0));
    point = new JLabel(String.format(POINT_FORMAT,  0));
    roll = new JLabel(String.format(ROLL_FORMAT,  0));
    point.setVisible(false);
    roll.setVisible(false);
    textPanel.add(wins);
    textPanel.add(losses);
    textPanel.add(point);
    textPanel.add(roll);
    frame.add(dicePanel, BorderLayout.NORTH);
    frame.add(textPanel, BorderLayout.CENTER);
    frame.add(buttonPanel, BorderLayout.SOUTH);
    frame.pack();
    frame.setVisible(true);
    synchronized (this) {
      uiSetup = true;
      notify(); // wakes play()
    }
  }
  
  private synchronized void play() {
    while (!uiSetup) {
      try {
        wait();  // waits for notify in createAndShowGui()
      } catch (InterruptedException ex) {
        //do nothing.
      }
    }
    
    StateMachine croupier = new StateMachine();
    croupier.setDisplay(this);
    croupier.setPlayable(this);
    croupier.setContinuable(this);
    croupier.play();
  }

  @Override
  public void update(int[] roll) {
    JLabel rollLabel = this.roll;
    synchronized (this) {
      for (int i = 0; i < 10; i++) {
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            roll1.setIcon(dieFaces[rng.nextInt(6)]);
            roll2.setIcon(dieFaces[rng.nextInt(6)]);            
          }
          
        });
        try{
          wait(100);
        } catch (InterruptedException ex) {
          // do nothing
        }
      }
    }
   SwingUtilities.invokeLater(() -> {
        roll1.setIcon(dieFaces[roll[0] - 1]);
        roll2.setIcon(dieFaces[roll[1] - 1]);
        rollLabel.setText(String.format(ROLL_FORMAT,roll[0] + roll[1]));
        rollLabel.setVisible(true);
   });
   }


  @Override
  public boolean continuePlay(int point) {
    JLabel pointLabel = this.point;
    SwingUtilities.invokeLater(() -> {
      pointLabel.setText(String.format(POINT_FORMAT,  point));
      pointLabel.setVisible(true);      
    });
    return getUserResponse();
  }

  @Override
  public boolean playAgain(int wins, int losses) {
    JLabel winsLabel = this.wins;
    JLabel lossesLabel = this.losses;
    JLabel pointLabel = this.point;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        pointLabel.setVisible(false);
        winsLabel.setText(String.format(WINS_FORMAT, wins));
        lossesLabel.setText(String.format(LOSSES_FORMAT, losses));
        winsLabel.setVisible(true);
        lossesLabel.setVisible(true);
      }        
    });
    return getUserResponse();
  }
  
  private synchronized boolean getUserResponse() {
    SwingUtilities.invokeLater(() -> enableButtons());
    while (!playClicked && !stopClicked) {
      try {
        wait();        
      } catch (InterruptedException ex) {
        // do nothing
      }
    }
    boolean result = playClicked;
    playClicked = false;
    return result;
  }
  
  private void enableButtons() {
    play.setEnabled(true);
    stop.setEnabled(true);
  }
  
  private void disableButtons() {
    play.setEnabled(true);
    stop.setEnabled(false);
  }
  
  private synchronized void resumePlay() {
    playClicked = true;
    notify();
  }
  private synchronized void stopPlay() {
    stopClicked = true;
    notify();
  }
  
  
  
}







