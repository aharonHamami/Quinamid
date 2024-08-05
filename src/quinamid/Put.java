package quinamid;

public class Put {
    private Location location; // location on the local board (6x6)

    public Put(Location location) {
        this.location = location;
    }
    
    public Put(Put put){
        if(put != null){
            this.location = new Location(put.location);
        }
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Put{" + "loc:" + location + '}';
    }
}