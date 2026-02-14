import org.reflections.vfs.Vfs;
import edu.princeton.cs.algs4.StdRandom;
import javax.servlet.http.Part;
import java.util.Map;
import java.awt.Color;
import java.util.regex.Pattern;


public class Particle {
    public static final int PLANT_LIFESPAN = 150;
    public static final int FLOWER_LIFESPAN = 75;
    public static final int FIRE_LIFESPAN = 10;
    public static final Map<ParticleFlavor, Integer> lifespans =
            Map.of(ParticleFlavor.FLOWER, FLOWER_LIFESPAN,
                    ParticleFlavor.PLANT, PLANT_LIFESPAN,
                    ParticleFlavor.FIRE, FIRE_LIFESPAN);
    ParticleFlavor flavor;
    int lifespan;

    Particle(ParticleFlavor flavor){
        this.flavor = flavor;
        this.lifespan = lifespans.getOrDefault(flavor,-1);
    }

    public Color color( ){
        if (this.flavor == ParticleFlavor.EMPTY) {
            return Color.BLACK;
        } else if (this.flavor == ParticleFlavor.SAND) {
            return Color.YELLOW;
        } else if (this.flavor == ParticleFlavor.BARRIER) {
            return Color.GRAY;
        } else if (this.flavor == ParticleFlavor.WATER) {
            return Color.BLUE;
        } else if (this.flavor == ParticleFlavor.FOUNTAIN) {
            return Color.CYAN;
        } else if (this.flavor == ParticleFlavor.PLANT) {
            double ratio = (double) Math.max(0, Math.min(lifespan, PLANT_LIFESPAN)) / PLANT_LIFESPAN;
            int g = 120 + (int) Math.round((255 - 120) * ratio);
            return new Color(0, g, 0);
        } else if (this.flavor == ParticleFlavor.FIRE) {
            double ratio = (double) Math.max(0, Math.min(lifespan, FIRE_LIFESPAN)) / FIRE_LIFESPAN;
            int r = (int) Math.round(255 * ratio);
            return new Color(r, 0, 0);
        } else if (this.flavor == ParticleFlavor.FLOWER) {
            double ratio = (double) Math.max(0, Math.min(lifespan, FLOWER_LIFESPAN)) / FLOWER_LIFESPAN;
            int r = 120 + (int) Math.round((255 - 120) * ratio);
            int g = 70 + (int) Math.round((141 - 70) * ratio);
            int b = 80 + (int) Math.round((161 - 80) * ratio);
            return new Color(r, g, b);
        } else {
            return Color.WHITE;
        }
    }

    public void moveInto(Particle other){
        other.flavor = this.flavor;
        other.lifespan = this.lifespan;

        this.flavor = ParticleFlavor.EMPTY;
        this.lifespan = -1;
    }

    public void fall(Map<Direction, Particle> neighbors){
        if(neighbors.get(Direction.DOWN).flavor.equals(ParticleFlavor.EMPTY)){
            moveInto(neighbors.get(Direction.DOWN));
        }
    }

    public void action(Map<Direction, Particle> neighbors){
        ParticleFlavor f = this.flavor;
        if(f.equals(ParticleFlavor.EMPTY) || f.equals(ParticleFlavor.BARRIER)){
            return ;
        }
        if (f.equals((ParticleFlavor.WATER))) {
            extinguishFire(neighbors);
            if(neighbors.get(Direction.DOWN).flavor.equals(ParticleFlavor.EMPTY)){
                fall(neighbors);
            }
            else{
                flow(neighbors);
            }
        } else if (f.equals(ParticleFlavor.FLOWER) || f.equals(ParticleFlavor.PLANT)) {
            grow(neighbors);
        } else if (f.equals(ParticleFlavor.FIRE)) {
            burn(neighbors);
        }

        fall(neighbors);
    }

    public void flow(Map<Direction, Particle> neighbors){
        int n = StdRandom.uniformInt(3);
        Particle left = neighbors.get(Direction.LEFT);
        Particle right = neighbors.get(Direction.RIGHT);
        switch (n){
            case 0:
                return ;
            case 1:
                if(left.flavor.equals(ParticleFlavor.EMPTY)){
                    moveInto(left);
                    break ;
                }
            case 2:
                if(right.flavor.equals(ParticleFlavor.EMPTY)){
                    moveInto(right);
                    break ;
                }
        }
    }

    public void grow(Map<Direction, Particle> neighbors){
        int n = StdRandom.uniformInt(10);
        Particle left = neighbors.get(Direction.LEFT);
        Particle right = neighbors.get(Direction.RIGHT);
        Particle up = neighbors.get(Direction.UP);
        Particle down = neighbors.get(Direction.DOWN);

        switch (n){
            case 1:
                if(up.flavor.equals(ParticleFlavor.EMPTY)){
                    up.flavor = this.flavor;
                    up.lifespan = lifespans.get(this.flavor);
                }
                break ;
            case 2:
                if(left.flavor.equals(ParticleFlavor.EMPTY)){
                    left.flavor = this.flavor;
                    left.lifespan = lifespans.get(this.flavor);
                }
                break ;
            case 3:
                if(right.flavor.equals(ParticleFlavor.EMPTY)){
                    right.flavor = this.flavor;
                    right.lifespan = lifespans.get(this.flavor);
                }
                break ;
            default:
                return ;
        }
    }

    public void burn(Map<Direction, Particle> neighbors){
        for(Direction d : new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT}){
            Particle p = neighbors.get(d);
            if(p.flavor.equals(ParticleFlavor.FLOWER) || p.flavor.equals(ParticleFlavor.PLANT)){
                int n = StdRandom.uniformInt(10);
                switch(n) {
                    case 0, 1, 2, 3:
                        p.flavor = ParticleFlavor.FIRE;
                        p.lifespan = lifespans.get(ParticleFlavor.FIRE);
                        break;
                    default:
                        break;
                }
            }


        }


    }

    public void extinguishFire(Map<Direction, Particle> neighbors){
        for(Direction d: new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT}){
            Particle p = neighbors.get(d);
            if(p.flavor.equals(ParticleFlavor.FIRE)){
                p.flavor = ParticleFlavor.EMPTY;
                p.lifespan = -1;

                this.flavor = ParticleFlavor.EMPTY;
                this.lifespan = -1;
                return ;
            }
        }

    }

    public void fountain(Map<Direction,Particle> neighbors){
        for(Direction d: new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT}) {
            Particle p = neighbors.get(d);
        }
            return;
    }

    public void decrementLifespan(){
        if (lifespan < 0){
            return ;
        }

        lifespan -= 1;

        if(lifespan == 0){
            flavor = ParticleFlavor.EMPTY;
            lifespan = -1;
        }


    }

}