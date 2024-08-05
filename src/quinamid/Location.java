package quinamid;


public class Location {
    
    private int x;
    private int y;
    
    public Location(){
        this.x = 0;
        this.y = 0;
    }
    
    public Location(int x, int y){
        this.x = x;
        this.y = y;
    }
    
    public Location(Location location){
        this.x = location.x;
        this.y = location.y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Location{" + x + "," + y + '}';
    }
}
