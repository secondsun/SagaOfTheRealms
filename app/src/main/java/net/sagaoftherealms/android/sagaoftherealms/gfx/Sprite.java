package net.sagaoftherealms.android.sagaoftherealms.gfx;

/**
 * Created by summers on 11/19/15.
 */
public class Sprite {

    //position
    public int x,y,z;

    //facing normal
    public int normalX,normalY,normalZ;

    //velocity
    public int speedX,speedY,speedZ;

    //A sprite index
    public int spriteIndex;
    public int [] spriteArray;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sprite sprite = (Sprite) o;

        if (x != sprite.x) return false;
        if (y != sprite.y) return false;
        if (z != sprite.z) return false;
        if (normalX != sprite.normalX) return false;
        if (normalY != sprite.normalY) return false;
        if (normalZ != sprite.normalZ) return false;
        if (speedX != sprite.speedX) return false;
        if (speedY != sprite.speedY) return false;
        if (speedZ != sprite.speedZ) return false;
        return spriteIndex == sprite.spriteIndex;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = 31 * result + normalX;
        result = 31 * result + normalY;
        result = 31 * result + normalZ;
        result = 31 * result + speedX;
        result = 31 * result + speedY;
        result = 31 * result + speedZ;
        result = 31 * result + spriteIndex;
        return result;
    }
}
