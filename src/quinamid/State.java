package quinamid;

import java.util.ArrayList;
import java.util.Arrays;

public class State {
    // game information
    private Board[] boards;
    public static final int WIN_SCORE = 500, LOSE_SCORE = -500;
    
    // board information
    private char[][] tools;
    private int[][] colors; // colors/hight
    
    // player information:
    private char turn; // 'b' = blue / 'r' = red
    
    // quick links:
    private ActionsManager actionManager;
    
    // minimax information:
    private int sumPlayerColors; // needs to be private
    
    public State(){
        this.boards = new Board[5];
        this.boards[0] = new Board(6, new Location(0, 0)); // main board
        this.boards[1] = new Board(5);
        this.boards[2] = new Board(4);
        this.boards[3] = new Board(3);
        this.boards[4] = new Board(2);
        
        this.tools = new char[6][6];
        this.colors = new int[6][6];
        
        this.actionManager = new ActionsManager(this.boards.length);
    }
    
    public State(State state){
        this.boards = new Board[state.boards.length];
        for(int i=0; i<this.boards.length; i++)
            this.boards[i] = new Board(state.boards[i]); // check
        
        this.tools = new char[state.tools.length][state.tools[0].length];
        this.colors = new int[state.colors.length][state.colors[0].length];
        for(int i=0; i<this.tools.length; i++){
            for(int j=0; j<this.tools[0].length; j++){
                this.tools[i][j] = state.tools[i][j];
                this.colors[i][j] = state.colors[i][j];
            }
        }
        this.turn = state.turn;
        this.actionManager = new ActionsManager(state.actionManager); // check
    }
    
    public void setup() {
        boards[0].setUp();
        actionManager.setup();
        actionManager.setMove(0, Consts.R_RIGHT);
        actionManager.setMove(0, Consts.R_LEFT);
        for (int i = 1; i < boards.length; i++) {
            int x = this.boards[i-1].getLocation().getX()+(int)(Math.random()*2);
            int y = this.boards[i-1].getLocation().getY()+(int)(Math.random()*2);
            this.boards[i].setLocation(new Location(x, y));
            
            if(x == boards[i-1].getLocationX()) // אתחול מערך ההזזות האפשריות של כל הלוחות
                actionManager.setMove(i, Consts.RIGHT);
            else
                actionManager.setMove(i, Consts.LEFT);
            
            if(y == boards[i-1].getLocationY())
                actionManager.setMove(i, Consts.DOWN);
            else
                actionManager.setMove(i, Consts.UP);
            
            actionManager.setMove(i, Consts.R_RIGHT);
            actionManager.setMove(i, Consts.R_LEFT);
            
            boards[i].setUp();
        }
        
        for(int i=0; i<tools.length; i++){ // אתחול מערך המקומות האפשריים להשמת כלי
            for(int j=0; j<tools[0].length; j++){
                actionManager.addPut(j, i);
            }
        }
        
        updateBoard();
        
        actionManager.setMove(-1, Consts.NO_DIRECTION);
        
        this.turn = Consts.RED;
        
        this.sumPlayerColors = 0;
    }
    
    public ActionsManager getActionManager(){
        return actionManager;
    }
    
    public char[][] getTools() {
        return tools;
    }
    
    public boolean isPutPossible(Location locateToSet){
        int x = locateToSet.getX();
        int y = locateToSet.getY();
        return tools[y][x] == Consts.NO_COLOR;
    }

    public void doPut(Location locateToSet) {
        int x = locateToSet.getX();
        int y = locateToSet.getY();
        
        if(tools[y][x] != Consts.NO_COLOR)
            System.out.println("<< !! attention: I putted a tool on another tool !! >>");
        tools[y][x] = turn;
        
        Board highestBoard = getHighestBoard(locateToSet);
        highestBoard.setGeneralTool(locateToSet, turn);
        
        actionManager.removePut(locateToSet.getX(), locateToSet.getY());
        
        sumPlayerColors += highestBoard.getHight() * ((turn==Consts.RED) ? 1 : -1);
    }
    
    public void doPut(Put currentPut){
        doPut(currentPut.getLocation());
    }
    
    public void undoPut(Location locateSetted){
        int x = locateSetted.getX();
        int y = locateSetted.getY();
        
        tools[y][x] = Consts.NO_COLOR;
        getHighestBoard(locateSetted).setGeneralTool(locateSetted, Consts.NO_COLOR);
        
        actionManager.addPut(locateSetted.getX(), locateSetted.getY());
        
        Board highestBoard = getHighestBoard(locateSetted);
        
        sumPlayerColors -= highestBoard.getHight() * ((turn==Consts.RED) ? 1 : -1);
    }
    
    public void undoPut(Put donePut){
        undoPut(donePut.getLocation());
    }

    public Board[] getBoards() {
        return boards;
    }
    
    public int[][] getColors() {
        return colors;
    }
    
    public char getTurn(){
        return turn;
    }
    
    public int eval(char player){
        final int scorePerSeriesSpot = 100; // 2 players * number of tools for full series (5) // 20
        final int scorePerHight = 1;
        final char enemy = (player==Consts.RED) ? Consts.BLUE : Consts.RED;
        
        int score = (State.WIN_SCORE + State.LOSE_SCORE) / 2;
        
        // here we calculate the potential of every player for doing series of 5 - every line:
        int mostPlayerSpots = 0, mostEnemySpots = 0;
        
        for(int i=0; i<tools.length; i++){  // horyzontal and vertical
            for(int j=0; j+4<tools[0].length; j++){
                
                int playerSpots = 0;
                int k=0;
                for(; k<5; k++){ // horizontal
                    if(tools[i][j+k] == player){
                        if(playerSpots < 0) // enemy's tools are in these 5
                            break;
                        playerSpots++;
                    }else if(tools[i][j+k] == enemy){
                        if(playerSpots > 0) // my tools are in these 5
                            break;
                        playerSpots--;
                    }
                }
                if(k==5){ // there is a series
                    mostPlayerSpots = Math.max(mostPlayerSpots, playerSpots);
                    mostEnemySpots = Math.max(mostEnemySpots, (-1)*playerSpots);
                }
                
                playerSpots = 0;
                k=0;
                for(; k<5; k++){ // vertical
                    if(tools[j+k][i] == player){
                        if(playerSpots < 0) // enemy's tools are in these 5
                            break;
                        playerSpots++;
                    }else if(tools[j+k][i] == enemy){
                        if(playerSpots > 0) // my tools are in these 5
                            break;
                        playerSpots--;
                    }
                }
                if(k==5){ // there is a series
                    mostPlayerSpots = Math.max(mostPlayerSpots, playerSpots);
                    mostEnemySpots = Math.max(mostEnemySpots, (-1)*playerSpots);
                }
                
            }
        }
        
        for(int i=0; i+4<tools.length; i++){
            int playerSpots = 0;
            int k=0;
            for(; k<5; k++){ // slant 1
                if(tools[i+k][i+k] == player){
                    if(playerSpots < 0) // enemy's tools are in these 5
                        break;
                    playerSpots++;
                }else if(tools[i+k][i+k] == enemy){
                    if(playerSpots > 0) // my tools are in these 5
                        break;
                    playerSpots--;
                }
            }
            if(k==5){ // there is a series
                mostPlayerSpots = Math.max(mostPlayerSpots, playerSpots);
                mostEnemySpots = Math.max(mostEnemySpots, (-1)*playerSpots);
            }
            
            playerSpots = 0;
            k=0;
            for(; k<5; k++){ // slant 2
                if(tools[i+k][tools.length-1-(i+k)] == player){
                    if(playerSpots < 0) // enemy's tools are in these 5
                        break;
                    playerSpots++;
                }else if(tools[i+k][tools.length-1-(i+k)] == enemy){
                    if(playerSpots > 0) // my tools are in these 5
                        break;
                    playerSpots--;
                }
            }
            if(k==5){ // there is a series
                mostPlayerSpots = Math.max(mostPlayerSpots, playerSpots);
                mostEnemySpots = Math.max(mostEnemySpots, (-1)*playerSpots);
            }
        }
        
        score += scorePerSeriesSpot*(mostPlayerSpots - mostEnemySpots);

        // adding colors:s
//        System.out.println("sum colors: "+sumPlayerColors);
        score += sumPlayerColors*scorePerHight;
//        System.out.println("eval is "+bestScore);
        
        return score;
    }
    
    private void updateBoard() {
        for(int i=0; i<boards.length; i++){
            int x = boards[i].getLocation().getX(); // start of board i (x)
            int y = boards[i].getLocation().getY(); // start of board i (y)
            for (int j = y; j < y+boards[i].getLength(); j++) {
                for (int k = x; k < x+boards[i].getLength(); k++) {
                    tools[j][k] = boards[i].getGenerallTool(new Location(k, j));
                    colors[j][k] = boards[i].getHight();
                }
            }
        }
        
        for(int i=0; i<tools.length; i++){
            for(int j=0; j<tools[0].length; j++){
                actionManager.removePut(j, i);
                if(tools[i][j] == '-')
                    actionManager.addPut(j, i);
            }
        }
    }
        
    public boolean isActionPossible(int boardIndx, char direction){
        Board currentBoard = boards[boardIndx];
        switch(direction){
            case Consts.UP: case Consts.DOWN: case Consts.RIGHT: case Consts.LEFT:
                return isMovePossible(currentBoard, direction);
            case Consts.R_RIGHT: case Consts.R_LEFT: case Consts.NO_DIRECTION:
                return true;
            default: // can be 'no action' - fix it
                System.out.println("<< state: didn't understand your action - \""+direction+"\" >>");
        }
        return false;
    }
    
    public void doAction(Board currentBoard, char direction){ // action = move / rotate
        //System.out.println("state:(2) making action: b:"+currentBoard.getHight()+" = "+currentBoard+", d:"+direction);
        switch(direction){
            case Consts.UP: case Consts.DOWN: case Consts.RIGHT: case Consts.LEFT:
                moveBoard(currentBoard, direction);
                break;
            case Consts.R_RIGHT: case Consts.R_LEFT:
                rotateBoard(currentBoard, direction);
                break;
            case Consts.NO_DIRECTION:
                // do nothing
                break;
            default: // can be 'no action' - fix it
                System.out.println("<< state: didn't understand your action - \""+direction+"\" >>");
        }
    }
    
    public void doAction(Move currentMove){
        if(currentMove.getDirection() != Consts.NO_DIRECTION){
            //System.out.println("state:(1) making action: b:"+currentMove.getBoard()+" = "+boards[currentMove.getBoard()]+", d:"+currentMove.getDirection());
            doAction(boards[currentMove.getBoard()], currentMove.getDirection());
        }
    }
    
    public void undoAction(Board movedboard, char direction){
        switch(direction){
            case Consts.UP:
                moveBoard(movedboard, Consts.DOWN);
                break;
            case Consts.DOWN:
                moveBoard(movedboard, Consts.UP);
                break;
            case Consts.RIGHT:
                moveBoard(movedboard, Consts.LEFT);
                break;
            case Consts.LEFT:
                moveBoard(movedboard, Consts.RIGHT);
                break;
            case Consts.R_RIGHT:
                rotateBoard(movedboard, Consts.R_LEFT);
                break;
            case Consts.R_LEFT:
                rotateBoard(movedboard, Consts.R_RIGHT);
                break;
            default: // can be 'no action' - fix it
                System.out.println("<< state: didn't understand your 'undo' action >>");
        }
    }
    
    public void undoAction(Move doneMove){
        if(doneMove.getDirection() != Consts.NO_DIRECTION){
            undoAction(boards[doneMove.getBoard()], doneMove.getDirection());
        }
    }
    
    public boolean isMovePossible(Board currentBoard, char direction){
        if(currentBoard.getHight() == 0){
            System.out.println("<< state: can't move the 0 board >>");
            return false; // can't move 0 board
        }
        switch(direction){
            case Consts.RIGHT:
                if(currentBoard.getLocationX() > 0 && currentBoard.getHight() - colors[currentBoard.getLocationY()][currentBoard.getLocationX()-1] == 1)
                    return false; // out of bounds
                break;
            case Consts.LEFT:
                if(currentBoard.getLocationX() == 0 || currentBoard.getHight() - colors[currentBoard.getLocationY()][currentBoard.getLocationX()-1] > 1)
                    return false; // out of bounds
                break;
            case Consts.UP:
                if(currentBoard.getLocationY() == 0 || currentBoard.getHight() - colors[currentBoard.getLocationY()-1][currentBoard.getLocationX()] > 1)
                    return false; // out of bounds
                break;
            case Consts.DOWN:
                if(currentBoard.getLocationY()> 0 && currentBoard.getHight() - colors[currentBoard.getLocationY()-1][currentBoard.getLocationX()] == 1)
                    return false; // out of bounds
                break;
            default:
                System.out.println("model: this move isn't available");
                return false;
        }
        return true;
    }
    
    public void moveBoard(Board currentBoard, char direction){
        if(currentBoard.getHight() == 0){
            System.out.println("<< state: I don't move the 0 high board >>");
            return;
        }
        //int newX = pressedBoard.getLocationX();
        //int newY = pressedBoard.getLocationY();
        switch(direction){
            case Consts.RIGHT:
                if(currentBoard.getLocationX() > 0 && currentBoard.getHight() - colors[currentBoard.getLocationY()][currentBoard.getLocationX()-1] == 1)
                    System.out.println("<< attention: board moved out of bounds (b:"+currentBoard.getHight()+", d:"+direction+") >>");
                for(int i=currentBoard.getHight(); i<boards.length; i++){
                    boards[i].moveX(1);
                }
                actionManager.swapHorizontalDirection(currentBoard.getHight());
                updateBoard(); // הסתבר לאחר ניסיון שלתת תווך ייקח יותר זמן מהסריקה עצמה
                break;
            case Consts.LEFT:
                if(currentBoard.getLocationX() == 0 || currentBoard.getHight() - colors[currentBoard.getLocationY()][currentBoard.getLocationX()-1] > 1)
                    System.out.println("<< attention: board moved out of bounds (b:"+currentBoard.getHight()+", d:"+direction+" from "+currentBoard.getLocation()+") >>");
                for(int i=currentBoard.getHight(); i<boards.length; i++){
                    boards[i].moveX(-1);
                }
                actionManager.swapHorizontalDirection(currentBoard.getHight());
                updateBoard(); // הסתבר לאחר ניסיון שלתת תווך ייקח יותר זמן מהסריקה עצמה
                break;
            case Consts.UP:
                if(currentBoard.getLocationY() == 0 || currentBoard.getHight() - colors[currentBoard.getLocationY()-1][currentBoard.getLocationX()] > 1)
                    System.out.println("<< attention: board moved out of bounds (b:"+currentBoard.getHight()+", d:"+direction+") >>");
                for(int i=currentBoard.getHight(); i<boards.length; i++){
                    boards[i].moveY(-1);
                }
                actionManager.swapVerticalDirection(currentBoard.getHight());
                updateBoard(); // הסתבר לאחר ניסיון שלתת תווך ייקח יותר זמן מהסריקה עצמה
                break;
            case Consts.DOWN:
                if(currentBoard.getLocationY()> 0 && currentBoard.getHight() - colors[currentBoard.getLocationY()-1][currentBoard.getLocationX()] == 1)
                    System.out.println("<< attention: board moved out of bounds (b:"+currentBoard.getHight()+", d:"+direction+") >>");
                for(int i=currentBoard.getHight(); i<boards.length; i++){
                    boards[i].moveY(1);
                }
                actionManager.swapVerticalDirection(currentBoard.getHight());
                updateBoard(); // הסתבר לאחר ניסיון שלתת תווך ייקח יותר זמן מהסריקה עצמה
                break;
            default:
                System.out.println("model: this move isn't available");
                return;
        }
    }
    
    public void rotateBoard(Board currentBoard, char direction){
        switch(direction){
            case Consts.R_RIGHT:
                currentBoard.rotateRight();
                for(int i=currentBoard.getHight()+1; i<boards.length; i++){
                    boards[i].rotateRight();
                    // שני השורות הבאות נלקחו אחרי חישובים מתמטיים של מרחקים סיבובים והפרשים בין גדלים
                    int newX = currentBoard.getLocationX()+(currentBoard.getLength()-boards[i].getLength())-(boards[i].getLocationY()-currentBoard.getLocationY());
                    int newY = currentBoard.getLocationY()+(boards[i].getLocationX()-currentBoard.getLocationX());
                    boards[i].setLocation(new Location(newX, newY));
                    if(boards[i].getLocationX() == boards[i-1].getLocationX())
                        actionManager.setHorizontalDirection(i, Consts.RIGHT);
                    else
                        actionManager.setHorizontalDirection(i, Consts.LEFT);
                    if(boards[i].getLocationY() == boards[i-1].getLocationY())
                        actionManager.setVerticalDirection(i, Consts.DOWN);
                    else
                        actionManager.setVerticalDirection(i, Consts.UP);
                }
                break;
            case Consts.R_LEFT:
                currentBoard.rotateLeft();
                for(int i=currentBoard.getHight()+1; i<boards.length; i++){
                    boards[i].rotateLeft();
                    // שני השורות הבאות נלקחו אחרי חישובים מתמטיים של מרחקים סיבובים והפרשים בין גדלים
                    int newX = currentBoard.getLocationX()+(boards[i].getLocationY()-currentBoard.getLocationY());
                    int newY = currentBoard.getLocationY()+(currentBoard.getLength()-boards[i].getLength())-(boards[i].getLocationX()-currentBoard.getLocationX());
                    boards[i].setLocation(new Location(newX, newY));
                    if(boards[i].getLocationX() == boards[i-1].getLocationX())
                        actionManager.setHorizontalDirection(i, Consts.RIGHT);
                    else
                        actionManager.setHorizontalDirection(i, Consts.LEFT);
                    if(boards[i].getLocationY() == boards[i-1].getLocationY())
                        actionManager.setVerticalDirection(i, Consts.DOWN);
                    else
                        actionManager.setVerticalDirection(i, Consts.UP);
                }
                break;
            default:
                System.out.println("model: this move isn't available");
                return;
        }
        
        updateBoard(); // הסתבר לאחר ניסיון שלתת תווך ייקח יותר זמן מהסריקה עצמה
    }
    
    public ArrayList<Location> checkWin(Location start, Location end){
        for(int i=start.getY(); i<=end.getY(); i++){
            for(int j=start.getX(); j<=end.getX(); j++){
                if(tools[i][j] == Consts.NO_COLOR)
                    continue;
                ArrayList<Location> winList = checkWin(new Location(j, i));
                if(winList != null)
                    return winList;
            }
        }
        
        return null;
    }
    
    public ArrayList<Location> checkWin(Location pressLoc){
        char pressTurn = tools[pressLoc.getY()][pressLoc.getX()];
        if(pressTurn == Consts.NO_COLOR){
            System.out.println("<< State: checkwin got empty space >>");
            return null;
        }
        int counter = 0; // how much times there are same chars in a row
        ArrayList<Location> arr = new ArrayList<Location>();
        
        // horizontal:
        for(int i=pressLoc.getX(); i<tools.length; i++){
            if(tools[pressLoc.getY()][i] != pressTurn)
                break;
            arr.add(new Location(i, pressLoc.getY()));
            counter++;
        }
        for(int i=pressLoc.getX()-1; i>=0; i--){
            if(tools[pressLoc.getY()][i] != pressTurn)
                break;
            arr.add(0, new Location(i, pressLoc.getY()));
            counter++;
        }
        if(counter>=5)
            return arr;
        
        // vertical:
        counter = 0;
        arr.clear();
        for(int i=pressLoc.getY(); i<tools.length; i++){
            if(tools[i][pressLoc.getX()] != pressTurn)
                break;
            arr.add(new Location(pressLoc.getX(), i));
            counter++;
        }
        for(int i=pressLoc.getY()-1; i>=0; i--){
            if(tools[i][pressLoc.getX()] != pressTurn)
                break;
            arr.add(0, new Location(pressLoc.getX(), i));
            counter++;
        }
        if(counter>=5)
            return arr;
        
        // slant 1:
        counter = 0;
        arr.clear();
        for(int i=pressLoc.getX(), j=pressLoc.getY(); i<tools.length && j<tools.length; i++, j++){
            if(tools[j][i] != pressTurn)
                break;
            arr.add(new Location(i, j));
            counter++;
        }
        for(int i=pressLoc.getX()-1, j=pressLoc.getY()-1; i>=0 && j>=0; i--, j--){
            if(tools[j][i] != pressTurn)
                break;
            arr.add(0, new Location(i, j));
            counter++;
        }
        if(counter>=5)
            return arr;
        
        // slant 2:
        counter = 0;
        arr.clear();
        for(int i=pressLoc.getX(), j=pressLoc.getY(); i<tools.length && j>=0; i++, j--){
            if(tools[j][i] != pressTurn)
                break;
            arr.add(new Location(i, j));
            counter++;
        }
        for(int i=pressLoc.getX()-1, j=pressLoc.getY()+1; i>=0 && j<tools.length; i--, j++){
            if(tools[j][i] != pressTurn)
                break;
            arr.add(0, new Location(i, j));
            counter++;
        }
        if(counter>=5)
            return arr;
        
        return null;
    }

    public Board getHighestBoard(Location location){
        for (int i = boards.length-1; i >= 0; i--) {
            if(boards[i].isInside(location)) return boards[i];
        }
        System.out.println("model: ***Problem: didn't find the highest board***");
        return null;
    }
    
    public Board getLowerBoard(Board upperBoard){
        if(upperBoard.getHight() > 0)
            return boards[upperBoard.getHight()-1];
        System.out.println("model: ***Problem: didn't find the lower board***");
        return null;
    }
    
    public void swapTurn(){
        if(turn==Consts.RED) turn = Consts.BLUE;
        else turn = Consts.RED;
    }
    
    @Override
    public String toString(){
        String str = "";
        str += "turn of: "+turn;
        str += "\n tools:             colors:\n";
        for (int i = 0; i < tools.length; i++) {
            str += Arrays.toString(tools[i])+" "+Arrays.toString(colors[i])+"\n";
        }
        return str;
    }
}
