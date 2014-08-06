package endee.fried.treasure;

/**
 * Created by natasha on 2014-08-05.
 */
public class MapLocation {

    private int center_x;
    private int center_y;
    private int radius;
    private boolean isActive;

    public MapLocation(int center_x, int center_y, int radius) {
        this.center_x = center_x;
        this.center_y = center_y;
        this.radius = radius;
        this.isActive = false;
    }

    /**
     * returns true if the Point at x,y is within bounds of the location.
     * @param x
     * @param y
     */
    public boolean isInBounds(float x, float y) {
        return Math.pow((x - center_x), 2) + Math.pow((y - center_y), 2) < Math.pow(radius,2);
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public int getX() {
        return this.center_x;
    }

    public int getY() {
        return this.center_y;
    }

    public int getRadius() {
        return this.radius;
    }


}
