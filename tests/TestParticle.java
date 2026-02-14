import org.junit.jupiter.api.Test;
import java.awt.*;

import static com.google.common.truth.Truth.assertThat;

public class TestParticle {
    @Test
    public void testConstructor(){
        Particle p = new Particle(ParticleFlavor.WATER);
        assertThat(p.flavor).isEqualTo(ParticleFlavor.WATER);
        assertThat(p.lifespan).isEqualTo(-1);

        Particle q = new Particle(ParticleFlavor.FLOWER);
        assertThat(q.flavor).isEqualTo(ParticleFlavor.FLOWER);
        assertThat(q.lifespan).isEqualTo(75);

        Particle r = new Particle(ParticleFlavor.FIRE);
        assertThat(r.flavor).isEqualTo(ParticleFlavor.FIRE);
        assertThat(r.lifespan).isEqualTo(10);

    }

    @Test
    public void testColor(){
        Particle emtyParticle = new Particle(ParticleFlavor.EMPTY);
        assertThat(emtyParticle.color()).isEqualTo(Color.BLACK);

        Particle flowerParticle = new Particle(ParticleFlavor.FLOWER);
        assertThat(flowerParticle.color()).isEqualTo(new Color(255, 141, 161));

        Particle sandParticle = new Particle(ParticleFlavor.SAND);
        assertThat(sandParticle.color()).isEqualTo(Color.YELLOW);

        Particle barrierParticle = new Particle(ParticleFlavor.BARRIER);
        assertThat(barrierParticle.color()).isEqualTo(Color.GRAY);

        Particle waterParticle = new Particle(ParticleFlavor.WATER);
        assertThat(waterParticle.color()).isEqualTo(Color.BLUE);

        Particle fountainParticle = new Particle(ParticleFlavor.FOUNTAIN);
        assertThat(fountainParticle.color()).isEqualTo(Color.CYAN);

        Particle plantParticle = new Particle(ParticleFlavor.PLANT);
        assertThat(plantParticle.color()).isEqualTo(new Color(0, 255, 0));

        Particle fireParticle = new Particle(ParticleFlavor.FIRE);
        assertThat(fireParticle.color()).isEqualTo(new Color(255, 0, 0));
    }


    @Test
    public void testMoveInto(){
        Particle p = new Particle(ParticleFlavor.FIRE);
        Particle q = new Particle(ParticleFlavor.FLOWER);
        p.moveInto(q);
        assertThat(q.flavor).isEqualTo(ParticleFlavor.FIRE);
        assertThat(q.lifespan).isEqualTo(10);
        assertThat(p.flavor).isEqualTo(ParticleFlavor.EMPTY);
        assertThat(p.lifespan).isEqualTo(-1);

        Particle r = new Particle(ParticleFlavor.SAND);
        Particle s = new Particle(ParticleFlavor.WATER);
        r.moveInto(s);
        assertThat(s.flavor).isEqualTo(ParticleFlavor.SAND);
        assertThat(s.lifespan).isEqualTo(-1);
        assertThat(r.flavor).isEqualTo(ParticleFlavor.EMPTY);
        assertThat(r.lifespan).isEqualTo(-1);
    }

    


}
