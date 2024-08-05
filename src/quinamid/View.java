package quinamid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class View {

    private Controller controller;
    
    // view contains:
    private JFrame window;
    private JPanel frPanel;
    private JPanel boardPnl;
    private JLabel informationLabel;
    private JButton[][] brButtons;
    private JButton[] dirButtons;
    private JButton aiButton;
    private JButton newGameButton;
    private JButton randomMoveButton;
    
    // listeners:
    private ActionListener northListener; // need a new listener
    private MouseListener brListener;
    private KeyListener keyboardListnr;
    
    // board information:
    private char[][] barTools;
    private int[][] barColors;
    
    // view looks:
    private final Color[] COLOR_LIST_D = {new Color(1,2,160), new Color(1,49,88), new Color(7,83,53), new Color(29,105,0), new Color(150,0,0)}; // blue -> red
    private final Color[] COLOR_LIST_L = {new Color(90,50,15), new Color(120,68,31), new Color(149,96,43), new Color(176,119,64), new Color(210,165,108)}; // bright brown -> dark brown
    private Color[] colorsPackage;
    
    // icons:
    private final Icon BLUE_ICON = new ImageIcon(getClass().getResource("/icons/bc.png"));
    private final Icon RED_ICON = new ImageIcon(getClass().getResource("/icons/rc.png"));
    
    // public consts:
    public static final int DARK_MODE = 0, LIGHT_MODE = 1;
    
    public View(Controller controller) {
        this.controller = controller;
        
        this.window = new JFrame();
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.window.setSize(700, 800);
        this.window.setLocation(1100, 100);
        
        this.frPanel = new JPanel(); // panel of the frame
        this.frPanel.setLayout(new BorderLayout(1, 1));
        this.window.add(this.frPanel);
        
        // menu bar:
        // הגדרות
        // minimax time
        JMenuBar menuBar = new JMenuBar();
        
        JMenu helpM = new JMenu("help");
        JMenuItem howToPlayMI = new JMenuItem("how to play");
        howToPlayMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frPanel, "<html><h1>Quinamid:</h1></html>"
                        + "\nAn abstract 2-player game in which the objective is to create a line of 5 counters."
                        + "\nThe twist here is that the counters are placed in a series of 5 boards of differing size that are formed to make a pyramid, hence the name ‘Quinamid’."
                        + "\n"
                        + "\nThe rules are simple, any player in his turn needs to do the following steps:"
                        + "\nPlace a counter in a vacant space"
                        + "\nthen, theplayer can in addition (if he wants to) do one of the following actions:"
                        + "\na) Rotate a board (including those boards ‘above’ it) through 90 degrees"
                        + "\nb) Move a board (including those boards ‘above’ it) one ‘position’ in any orthogonal direction."
                        + "\nPlayers take it in turns to make their move until one of them reveals a line of 5 counters, this being in a vertical, horizontal or diagonal direction."
                        + "\n"
                        + "\nAs players use option b) above they will ‘cover’ boards below, thereby hiding the contents. It follows therefore that there is a bit of memory required to recall what was under the various boards if you are to use it to your advantage."
                        + "\n"
                        + "\nA game is often completed in 2 or 3 minutes so replay value is high. It is often better to play a ‘best of 5’ series to employ different strategies, so that would take around 15 minutes."
                        + "\n"
                        + "\nThe game should appeal to players aged 8-80. It could be played whilst waiting for a meal to be prepared or as a post-prandial diversion with other players spectating and giving out ‘advice’ ! Easy until they themselves have a go !!"
                        , "how to play", JOptionPane.PLAIN_MESSAGE);
            }
        });
        JMenuItem shortcutsMI = new JMenuItem("keyboard shortcuts");
        shortcutsMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frPanel, "<html><h1>keyboard shortcuts:</h1></html>"
                        + "\n\n<html><h3>up / down / left / right keys -> slide board</h3></html>"
                        + "\n\n<html><h3>&lt / &gt keys -> rotate board</h3></html>"
                        + "\n\n<html><h3>space key -> don't do any move</h3></html>"
                        , "keyboard shortcuts", JOptionPane.PLAIN_MESSAGE);
            }
        });
        JMenuItem aboutMI = new JMenuItem("about");
        aboutMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frPanel, "<html><h1>About:</h1></html>"
                        + "\n<html><h3>Name: quinamid</h3></html>"
                        + "\n<html><h3>Designer: Aharon Hamamian</h3></html>"
                        , "about", JOptionPane.PLAIN_MESSAGE);
            }
        });
        helpM.add(howToPlayMI);
        helpM.add(shortcutsMI);
        helpM.add(aboutMI);
        menuBar.add(helpM);
        
        JMenu settingsM = new JMenu("settings");
        JMenuItem darkWthiteMI = new JMenuItem((colorsPackage == COLOR_LIST_L) ? "dark mode" : "light mode");
        darkWthiteMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(colorsPackage == COLOR_LIST_L){ // ((JMenuItem)(e.getSource())).getText().equals("dark mode")
                    colorsPackage = COLOR_LIST_D;
                    ((JMenuItem)(e.getSource())).setText("light mode");
                }else{
                    colorsPackage = COLOR_LIST_L;
                    ((JMenuItem)(e.getSource())).setText("dark mode");
                }
                updateBoardColors();
            }
        });
        JMenuItem aiTime = new JMenuItem("ai time");
        aiTime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = "AI time:";
                int timeNum = -1;
                do{
                    String timeStr = (String)JOptionPane.showInputDialog(
                            frPanel,
                            message,
                            "change AI time",
                            JOptionPane.PLAIN_MESSAGE,
                            null, null,
                            "10");
                    if(timeStr == null)
                        return;
                    timeNum = Integer.parseInt(timeStr);
                    if(timeNum <= 0)
                        message = "AI time:\n<html><div style = 'color:red'>Attention: time needs to be 1 second and above.</div></html>";
                }while(timeNum <= 0);
                controller.changeAiTime(timeNum);
            }
        });
        settingsM.add(darkWthiteMI);
        settingsM.add(aiTime);
        
//        settingsM.add(new JSeparator());
//        settingsM.add(new JMenuItem("after seperator"));
        JMenuItem item = new JMenuItem("action");
//        item.addActionListener(listener);
        menuBar.add(settingsM);
        
        window.setJMenuBar(menuBar);
        
        // north area:
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout());
        northListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton pressedButton = (JButton)e.getSource();
                controller.commandPressed(pressedButton.getActionCommand());
            }
        };
        aiButton = new JButton("AI Move");
        aiButton.setActionCommand("AI Move");
        aiButton.addActionListener(northListener);
        aiButton.setFocusable(false);
        
        newGameButton = new JButton("New Game");
        newGameButton.setActionCommand("New Game");
        newGameButton.addActionListener(northListener);
        newGameButton.setFocusable(false);
        
        randomMoveButton = new JButton("Random Move");
        randomMoveButton.setActionCommand("Random Move");
        randomMoveButton.addActionListener(northListener);
        randomMoveButton.setFocusable(false);
        
        northPanel.add(aiButton);
        northPanel.add(newGameButton);
        northPanel.add(randomMoveButton);
        northPanel.setFocusable(false);
        this.frPanel.add(northPanel, BorderLayout.NORTH);
       
        this.boardPnl = new JPanel();   // the panel where we play the game
        this.boardPnl.setLayout(new GridLayout(6, 6));
        this.boardPnl.setPreferredSize(new Dimension(700, 700));
        keyboardListnr = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                // action:
                switch(e.getKeyCode()){
                    case KeyEvent.VK_LEFT:
                        controller.directionPressed("move left");
                        break;
                    case KeyEvent.VK_RIGHT:
                        controller.directionPressed("move right");
                        break;
                    case KeyEvent.VK_UP:
                        controller.directionPressed("move up");
                        break;
                    case KeyEvent.VK_DOWN:
                        controller.directionPressed("move down");
                        break;
                    case 46: // > key
                        controller.directionPressed("rotate right");
                        break;
                    case 44: // < key
                        controller.directionPressed("rotate left");
                        break;
                    case 32: // 'space' key
                        controller.directionPressed("no direction");
                        break;
                    default:
                        System.out.println("<< view: keyListener: don't know this key (code: "+e.getKeyCode()+")>>");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        };
        this.boardPnl.setFocusable(true);
        this.frPanel.add(this.boardPnl);
        
        // board buttons:
        brListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JButton source = (JButton)e.getSource();
                if((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) // if left button clicked
                    controller.movePressed(source.getActionCommand());
                else if((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) // if right button clicked
                    controller.putToolPressed(((JButton)(e.getSource())).getActionCommand());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        this.brButtons = new JButton[6][6];
        window.setVisible(true);
        for (int i = 0; i < brButtons.length; i++) {
            for (int j = 0; j < brButtons[0].length; j++) {
                brButtons[i][j] = new JButton();
                brButtons[i][j].setFont(new Font("", Font.BOLD, 25));
                brButtons[i][j].setActionCommand(j+","+i);
                brButtons[i][j].addMouseListener(brListener);
                brButtons[i][j].setFocusable(false);
                this.boardPnl.add(brButtons[i][j]);
            }
        }
        
        JPanel southPanel = new JPanel();        // הפנל הדרומי שבו יהיה את כפתורי הכיוון והאינפורמציה
        // direction buttons
        JPanel dirPanel = new JPanel();           // direction panel
        dirPanel.setLayout(new FlowLayout());
        dirButtons = new JButton[6];
        dirButtons[0] = new JButton("move up");
        dirButtons[1] = new JButton("move down");
        dirButtons[2] = new JButton("move left");
        dirButtons[3] = new JButton("move right");
        dirButtons[4] = new JButton("rotate left");
        dirButtons[5] = new JButton("rotate right");
        ActionListener dirListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton source = (JButton)e.getSource();
                controller.directionPressed(source.getActionCommand());
            }
        };
        for(int i=0; i<dirButtons.length; i++){
            dirButtons[i].addActionListener(dirListener);
            dirPanel.add(dirButtons[i]);
        }
        JPanel infoPanel = new JPanel();         // information panel
        dirPanel.setLayout(new FlowLayout());
        informationLabel = new JLabel();
        informationLabel.setFont(new Font("", 0, 20));
        infoPanel.add(informationLabel);
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(dirPanel);
        southPanel.add(infoPanel);
        this.frPanel.add(southPanel,BorderLayout.SOUTH);
        
        window.pack();
    }
    
    public void setup(char[][] tools, int[][] colors, int mode){
        switch(mode){
            case DARK_MODE:
                setup1(tools, colors);
                break;
            case LIGHT_MODE:
                setup2(tools, colors);
                break;
            default:
                System.out.println("<< View: didn't get your mode >>");
        }
    }
    
    private void setup1(char[][] tools, int[][] colors) { // dark mode
        window.setVisible(true);
        
        colorsPackage = COLOR_LIST_D;
        
        barColors = new int[colors.length][colors[0].length];
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[0].length; j++) {
//                brButtons[i][j].setBackground(new Color(50*colors[i][j], 255-50*colors[i][j], 255-50*colors[i][j]));
                barColors[i][j] = colors[i][j];
                brButtons[i][j].setBackground(colorsPackage[colors[i][j]]);
                brButtons[i][j].setForeground(colorsPackage[colors[i][j]].brighter());
            }
        }
        
        barTools = new char[tools.length][tools[0].length];
        for (int i = 0; i < tools.length; i++) {
            for (int j = 0; j < tools[0].length; j++) {
                barTools[i][j] = tools[i][j];
                if(tools[i][j] == Consts.NO_COLOR){
                    brButtons[i][j].setText("+");
                    brButtons[i][j].setIcon(null);
                }
                else{
                    brButtons[i][j].setText(""+tools[i][j]);
                    brButtons[i][j].setForeground(Color.yellow);
                }
            }
        }
        
        disableDirection();
    }
    
    private void setup2(char[][] tools, int[][] colors) { // light mode
        window.setVisible(true);
        
        colorsPackage = COLOR_LIST_L;
        
        barColors = new int[colors.length][colors[0].length];
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[0].length; j++) {
//                brButtons[i][j].setBackground(new Color(50*colors[i][j], 255-50*colors[i][j], 255-50*colors[i][j]));
                barColors[i][j] = colors[i][j];
                brButtons[i][j].setBackground(colorsPackage[colors[i][j]]);
                brButtons[i][j].setForeground(colorsPackage[colors[i][j]].brighter());
            }
        }
        
        barTools = new char[tools.length][tools[0].length];
        for (int i = 0; i < tools.length; i++) {
            for (int j = 0; j < tools[0].length; j++) {
                barTools[i][j] = tools[i][j];
                if(tools[i][j] == Consts.NO_COLOR){
                    brButtons[i][j].setText("+");
                    brButtons[i][j].setIcon(null);
                }
                else{
                    brButtons[i][j].setText(""+tools[i][j]);
                    brButtons[i][j].setForeground(Color.yellow);
                }
            }
        }
        
        disableDirection();
    }

    public void updateBoard(Location locateToSet, char turn) {
        int x = locateToSet.getX();
        int y = locateToSet.getY();
        barTools[y][x] = turn;
        brButtons[y][x].setText(turn+"");
        brButtons[y][x].setForeground(Color.yellow);
        
        // test
        brButtons[y][x].setText(null);
        switch(turn){
            case Consts.RED:
                brButtons[y][x].setIcon(RED_ICON);
                break;
            case Consts.BLUE:
                brButtons[y][x].setIcon(BLUE_ICON);
                break;
            default:
                System.out.println("<< view->updateBoard1: didn't get your tool >>");
        }
    }
    
    public void updateBoard(char[][] tools, int[][] colors, Location start, Location end){
        // colors and tools:
        for (int i = start.getY(); i <= end.getY(); i++) {
            for (int j = start.getX(); j <= end.getX(); j++) {
                barColors[i][j] = colors[i][j];
                brButtons[i][j].setBackground(colorsPackage[colors[i][j]]);
                brButtons[i][j].setForeground(colorsPackage[barColors[i][j]].brighter());
                
                barTools[i][j] = tools[i][j];
                if(tools[i][j] == Consts.NO_COLOR){
                    brButtons[i][j].setText("+");
                    brButtons[i][j].setIcon(null);
                }
                else{
                    brButtons[i][j].setText(""+tools[i][j]);
                    brButtons[i][j].setForeground(Color.yellow);
                    
                    // test
                    brButtons[i][j].setText(null);
                    switch(tools[i][j]){
                        case Consts.RED:
                            brButtons[i][j].setIcon(RED_ICON);
                            break;
                        case Consts.BLUE:
                            brButtons[i][j].setIcon(BLUE_ICON);
                            break;
                        default:
                            System.out.println("<< view->updateBoard2: didn't get your tool >>");
                    }
                }
            }
        }
    }
    
    private void updateBoardColors(){ // updates the colors
        // colors and tools:
        for (int i = 0; i < barColors.length; i++) {
            for (int j = 0; j < barColors[0].length; j++) {
                brButtons[i][j].setBackground(colorsPackage[barColors[i][j]]);
                if(barTools[i][j] == Consts.NO_COLOR)
                    brButtons[i][j].setForeground(colorsPackage[barColors[i][j]].brighter());
            }
        }
    }
    
    public void disableBoardButtons(){
        System.out.println("!! board disabled");
        for(int i=0; i<brButtons.length; i++){
            for(int j=0; j<brButtons[0].length; j++){
                for (MouseListener mouseListener : brButtons[i][j].getMouseListeners()) {
                    if(mouseListener.equals(brListener))
                        brButtons[i][j].removeMouseListener(mouseListener);
                }
            }
        }
    }
    
    public void enableBoardButtons(){
        System.out.println("!! board enabled");
        for(int i=0; i<brButtons.length; i++){
            for(int j=0; j<brButtons[0].length; j++){
                boolean foundListener = false;
                for(MouseListener mouseListener : brButtons[i][j].getMouseListeners()){
                    if(mouseListener.equals(brListener)){
                        foundListener = true;
                        break;
                    }
                }
                if(!foundListener)
                    brButtons[i][j].addMouseListener(brListener);
            }
        }
    }
    
    public void enableDirection(){
        for(int i=0; i<dirButtons.length; i++)
            dirButtons[i].setEnabled(true);
        if(this.boardPnl.getKeyListeners().length == 0)
            this.boardPnl.addKeyListener(keyboardListnr);
    }
    
    public void disableDirection(){
        for(int i=0; i<dirButtons.length; i++)
            dirButtons[i].setEnabled(false);
        this.boardPnl.removeKeyListener(keyboardListnr);
    }
    
    public void print(String text){
        informationLabel.setText(text);
    }
    
    public int showWinner(char player){
        disableBoardButtons();
        int dialogResult = JOptionPane.showConfirmDialog(this.window, "game over, "+player+" won.\ndo you want to start again?");
        return dialogResult;
    }
    
}