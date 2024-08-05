package quinamid;

public class Move {
    private int board; // index/hight of the board, -1 if there is no board
    private char direction; // Up/Down/Left/Right/rotate_right/rotate_left/no_move (U/D/L/R/>/</N)
    
    public Move(int board, char direction){
        this.board = board;
        this.direction = direction;
    }
    
    public Move(Move move){
        if(move != null){
            this.board = move.board;
            this.direction = move.direction;
        }
    }

    public int getBoard() {
        return board;
    }

    public char getDirection() {
        return direction;
    }
    
    public void setDirection(char direction){
        this.direction = direction;
    }
    
    void swapDirection(){
        switch(direction){
            case Consts.UP:
                direction = Consts.DOWN;
                break;
            case Consts.DOWN:
                direction = Consts.UP;
                break;
            case Consts.RIGHT:
                direction = Consts.LEFT;
                break;
            case Consts.LEFT:
                direction = Consts.RIGHT;
                break;
            default:
                System.out.println("Move: there is a problem in exchanging");
        }
    }

    @Override
    public String toString() {
        return "Move{" + "b:" + board + ", dir:" + direction + '}';
    }
}