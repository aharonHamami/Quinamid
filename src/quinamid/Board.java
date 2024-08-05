package quinamid;



public class Board 
{
    //enum Direction {LEFT, RIGHT, DOWN, UP};
    
    private char[][] content;
    private Location location;
    private int hight;
    
    public Board(int length){
        this.content = new char[length][length];
        this.location = new Location();
        this.hight = 6-length; // len6 -> high0 , len2 -> high4
    }
    
    public Board(int length, Location location){
        this.content = new char[length][length];
        this.location = location;
        this.hight = 6-length; // len6 -> high0 , len2 -> high4
    }
    
    public Board(Board board){
        this.content = new char[board.content.length][board.content[0].length];
        for(int i=0; i<this.content.length; i++){ // copy content
            for (int j=0; j<this.content[0].length; j++) {
                this.content[i][j] = board.content[i][j];
            }
        }
        this.location = new Location(board.location); // check
        this.hight = board.hight;
    }
    
    public void setUp(){
        for (int i = 0; i < content.length; i++) {
            for (int j = 0; j < content[0].length; j++) {
                content[i][j] = Consts.NO_COLOR;
            }
        }
    }

    public char[][] getContent() {
        return content;
    }
    
    public char getTool(Location chrLocation){ // gets location comare to this board
        return content[chrLocation.getY()][chrLocation.getX()];
    }
    
    public char getGenerallTool(Location chrLocation){ // gets location compare to the general board
        if(isInside(chrLocation))
            return content[chrLocation.getY()-this.location.getY()][chrLocation.getX()-this.location.getX()];
        return 'n'; // null
    }

    public Location getLocation() {
        return location;
    }
    
    public int getLocationX(){
        return location.getX();
    }
    
    public int getLocationY(){
        return location.getY();
    }

    public int getHight() {
        return hight;
    }
    
    public int getLength(){
        return content.length;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
    
    public void moveX(int d){
        location = new Location(getLocationX()+d, location.getY());
    }
    
    public void moveY(int d){
        location = new Location(location.getX(), getLocationY()+d);
    }
    
    public void rotateRight(){
        for(int i=0; i<=(content.length-1)/2; i++){
            for (int j = 0; j < content.length/2; j++) {
                char temp = content[i][j]; // שמירת רביע שני
                content[i][j] = content[content.length-1-j][i]; // רביע שני שומר רביע שלישי
                content[content.length-1-j][i] = content[content.length-1-i][content.length-1-j]; // רביע שלישי שומר רביע רביעי
                content[content.length-1-i][content.length-1-j] = content[j][content.length-1-i]; // רביע רביעי שומר רביע ראשון
                content[j][content.length-1-i] = temp; // רביע ראשון שומר את רביע שני
            }
        }
    }
    
    public void rotateLeft(){
        for(int i=0; i<=(content.length-1)/2; i++){
            for (int j = 0; j < content.length/2; j++) {
                char temp = content[i][j]; // שמירת רביע שני
                content[i][j] = content[j][content.length-1-i]; // רביע שני שומר רביע שלישי
                content[j][content.length-1-i] = content[content.length-1-i][content.length-1-j]; // רביע שלישי שומר רביע רביעי
                content[content.length-1-i][content.length-1-j] = content[content.length-1-j][i]; // רביע רביעי שומר רביע ראשון
                content[content.length-1-j][i] = temp; // רביע ראשון שומר את רביע שני
            }
        }
    }
    
    public void setGeneralTool(Location chrLocation, char chr){ // location compare to the bottom board
        if(isInside(chrLocation))
            content[chrLocation.getY()-this.location.getY()][chrLocation.getX()-this.location.getX()] = chr;
    }
    
    public void setTool(Location chrLocation, char chr){ // location compare to this board
        content[chrLocation.getY()][chrLocation.getX()] = chr;
    }

    boolean isInside(Location inLocation) {
        int xDifference = inLocation.getX() - this.location.getX();
        int yDifference = inLocation.getY() - this.location.getY();
        if(xDifference>=0 && xDifference<getLength())
            if(yDifference>=0 && yDifference<getLength())
                return true;
        return false;
    }
    
}
