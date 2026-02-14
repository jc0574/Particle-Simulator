import edu.princeton.cs.algs4.StdDraw;

import java.util.HashMap;
import java.util.Map;

public class ParticleSimulator {
    public Particle[][] particles;
    public int width = 150;
    public int height = 150;

    public static final Map<Character, ParticleFlavor> LETTER_TO_PARTICLE = Map.of(
            's', ParticleFlavor.SAND,
            'b', ParticleFlavor.BARRIER,
            'w', ParticleFlavor.WATER,
            'p', ParticleFlavor.PLANT,
            'f', ParticleFlavor.FIRE,
            '.', ParticleFlavor.EMPTY,
            'n', ParticleFlavor.FOUNTAIN,
            'r', ParticleFlavor.FLOWER
    );


    ParticleSimulator(int w, int h){
        width = w;
        height = h;
        particles = new Particle[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                particles[x][y] = new Particle(ParticleFlavor.EMPTY);
            }
        }
    }

    public void drawParticles(){
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                StdDraw.setPenColor(particles[x][y].color());
                StdDraw.filledSquare(x, y, 0.5);
            }
        }
    }

    public boolean validIndex(int x, int y){
        if(x >= 0 && x < width){
            return y >= 0 && y < height;
        }
        else{
            return false;
        }

    }


    public Map<Direction, Particle> getNeighbors(int x, int y){
        Map<Direction, Particle> result = new HashMap<>();

        if (y + 1 < height) {
            result.put(Direction.UP, particles[x][y + 1]);
        } else {
            result.put(Direction.UP, new Particle(ParticleFlavor.BARRIER));
        }

        if (y - 1 >= 0) {
            result.put(Direction.DOWN, particles[x][y - 1]);
        } else {
            result.put(Direction.DOWN, new Particle(ParticleFlavor.BARRIER));
        }

        if (x - 1 >= 0) {
            result.put(Direction.LEFT, particles[x - 1][y]);
        } else {
            result.put(Direction.LEFT, new Particle(ParticleFlavor.BARRIER));
        }

        if (x + 1 < width) {
            result.put(Direction.RIGHT, particles[x + 1][y]);
        } else {
            result.put(Direction.RIGHT, new Particle(ParticleFlavor.BARRIER));
        }

        return result;
    }

    public void tick(){
        for(int x = 0; x < width; x += 1 ){
            for(int y = 0; y < height; y += 1){
                particles[x][y].action(getNeighbors(x,y));
                particles[x][y].decrementLifespan();
            }
        }
    }


    public static void main(String[] args) {
        ParticleSimulator particleSimulator = new ParticleSimulator(150, 150);

        StdDraw.setXscale(0, particleSimulator.width);
        StdDraw.setYscale(0, particleSimulator.height);
        StdDraw.enableDoubleBuffering();
        StdDraw.clear(StdDraw.BLACK);
        ParticleFlavor nextParticleFlavor = ParticleFlavor.FOUNTAIN;

        while (true) {
            if(StdDraw.hasNextKeyTyped()){
                char l = StdDraw.nextKeyTyped();
                nextParticleFlavor = LETTER_TO_PARTICLE.getOrDefault(l, nextParticleFlavor);
            }
            if (StdDraw.isMousePressed()) {
                int x = (int) StdDraw.mouseX();
                int y = (int) StdDraw.mouseY();
                particleSimulator.particles[x][y] = new Particle(nextParticleFlavor);
            }
            particleSimulator.tick();
            particleSimulator.drawParticles();
            StdDraw.show();
            StdDraw.pause(5);
        }
    }

    @Override
    public String toString() {
        // 1. Build a reverse map to look up characters by Flavor
        Map<ParticleFlavor, Character> flavorToChar = new HashMap<>();
        for (Map.Entry<Character, ParticleFlavor> entry : LETTER_TO_PARTICLE.entrySet()) {
            flavorToChar.put(entry.getValue(), entry.getKey());
        }

        StringBuilder sb = new StringBuilder();

        // Have to iterate from the top so that
        // the top particles are shown first.
        for (int y = height - 1; y >= 0; y -= 1) {
            for (int x = 0; x < width; x += 1) {
                Particle p = particles[x][y];
                sb.append(flavorToChar.get(p.flavor));
            }
            sb.append("\n");
        }
        return sb.toString();
    }


}

