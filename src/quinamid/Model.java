package quinamid;

import java.util.ArrayList;

public class Model {
    private Controller controller;
    private State state;
    
//    public static final int WIN_SCORE = 100, LOSE_SCORE = 0;
    public static final int MINIMAX_DEPTH = 2;
    
    public Model(Controller controller){
        this.controller = controller;
        state = new State();
    }
    
    /**
     * return the current state of the game
     * @return 
     */
    public State getState(){
        return state;
    }
    
    /**
     * reduce an amount of action from the given array lists
     * @param possibleMoves - possible moves that the player can do
     * @param possiblePuts - possible puts that the player can do
     */
    public void reduceMoves(ArrayList<Move> possibleMoves, ArrayList<Put> possiblePuts){
        // puts:
//        boolean[] hightFull = new boolean[state.getTools().length];
//        for(int i=0; i<possiblePuts.size(); i++){
//            
//        }

        // moves:
        for(int i=0; i<possibleMoves.size(); i++){
            Move currentMove = possibleMoves.get(i);
            if(currentMove.getBoard() == 0){
                possibleMoves.remove(currentMove);
                i--; // כדי שלא ידלג על האיבר הבא אחריו
//                System.out.println("removed "+currentMove.toString());
            }
        }
    }
    
    // לא בשימוש בגלל איטיות אבל עדיין יכול לעבוד
    /**
     * reduce an amount of action from the given array lists,
     * + uses the current state to see placec that are close to peacec on the board
     * @param currentState - current state of the game
     * @param possibleMoves - possible moves that the player can do
     * @param possiblePuts - possible puts that the player can do
     */
    public void reduceMoves(State currentState, ArrayList<Move> possibleMoves, ArrayList<Put> possiblePuts){
        // puts:
        ArrayList<Put> newArray = new ArrayList<Put>();
        
        int[][] currentColors = currentState.getColors();
        ArrayList<Put> highestPuts = new ArrayList<Put>();
        int biggestHight = 0;
        for(int i=0; i<possiblePuts.size(); i++){
            Put currentPut = possiblePuts.get(i);
            int putHight = currentColors[currentPut.getLocation().getY()][currentPut.getLocation().getX()];
            if(putHight > biggestHight){
                biggestHight = putHight;
                highestPuts.clear();
                highestPuts.add(currentPut);
            }else if(putHight == biggestHight){
                highestPuts.add(currentPut);
            }
        }
        newArray.addAll(highestPuts);
        
        char[][] currentTools = currentState.getTools();
        ArrayList<Put> neighbors = new ArrayList<Put>();
        for(int i=0; i<possiblePuts.size(); i++){
            Put currentPut = possiblePuts.get(i);
            if(newArray.contains(currentPut))
                continue;
            Location putLoc = currentPut.getLocation();
            for(int j=putLoc.getX()-1; j<=putLoc.getX()+1; j++){
                if(j<0 || j>=currentTools.length)
                    continue;
                boolean breakLoop = false;
                for(int k=putLoc.getY()-1; k<=putLoc.getY()+1; k++){
                    if(k<0 || k>=currentTools.length || (j==putLoc.getX() && k==putLoc.getY()))
                        continue;
                    if(currentTools[k][j]!=Consts.NO_COLOR){
                        neighbors.add(currentPut);
                        breakLoop = true;
                        break;
                    }
                }
                if(breakLoop) break;
            }
        }
        newArray.addAll(neighbors);

        // moves:
        for(int i=0; i<possibleMoves.size(); i++){
            Move currentMove = possibleMoves.get(i);
            if(currentMove.getBoard() == 0){
                possibleMoves.remove(currentMove);
                i--; // כדי שלא ידלג על האיבר הבא אחריו
//                System.out.println("removed "+currentMove.toString());
            }
        }
    }
    
    /**
     * Searches for the best move that the player can do.
     * This function is with limitation for time as long as the user wants so the search won't take too long.
     * @param currentState - current state of the game
     * @param seconds - time limit for Minimax algorithm.
     * @return best action the player can do
     */
    public Action getMinimaxInTime(State currentState, int seconds){
        long totalTime = seconds * 1000;
        long t1, t2;
        System.out.println("total time: "+ totalTime);
        
        int maxDepth = MINIMAX_DEPTH;
        
        Action bestAction = null;
        
        while(totalTime > 0){
            t1 = System.currentTimeMillis();
            
            System.out.println("\nminimax depth -> "+maxDepth);
            bestAction = getMinimaxAction(currentState, State.LOSE_SCORE, State.WIN_SCORE, 0, maxDepth);
            if(currentState.getTurn() == Consts.RED && bestAction.getScore() == State.WIN_SCORE)
                return bestAction;
            else if(currentState.getTurn() == Consts.BLUE && bestAction.getScore() == State.LOSE_SCORE)
                return bestAction;
            
            t2 = System.currentTimeMillis();
            totalTime -= t2 - t1;
            System.out.println("total time: "+totalTime);
            
            maxDepth++;
        }
        
        return bestAction;
    }
    
    /**
     * Searches for the best move that the player can do.
     * The serach is done by Minimax algorithm.
     * @param currentState - current state of the game
     * @param alpha - lower limit for score.
     * @param beta - upper limit for score.
     * @param depth - current depth for Minimax algorithm.
     * @param maxDepth - maximum depth for Minimax algorithm.
     * @return the best action the player can do
     */
    public Action getMinimaxAction(State currentState, int alpha, int beta, int depth, int maxDepth){ // גרסה לא מוכנה שאמורה להחזיר פעולה במקום ציון
        Action bestAction = null; // אתחול ראשוני
        
        if(depth >= maxDepth){
            int score = currentState.eval(Consts.RED);
//            int score = 50;
            return new Action(score, depth);
        }
        
        ArrayList<Put> copyPuts = currentState.getActionManager().getAllPossiblePuts();
        ArrayList<Put> possiblePuts = new ArrayList<Put>();
        for(int i=0; i<copyPuts.size(); i++){
            possiblePuts.add(new Put(copyPuts.get(i)));
        }
        
        ArrayList<Move> copyMoves = currentState.getActionManager().getAllPossibleMoves();
        ArrayList<Move> possibleMoves = new ArrayList<Move>();
        for(int i=0; i<copyMoves.size(); i++){
            possibleMoves.add(new Move(copyMoves.get(i)));
        }
        
        reduceMoves(possibleMoves, possiblePuts);
//        reduceMoves(currentState, possibleMoves, possiblePuts);
        
        ArrayList<Location> winList;
        
        if(currentState.getTurn() == Consts.RED){ // maximum move
            bestAction = new Action(Integer.MIN_VALUE, depth); // null null
            for(int i=0; i<possiblePuts.size(); i++){
                Put currentPut = possiblePuts.get(i);
                currentState.doPut(currentPut);
                
                winList = currentState.checkWin(currentPut.getLocation());
                if(winList != null){
                    char winnerTurn = currentState.getTools()[winList.get(0).getY()][winList.get(0).getX()];
                    int currentScore = (winnerTurn == Consts.RED) ? State.WIN_SCORE : State.LOSE_SCORE;
                    if(depth == 0) System.out.println("#("+(int)(((i*possibleMoves.size()*1.0)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) put: "+currentPut+" || score: "+currentScore);
                    
                    currentState.undoPut(currentPut);
                    if(currentScore == State.WIN_SCORE){ // that's what red is looking for
                        if(depth == 0) System.out.println("absolute win");
                        return new Action(currentPut, new Move(-1, Consts.NO_DIRECTION), State.WIN_SCORE, depth);
                    }
                    
                    continue;
                }
                
                for(int j=0; j<possibleMoves.size(); j++){
                    Move currentMove = possibleMoves.get(j);
                    currentState.doAction(currentMove);
                    
                    winList = currentState.checkWin(new Location(0,0), new Location(5,5));
                    if(winList != null){
                        char winnerTurn = currentState.getTools()[winList.get(0).getY()][winList.get(0).getX()];
                        int currentScore = (winnerTurn == Consts.RED) ? State.WIN_SCORE : State.LOSE_SCORE;
                        
                        if(depth == 0)
                            System.out.println("#("+(int)(((i*possibleMoves.size()*1.0+j)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) move: "+currentMove+" || put: "+currentPut+" || score: "+currentScore);
                        
                        currentState.undoAction(currentMove);
                        if(currentScore == State.WIN_SCORE){
                            if(depth == 0) System.out.println("absolute win");
                            currentState.undoPut(currentPut);
                            return new Action(currentPut, currentMove, State.WIN_SCORE, depth);
                        }

                        continue;
                    }
                    
//                    System.out.println("(depth: "+depth+") tried action >> move: "+currentMove+" || put: "+currentPut);
                    currentState.swapTurn();
                    Action currentAction = getMinimaxAction(currentState, alpha, beta, depth+1, maxDepth);
//                    bestAction = Math.max(currentAction, bestAction);
                    if(currentAction.getScore() > bestAction.getScore()){
                        bestAction = currentAction;
                        if(depth == 0){
                            bestAction.setPut(currentPut);
                            bestAction.setMove(currentMove);
                        }
                    }else if(currentAction.getScore() == bestAction.getScore()){
                        if(currentAction.getDepth() < bestAction.getDepth()){
                            bestAction = currentAction;
                            if(depth == 0){
                                bestAction.setPut(currentPut);
                                bestAction.setMove(currentMove);
                            }
                        }
                    }
                    if(currentAction.getScore() == State.WIN_SCORE && currentAction.getDepth() < maxDepth)
                        maxDepth = currentAction.getDepth();
                    
//                    alpha = Math.max(currentAction.getScore(), alpha);
                    if(currentAction.getScore() > alpha)
                        alpha = currentAction.getScore();
                    
                    if(depth == 0)
                        System.out.println("#("+(int)(((i*possibleMoves.size()*1.0+j)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) move: "+currentMove+" || put: "+currentPut+" || score: "+currentAction.getScore());
                    
                    currentState.swapTurn();
                    currentState.undoAction(currentMove);
                    if(alpha >= beta){
                        currentState.undoPut(currentPut);
                        return bestAction;
                    }
                }
                
                currentState.undoPut(currentPut);
            }
        }else{ // if it's blue turn - minimum move
            bestAction = new Action(Integer.MAX_VALUE, depth);// null null
            for(int i=0; i<possiblePuts.size(); i++){
                Put currentPut = possiblePuts.get(i);
                currentState.doPut(currentPut);
                
                winList = currentState.checkWin(currentPut.getLocation());
                if(winList != null){
                    char winnerTurn = currentState.getTools()[winList.get(0).getY()][winList.get(0).getX()];
                    int currentScore = (winnerTurn == Consts.RED) ? State.WIN_SCORE : State.LOSE_SCORE;
                    
                    if(depth == 0)
                        System.out.println("#("+(int)(((i*possibleMoves.size()*1.0)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) put: "+currentPut+" || score: "+currentScore);
                    
                    currentState.undoPut(currentPut);
                    if(currentScore == State.LOSE_SCORE){
                        if(depth == 0) System.out.println("absolute lose");
                        return new Action(currentPut, new Move(-1, Consts.NO_DIRECTION), State.LOSE_SCORE, depth);
                    }

                    continue;
                }
                
                for(int j=0; j<possibleMoves.size(); j++){
                    Move currentMove = possibleMoves.get(j);
                    currentState.doAction(currentMove);
                    
                    winList = currentState.checkWin(new Location(0,0), new Location(5,5));
                    if(winList != null){
                        char winnerTurn = currentState.getTools()[winList.get(0).getY()][winList.get(0).getX()];
                        int currentScore = (winnerTurn == Consts.RED) ? State.WIN_SCORE : State.LOSE_SCORE;
                        if(depth == 0)
                            System.out.println("#("+(int)(((i*possibleMoves.size()*1.0+j)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) move: "+currentMove+" || put: "+currentPut+" || score: "+currentScore);
                        
                        currentState.undoAction(currentMove);
                        if(currentScore == State.LOSE_SCORE){
                            currentState.undoPut(currentPut);
                            if(depth == 0) System.out.println("absolute lose");
                            return new Action(currentPut, currentMove, State.LOSE_SCORE, depth);
                        }
                        
                        continue;
                    }
                    
//                    System.out.println("(depth: "+depth+") tried action >> move: "+currentMove+" || put: "+currentPut);
                    currentState.swapTurn();
                    Action currentAction = getMinimaxAction(currentState, alpha, beta, depth+1, maxDepth);
                    if(currentAction.getScore() < bestAction.getScore()){
                        bestAction = currentAction;
                        if(depth == 0){
                            bestAction.setPut(currentPut);
                            bestAction.setMove(currentMove);
                        }
                    }else if(currentAction.getScore() == bestAction.getScore()){
                        if(currentAction.getDepth() < bestAction.getDepth()){
                            bestAction = currentAction;
                            if(depth == 0){
                                bestAction.setPut(currentPut);
                                bestAction.setMove(currentMove);
                            }
                        }
                    }
                    
                    if(currentAction.getScore() == State.LOSE_SCORE && currentAction.getDepth() < maxDepth)
                        maxDepth = currentAction.getDepth();
                    
//                    beta = Math.min(currentAction.getScore(), beta);
                    if(currentAction.getScore() < beta)
                        beta = currentAction.getScore();
                    
                    if(depth == 0)
                        System.out.println("#("+(int)(((i*possibleMoves.size()*1.0+j)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) move: "+currentMove+" || put: "+currentPut+" || score: "+currentAction.getScore());
                    
                    currentState.swapTurn();
                    currentState.undoAction(currentMove);
                    if(beta <= alpha){
                        currentState.undoPut(currentPut);
                        return bestAction;
                    }
                }
                
                currentState.undoPut(currentPut);
            }
        }
        
        return bestAction;
    }
    
    /**
     * Searches for the best move that the player can do.
     * The serach is done by Minimax algorithm.
     * This function is with limitation for time as long as the user wants so the search won't take too long.
     * @param currentState - current state of the game
     * @param alpha - lower limit for score.
     * @param beta - upper limit for score.
     * @param depth - current depth for Minimax algorithm.
     * @param maxDepth - maximum depth for Minimax algorithm.
     * @param seconds - time limit for Minimax algorithm.
     * @return the best action the player can do
     */
    public Action getMinimaxAction(State currentState, int alpha, int beta, int depth, int maxDepth, long seconds){ // גרסה לא מוכנה שאמורה להחזיר פעולה במקום ציון
        Action bestAction = null; // אתחול ראשוני
        
        // time:
        long totalTime = seconds;
        long t1, t2;
        int size;
        
        if(depth >= maxDepth || totalTime == 0){
            int score = currentState.eval(Consts.RED);
//            int score = 50;
            return new Action(score, depth);
        }
        
        ArrayList<Put> copyPuts = currentState.getActionManager().getAllPossiblePuts();
        ArrayList<Put> possiblePuts = new ArrayList<Put>();
        for(int i=0; i<copyPuts.size(); i++){
            possiblePuts.add(new Put(copyPuts.get(i)));
        }
        
        ArrayList<Move> copyMoves = currentState.getActionManager().getAllPossibleMoves();
        ArrayList<Move> possibleMoves = new ArrayList<Move>();
        for(int i=0; i<copyMoves.size(); i++){
            possibleMoves.add(new Move(copyMoves.get(i)));
        }
        
        reduceMoves(possibleMoves, possiblePuts);
//        reduceMoves(currentState, possibleMoves, possiblePuts);
        size = possiblePuts.size() * possibleMoves.size();
        System.out.println("average: "+totalTime/size);
        
        ArrayList<Location> winList;
        
        if(currentState.getTurn() == Consts.RED){ // maximum move
            bestAction = new Action(Integer.MIN_VALUE, depth); // null null
            for(int i=0; i<possiblePuts.size(); i++){
                Put currentPut = possiblePuts.get(i);
                currentState.doPut(currentPut);
                
                winList = currentState.checkWin(currentPut.getLocation());
                if(winList != null){
                    char winnerTurn = currentState.getTools()[winList.get(0).getY()][winList.get(0).getX()];
                    int currentScore = (winnerTurn == Consts.RED) ? State.WIN_SCORE : State.LOSE_SCORE;
                    if(depth == 0)
                        System.out.println("#("+(int)(((i*possibleMoves.size()*1.0)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) put: "+currentPut+" || score: "+currentScore);
                    
                    currentState.undoPut(currentPut);
                    if(currentScore == State.WIN_SCORE){ // that's what red is looking for
                        if(depth == 0)
                            System.out.println("absolute win");
                        return new Action(currentPut, new Move(-1, Consts.NO_DIRECTION), State.WIN_SCORE, depth);
                    }
                    
                    continue;
                }
                
                for(int j=0; j<possibleMoves.size(); j++, size--){
                    t1 = System.currentTimeMillis();
                    
                    Move currentMove = possibleMoves.get(j);
                    currentState.doAction(currentMove);
                    
                    winList = currentState.checkWin(new Location(0,0), new Location(5,5));
                    if(winList != null){
                        char winnerTurn = currentState.getTools()[winList.get(0).getY()][winList.get(0).getX()];
                        int currentScore = (winnerTurn == Consts.RED) ? State.WIN_SCORE : State.LOSE_SCORE;
                        
                        if(depth == 0)
                            System.out.println("#("+(int)(((i*possibleMoves.size()*1.0+j)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) move: "+currentMove+" || put: "+currentPut+" || score: "+currentScore);
                        
                        currentState.undoAction(currentMove);
                        if(currentScore == State.WIN_SCORE){
                            if(depth == 0)
                                System.out.println("absolute win");
                            currentState.undoPut(currentPut);
                            return new Action(currentPut, currentMove, State.WIN_SCORE, depth);
                        }

                        continue;
                    }
                    
//                    System.out.println("(depth: "+depth+") tried action >> move: "+currentMove+" || put: "+currentPut);
                    currentState.swapTurn();
                    Action currentAction = getMinimaxAction(currentState, alpha, beta, depth+1, maxDepth, totalTime/size);
//                    bestAction = Math.max(currentAction, bestAction);
                    if(currentAction.getScore() > bestAction.getScore()){
                        bestAction = currentAction;
                        if(depth == 0){
                            bestAction.setPut(currentPut);
                            bestAction.setMove(currentMove);
                        }
                    }else if(currentAction.getScore() == bestAction.getScore()){
                        if(currentAction.getDepth() < bestAction.getDepth()){
                            bestAction = currentAction;
                            if(depth == 0){
                                bestAction.setPut(currentPut);
                                bestAction.setMove(currentMove);
                            }
                        }
                    }
                    
                    if(currentAction.getScore() == State.WIN_SCORE && currentAction.getDepth() < maxDepth)
                        maxDepth = currentAction.getDepth();
                    
//                    alpha = Math.max(currentAction, alpha);
                    if(currentAction.getScore() > alpha)
                        alpha = currentAction.getScore();
                    
                    if(depth == 0)
                        System.out.println("#("+(int)(((i*possibleMoves.size()*1.0+j)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) move: "+currentMove+" || put: "+currentPut+" || score: "+currentAction.getScore());
                    
                    currentState.swapTurn();
                    currentState.undoAction(currentMove);
                    if(alpha >= beta){
                        currentState.undoPut(currentPut);
                        return bestAction;
                    }
                    
                    t2 = System.currentTimeMillis();
                    long average = totalTime / size;
                    if(t1-t2 > average)
                        maxDepth--;
                    else if(t1-t2 < average)
                        maxDepth++;
                    totalTime -= t1-t2;
                }
                
                currentState.undoPut(currentPut);
            }
        }else{ // if it's blue turn - minimum move
            bestAction = new Action(Integer.MAX_VALUE, depth);// null null
            for(int i=0; i<possiblePuts.size(); i++){
                Put currentPut = possiblePuts.get(i);
                currentState.doPut(currentPut);
                
                winList = currentState.checkWin(currentPut.getLocation());
                if(winList != null){
                    char winnerTurn = currentState.getTools()[winList.get(0).getY()][winList.get(0).getX()];
                    int currentScore = (winnerTurn == Consts.RED) ? State.WIN_SCORE : State.LOSE_SCORE;
                    
                    if(depth == 0)
                        System.out.println("#("+(int)(((i*possibleMoves.size()*1.0)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) put: "+currentPut+" || score: "+currentScore);
                    
                    currentState.undoPut(currentPut);
                    if(currentScore == State.LOSE_SCORE){
                        if(depth == 0)
                            System.out.println("absolute lose");
                        return new Action(currentPut, new Move(-1, Consts.NO_DIRECTION), State.LOSE_SCORE, depth);
                    }

                    continue;
                }
                
                for(int j=0; j<possibleMoves.size(); j++, size--){
                    t1 = System.currentTimeMillis();
                    
                    Move currentMove = possibleMoves.get(j);
                    currentState.doAction(currentMove);
                    
                    winList = currentState.checkWin(new Location(0,0), new Location(5,5));
                    if(winList != null){
                        char winnerTurn = currentState.getTools()[winList.get(0).getY()][winList.get(0).getX()];
                        int currentScore = (winnerTurn == Consts.RED) ? State.WIN_SCORE : State.LOSE_SCORE;
                        
                        if(depth == 0)
                            System.out.println("#("+(int)(((i*possibleMoves.size()*1.0+j)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) move: "+currentMove+" || put: "+currentPut+" || score: "+currentScore);
                        
                        currentState.undoAction(currentMove);
                        if(currentScore == State.LOSE_SCORE){
                            currentState.undoPut(currentPut);
                            if(depth == 0)
                                System.out.println("absolute lose");
                            return new Action(currentPut, currentMove, State.LOSE_SCORE, depth);
                        }
                        
                        continue;
                    }
                    
//                    System.out.println("(depth: "+depth+") tried action >> move: "+currentMove+" || put: "+currentPut);
                    currentState.swapTurn();
                    Action currentAction = getMinimaxAction(currentState, alpha, beta, depth+1, maxDepth, totalTime/size);
                    if(currentAction.getScore() < bestAction.getScore()){
                        bestAction = currentAction;
                        if(depth == 0){
                            bestAction.setPut(currentPut);
                            bestAction.setMove(currentMove);
                        }
                    }else if(currentAction.getScore() == bestAction.getScore()){
                        if(currentAction.getDepth() < bestAction.getDepth()){
                            bestAction = currentAction;
                            if(depth == 0){
                                bestAction.setPut(currentPut);
                                bestAction.setMove(currentMove);
                            }
                        }
                    }
                    
                    if(currentAction.getScore() == State.LOSE_SCORE && currentAction.getDepth() < maxDepth)
                        maxDepth = currentAction.getDepth();
                    
//                    beta = Math.min(currentAction, beta);
                    if(currentAction.getScore() < beta)
                        beta = currentAction.getScore();
                    
                    if(depth == 0)
                        System.out.println("#("+(int)(((i*possibleMoves.size()*1.0+j)/(possiblePuts.size()*possibleMoves.size()))*100)+"%) move: "+currentMove+" || put: "+currentPut+" || score: "+currentAction.getScore());
                    
                    currentState.swapTurn();
                    currentState.undoAction(currentMove);
                    if(beta <= alpha){
                        currentState.undoPut(currentPut);
                        return bestAction;
                    }
                    
                    t2 = System.currentTimeMillis();
                    long average = totalTime / size;
                    if(t1-t2 > average)
                        maxDepth--;
                    else if(t1-t2 < average)
                        maxDepth++;
                    totalTime -= t1-t2;
                }
                
                currentState.undoPut(currentPut);
            }
        }
        
        return bestAction;
    }
}