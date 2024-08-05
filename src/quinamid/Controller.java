package quinamid;

import java.util.ArrayList;
import javax.swing.JOptionPane;

public class Controller {
    
    private static Model model;
    private static View view;
    private static State currentState;
    private int waitingBoard; // index of the board that is waiting for direction to be detected
    private boolean putMade;
    private int minimxTime;
    
    public static void main(String[] args){
        Controller controller = new Controller();
        controller.startNewGame();
    }
    
    public Controller(){
        this.model = new Model(this);
        this.currentState = this.model.getState();
        this.view = new View(this);
    }
    
    private void startNewGame() {
        this.currentState.setup();
        this.view.setup(this.currentState.getTools(), this.currentState.getColors(), View.DARK_MODE);
        this.view.enableBoardButtons();
        System.out.println(currentState.toString());
        view.print("turn: "+currentState.getTurn());
        putMade = false;
        minimxTime = 10;
    }
    
    public boolean putTool(Location locateToSet) { // button pressed to set tool
        if(putMade){
            System.out.println("<< controller: put already made, make a move or press <space> to continue >>");
            return false;
        }
        System.out.println("controller: put tool in: "+locateToSet.toString());
        // move:
        if(currentState.isPutPossible(locateToSet))
            currentState.doPut(locateToSet);
        else{
            System.out.println("<< controller: you can't do that put >>");
            return false;
        }
        
        // update the board:
        view.updateBoard(locateToSet, currentState.getTurn());
        
        // check:
        
        putMade = true;
        
        boolean thereIsWinner = checkWin();
        return thereIsWinner;
    }
    
    public boolean makeBoardAction(int boardIndx, char direction){ // action = rotate/move
        if(!putMade){
            System.out.println("<< controller: you didn't do put yet >>");
            return false;
        }
        System.out.println("controller: making action: "+direction+" for: "+boardIndx);
        Board currentBoard = currentState.getBoards()[boardIndx];
        if(currentState.isActionPossible(boardIndx, direction))
            currentState.doAction(currentBoard, direction);
        else{
            System.out.println("<< controller: you can't do that move >>");
            return false;
        }
        view.updateBoard(currentState.getTools(), currentState.getColors(), new Location(0, 0), new Location(5, 5));
        
        boolean thereIsWinner = checkWin();
        if(thereIsWinner)
            return true;
        
        swapTurn();
        return false;
    }
    
    public void makeRandomMove(){
        // put:
        Put randPut = currentState.getActionManager().getRandomPut();
        
        boolean thereIsWinner = putTool(randPut.getLocation());
        if(thereIsWinner)
            return;
        
        // move:
        Move randMove = currentState.getActionManager().getRandomMove();
        
        if(randMove.getDirection() != Consts.NO_DIRECTION)
            makeBoardAction(randMove.getBoard(), randMove.getDirection());
    }
    
    public void commandPressed(String command){ // the buttons in the north area
        if(putMade){
            swapTurn();
        }
        switch(command){
            case "New Game":
                startNewGame();
                break;
            case "AI Move":
                System.out.println("\n--- MiniMax Test ---\n");
                
                Action minimaxAction = model.getMinimaxAction(new State(currentState), State.LOSE_SCORE, State.WIN_SCORE, 0, Model.MINIMAX_DEPTH);
//                Action minimaxAction = model.getMinimaxInTime(new State(currentState), minimxTime);
                
                System.out.println("\n--- MiniMax done ---\n");
                System.out.println("");
                System.out.println("best action is:\n"+minimaxAction);
                System.out.println("");
                
                if(minimaxAction.getPut() != null || minimaxAction.getMove() != null){
                    Put mPut = minimaxAction.getPut();
                    boolean thereIsWinner = putTool(mPut.getLocation());
                    if(thereIsWinner)
                        return;
                    Move mMove = minimaxAction.getMove();
                    if(mMove.getDirection() != Consts.NO_DIRECTION)
                        makeBoardAction(mMove.getBoard(), mMove.getDirection());
                }
                else
                    System.out.println("<< Controller->AI move: i got null from minimax >>");
                
                break;
            case "Random Move":
                makeRandomMove();                
                break;
            default:
                System.out.println("controller: didn't understand your command");
        }
    }
    
    public void putToolPressed(String command){
        System.out.println("button "+command+" pressed");
        
        String[] commandSp = command.split(",");
        int x = Integer.parseInt(commandSp[0]);
        int y = Integer.parseInt(commandSp[1]);
        Location locateToSet = new Location(x, y);
        if(currentState.isPutPossible(locateToSet)){
            putTool(locateToSet);

            waitingBoard = 0; // automaticly
            view.enableDirection();
        }else{
            System.out.println("<< controller: couldn't do that put >>");
        }
        
        System.out.println("eval is: "+currentState.eval(Consts.RED));
    }

    public void movePressed(int boardIndx){
        waitingBoard = boardIndx;
        view.enableDirection();
    }
    public void movePressed(String command) { // button pressed to show who do you want to move
        if(!putMade){
            System.out.println("<< controller: you didn't do put yet >>");
            return;
        }
        String[] commandSp = command.split(",");
        int x = Integer.parseInt(commandSp[0]);
        int y = Integer.parseInt(commandSp[1]);
        waitingBoard = currentState.getHighestBoard(new Location(x, y)).getHight();
//        view.enableDirection();
    }
    
    public void directionPressed(String command) { // button pressed to show where to move (after we decide who to move)
        String[] commandSp = command.split(" ");
        char direction = '0';
        switch(command){
            case "move left":
                direction = Consts.LEFT;
                break;
            case "move right":
                direction = Consts.RIGHT;
                break;
            case "move up":
                direction = Consts.UP;
                break;
            case "move down":
                direction = Consts.DOWN;
                break;
            case "rotate left":
                direction = Consts.R_LEFT;
                break;
            case "rotate right":
                direction = Consts.R_RIGHT;
                break;
            case "no direction":
                swapTurn();
                return;
            default:
                System.out.println("<< controller: didn't get an appropriate direction >>");
        }
        if(currentState.isActionPossible(waitingBoard, direction)){
            makeBoardAction(waitingBoard, direction);
        }else{
            System.out.println("<< controller: couldn't do that action >>");
        }
    }
    
    public boolean checkWin(){
//        ArrayList<Location> winList = currentState.checkWin(currentState.getTurn(), new Location(0, 0), new Location(5, 5));
        ArrayList<Location> winList = currentState.checkWin(new Location(0, 0), new Location(5, 5));
        if(winList != null){
            System.out.println(currentState.getTurn()+" is a winner:");
            System.out.println(winList.toString());
            System.out.println("");
            char winnerTurn = currentState.getTools()[winList.get(0).getY()][winList.get(0).getX()];
            int answer = view.showWinner(winnerTurn);
            if(answer == JOptionPane.YES_OPTION)
                startNewGame();
            else if(answer == JOptionPane.NO_OPTION)
                System.exit(0);
            return true;
        }
        return false;
    }
    
    public void changeAiTime(int time){
        minimxTime = time;
    }
    
    public void swapTurn(){
        currentState.swapTurn();
        view.print("turn: "+currentState.getTurn());
        putMade = false;
        view.disableDirection();
    }
    
}