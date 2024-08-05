package quinamid;

import java.util.ArrayList;

public class ActionsManager {

    private ArrayList<Move> possibleMoves;
    private ArrayList<Put> possiblePuts;
    
    private Put[][] puts;   // table of links with puts, the key here is the location ([y][x])
    private Move[][] moves; // array of links with moves, the key here is the board ([board index][0=horizontalM/1=verticalM/2=rotateR/3=rotateLeft])
    private static final int HOR_MOV_INDX=0, VER_MOV_INDX=1, ROT_RIGHT_INDX=2, ROT_LEFT_INDX=3; // help indexes for 'moves' array
    
    public ActionsManager(int numOfBoards){
        possibleMoves = new ArrayList<Move>(); // צריך להוסיף אפשרויות
        possiblePuts = new ArrayList<Put>();   // צריך להוסיף אפשרויות
        
        this.puts = new Put[6][6];
        for(int i=0; i<puts.length; i++){
            for(int j=0; j<puts[0].length; j++){
                setPut(new Location(j, i));
            }
        }
        
        this.moves = new Move[numOfBoards][4]; // boards.length
        
        setup();
    }
    
    public ActionsManager(ActionsManager actionManager){        
        this.puts = new Put[actionManager.puts.length][actionManager.puts[0].length];
        for(int i=0; i<this.puts.length; i++){
            for(int j=0; j<this.puts[0].length; j++){
                this.puts[i][j] = new Put(actionManager.puts[i][j]);
            }
        }
        this.possiblePuts = new ArrayList<Put>();
        for(int i=0; i<actionManager.possiblePuts.size(); i++){
            Put copyPut = actionManager.possiblePuts.get(i);
            this.possiblePuts.add(this.puts[copyPut.getLocation().getY()][copyPut.getLocation().getX()]);
        }
        
        this.moves = new Move[actionManager.moves.length][actionManager.moves[0].length];
        this.possibleMoves = new ArrayList<Move>();
        
        for(int i=0; i<actionManager.possibleMoves.size(); i++){
            Move copyMove = actionManager.possibleMoves.get(i);
            setMove(copyMove.getBoard(), copyMove.getDirection());
        }
    }
    
    public void setup(){
        possibleMoves.clear();
        possiblePuts.clear();
        
        for(int i=0; i<moves.length; i++){
            for(int j=0; j<moves[0].length; j++){
                moves[i][j] = null;
            }
        }
    }
    
    public void setMove(int boardNum, char direction){
        switch(direction){
            case Consts.RIGHT:
            case Consts.LEFT:
                moves[boardNum][HOR_MOV_INDX] = new Move(boardNum, direction);
                possibleMoves.add(moves[boardNum][HOR_MOV_INDX]);
                break;
            case Consts.UP:
            case Consts.DOWN:
                moves[boardNum][VER_MOV_INDX] = new Move(boardNum, direction);
                possibleMoves.add(moves[boardNum][VER_MOV_INDX]);
                break;
            case Consts.R_LEFT:
                moves[boardNum][ROT_LEFT_INDX] = new Move(boardNum, Consts.R_LEFT);
                possibleMoves.add(moves[boardNum][ROT_LEFT_INDX]);
                break;
            case Consts.R_RIGHT:
                moves[boardNum][ROT_RIGHT_INDX] = new Move(boardNum, Consts.R_RIGHT);
                possibleMoves.add(moves[boardNum][ROT_RIGHT_INDX]);
                break;
            case Consts.NO_DIRECTION:
                possibleMoves.add(new Move(-1, Consts.NO_DIRECTION));
                break;
            default: 
                System.out.println("ActionManager: can't recognize your action");
        }
    }
    
    private void setPut(Location loc){
        puts[loc.getY()][loc.getX()] = new Put(loc);
        possiblePuts.add(puts[loc.getY()][loc.getX()]);
    }
    
    public ArrayList<Move> getAllPossibleMoves(){
        return possibleMoves;
    }
    
    public Move getRandomMove(){
        int index = (int)(Math.random()*possibleMoves.size());
        return possibleMoves.get(index);
    }
    
    public void changeMove(int index){
        Move exchMove = possibleMoves.get(index); // move to remove
        exchMove.swapDirection();
    }
    
    public  ArrayList<Put> getAllPossiblePuts(){
        return possiblePuts;
    }
    
    public Put getRandomPut(){
        int index = (int)(Math.random()*possiblePuts.size());
        return possiblePuts.get(index);
    }
    
    public void removePut(int index){
        possiblePuts.remove(index);
//        if(possiblePuts.isEmpty())
//            System.out.println("<< possiblePuts array is empty >>");
    }
    
    public void removePut(int x, int y){
        possiblePuts.remove(puts[y][x]);
//        if(possiblePuts.isEmpty())
//            System.out.println("<< possiblePuts array is empty >>");
    }
    
    public void addPut(int x, int y){
        possiblePuts.add(puts[y][x]);
    }
    
    public void swapHorizontalDirection(int boardNum){
        moves[boardNum][HOR_MOV_INDX].swapDirection();
    }
    
    public void setHorizontalDirection(int boardNum, char direction){
        moves[boardNum][HOR_MOV_INDX].setDirection(direction);
    }
    
    public void swapVerticalDirection(int boardNum){
        moves[boardNum][VER_MOV_INDX].swapDirection();
    }
    
    public void setVerticalDirection(int boardNum, char direction){
        moves[boardNum][VER_MOV_INDX].setDirection(direction);
    }
    
}
