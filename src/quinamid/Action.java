package quinamid;


public class Action {
    Put put;
    Move move;
    
    // לפתח בהמשך
    int score;
    int depth;
    
    public Action(Put put, Move move){
        this.put = put;
        this.move = move;
        
        this.score = -1;
        this.depth = -1;
    }
    
    public Action(Put put, Move move, int score, int depth){
        this.put = put;
        this.move = move;
        
        this.score = score;
        this.depth = depth;
    }
    
    public Action(int score, int depth){
        this.put = null; // no put
        this.move = null; // no move
        
        this.score = score;
        this.depth = depth;
    }
    
    public Action(Action action){
        this.put = new Put(action.put);
        this.move = new Move(action.move);
        
        this.depth = action.depth;
        this.score = action.score;
    }

    public Put getPut() {
        return put;
    }

    public void setPut(Put put) {
        this.put = put;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
    @Override
    public String toString(){
        return "Action{"+put+", "+move+", score: "+score+", depth: "+depth+"}";
    }
}
