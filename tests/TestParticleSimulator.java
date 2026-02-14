import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import java.util.Map;
import org.junit.Test;
import java.util.*;

public class TestParticleSimulator {

    @Test
    public void testConstructor_initializesEmptyGrid_usingIndices() {
        ParticleSimulator simulator = new ParticleSimulator(150, 150);

        // 1. Verify the outer array length (Width)
        assertThat(simulator.particles).hasLength(simulator.width);

        // 2. Iterate using Integer Indices
        for (int x = 0; x < simulator.width; x++) {

            // Verify the inner array length (Height) for this column
            assertThat(simulator.particles[x]).hasLength(simulator.height);

            for (int y = 0; y < simulator.height; y++) {
                Particle particle = simulator.particles[x][y];

                // Verify the particle is not null
                assertThat(particle).isNotNull();

                // Verify the particle is initialized to EMPTY
                assertWithMessage("Particle at x=%s, y=%s should be EMPTY", x, y)
                        .that(particle.flavor)
                        .isEqualTo(ParticleFlavor.EMPTY);
            }
        }
    }

    @Test
    public void testValidIndex(){

        int width = 10;
        int height = 20;
        ParticleSimulator sim = new ParticleSimulator(width,height);

        assertThat(sim.validIndex(0,0)).isTrue();
        assertThat(sim.validIndex(0,12)).isTrue();
        assertThat(sim.validIndex(8,0)).isTrue();

        assertThat(sim.validIndex(-1, 0)).isFalse();
        assertThat(sim.validIndex(0, -1)).isFalse();
        assertThat(sim.validIndex(10, 0)).isFalse();
        assertThat(sim.validIndex(0, 20)).isFalse();
        assertThat(sim.validIndex(100, 100)).isFalse();

    }

    @Test
    public void testGetNeighbors() {
        // Arrange: Create a small 3x3 grid
        // (0,2) (1,2) (2,2)
        // (0,1) (1,1) (2,1)
        // (0,0) (1,0) (2,0)
        ParticleSimulator sim = new ParticleSimulator(3, 3);

        // Setup specific particles around the center (1,1) to verify correct mapping
        sim.particles[1][2] = new Particle(ParticleFlavor.WATER); // UP of center (x=1, y=2)
        sim.particles[1][0] = new Particle(ParticleFlavor.SAND);  // DOWN of center (x=1, y=0)
        sim.particles[0][1] = new Particle(ParticleFlavor.FIRE);  // LEFT of center (x=0, y=1)
        sim.particles[2][1] = new Particle(ParticleFlavor.PLANT); // RIGHT of center (x=2, y=1)

        // --- Case 1: Center Particle (All neighbors are within bounds) ---
        Map<Direction, Particle> centerNeighbors = sim.getNeighbors(1, 1);

        assertThat(centerNeighbors.get(Direction.UP).flavor).isEqualTo(ParticleFlavor.WATER);
        assertThat(centerNeighbors.get(Direction.DOWN).flavor).isEqualTo(ParticleFlavor.SAND);
        assertThat(centerNeighbors.get(Direction.LEFT).flavor).isEqualTo(ParticleFlavor.FIRE);
        assertThat(centerNeighbors.get(Direction.RIGHT).flavor).isEqualTo(ParticleFlavor.PLANT);

        // --- Case 2: Bottom-Left Corner (0,0) (Verify Off-Screen is Barrier) ---
        // Neighbors for (0,0):
        // UP: (0,1) -> Fire (from setup above)
        // RIGHT: (1,0) -> Sand (from setup above)
        // DOWN: (0,-1) -> Off screen -> Should be BARRIER
        // LEFT: (-1,0) -> Off screen -> Should be BARRIER

        Map<Direction, Particle> cornerNeighbors = sim.getNeighbors(0, 0);

        // Verify valid neighbors
        assertThat(cornerNeighbors.get(Direction.UP).flavor).isEqualTo(ParticleFlavor.FIRE);
        assertThat(cornerNeighbors.get(Direction.RIGHT).flavor).isEqualTo(ParticleFlavor.SAND);

        // Verify invalid/off-screen neighbors are treated as BARRIER
        assertWithMessage("Off-screen neighbor (Down) should be treated as BARRIER")
                .that(cornerNeighbors.get(Direction.DOWN).flavor).isEqualTo(ParticleFlavor.BARRIER);
        assertWithMessage("Off-screen neighbor (Left) should be treated as BARRIER")
                .that(cornerNeighbors.get(Direction.LEFT).flavor).isEqualTo(ParticleFlavor.BARRIER);
    }

    @Test
    public void testFall() {
        // Arrange: Initialize a small 2x2 simulator
        ParticleSimulator sim = new ParticleSimulator(2, 2);

        // --- Scenario 1: Fall into Empty Space ---
        // Setup: Place SAND at (0, 1) and ensure (0, 0) is EMPTY
        // Note that 0, 0 is the bottom left, and 0, 1 is the top left.
        sim.particles[0][1] = new Particle(ParticleFlavor.SAND);
        sim.particles[0][0] = new Particle(ParticleFlavor.EMPTY);

        // Get real neighbors for the particle at (0, 1)
        Map<Direction, Particle> neighbors1 = sim.getNeighbors(0, 1);

        // Act: Tell the particle at (0, 1) to fall
        sim.particles[0][1].fall(neighbors1);

        // Assert:
        // 1. Old position (0, 1) should now be EMPTY
        assertThat(sim.particles[0][1].flavor).isEqualTo(ParticleFlavor.EMPTY);
        // 2. New position (0, 0) should now be SAND
        assertThat(sim.particles[0][0].flavor).isEqualTo(ParticleFlavor.SAND);


        // --- Scenario 2: Blocked by Barrier ---
        // Setup: Place SAND at (1, 1) and BARRIER at (1, 0)
        sim.particles[1][1] = new Particle(ParticleFlavor.SAND);
        sim.particles[1][0] = new Particle(ParticleFlavor.BARRIER);

        // Get real neighbors for the particle at (1, 1)
        Map<Direction, Particle> neighbors2 = sim.getNeighbors(1, 1);

        // Act: Tell the particle at (1, 1) to fall
        sim.particles[1][1].fall(neighbors2);

        // Assert:
        // 1. Position (1, 1) stays SAND (blocked)
        assertThat(sim.particles[1][1].flavor).isEqualTo(ParticleFlavor.SAND);
        // 2. Position (1, 0) stays BARRIER
        assertThat(sim.particles[1][0].flavor).isEqualTo(ParticleFlavor.BARRIER);
    }

    @Test
    public void testTick_updatesParticlesBottomUp() {
        // Arrange: Create a tall, narrow grid (1 wide, 3 high)
        // Coordinates: (0,0) is bottom, (0,2) is top
        ParticleSimulator sim = new ParticleSimulator(1, 3);

        // Setup a stack of sand with a gap at the bottom
        sim.particles[0][0] = new Particle(ParticleFlavor.EMPTY); // Bottom
        sim.particles[0][1] = new Particle(ParticleFlavor.SAND);  // Middle
        sim.particles[0][2] = new Particle(ParticleFlavor.SAND);  // Top

        // Act: Run one simulation step
        sim.tick();

        // Assert: Both particles should have moved down one step

        // 1. The bottom spot (0,0) catches the first falling sand
        assertThat(sim.particles[0][0].flavor).isEqualTo(ParticleFlavor.SAND);

        // 2. The middle spot (0,1) catches the second falling sand
        // (If the loop ran top-down, this would be EMPTY because the top sand would have been blocked)
        assertThat(sim.particles[0][1].flavor).isEqualTo(ParticleFlavor.SAND);

        // 3. The top spot (0,2) should now be empty
        assertThat(sim.particles[0][2].flavor).isEqualTo(ParticleFlavor.EMPTY);
    }

    private ParticleSimulator fromBoardString(String board) {
        String[] lines = board.trim().split("\\n");
        int height = lines.length;
        int width = lines[0].trim().length();

        ParticleSimulator sim = new ParticleSimulator(width, height);

        for (int i = 0; i < height; i++) {
            String line = lines[i].trim();
            for (int x = 0; x < width; x++) {
                char c = line.charAt(x);
                int y = height - 1 - i;
                ParticleFlavor flavor = ParticleSimulator.LETTER_TO_PARTICLE.get(c);
                sim.particles[x][y] = new Particle(flavor);
            }
        }
        return sim;
    }

    @Test
    public void testTickVisual() {
        // Arrange: A 3x5 grid with sand (s) suspended over empty space (d)
        // and a barrier (b) at the bottom.
        String initialBoard = """
            s.s
            s.s
            ...
            ...
            bbb
            """;

        ParticleSimulator sim = fromBoardString(initialBoard);

        // Act: Run 1 tick
        sim.tick();

        String expectedAfter1Tick = """
            ...
            s.s
            s.s
            ...
            bbb
            """;

        // Assert: Verify state after 1 tick
        assertThat(sim.toString().trim()).isEqualTo(expectedAfter1Tick.trim());

        // Act: Run 2nd tick
        sim.tick();

        String expectedAfter2Ticks = """
            ...
            ...
            s.s
            s.s
            bbb
            """;

        // Assert: Verify state after 2 ticks
        assertThat(sim.toString().trim()).isEqualTo(expectedAfter2Ticks.trim());
    }

    @Test
    public void testAction(){
        ParticleSimulator sim = new ParticleSimulator(2, 2);

        // Scenario 1: EMPTY does nothing
        sim.particles[0][0] = new Particle(ParticleFlavor.EMPTY);
        Map<Direction, Particle> n1 = sim.getNeighbors(0, 0);
        sim.particles[0][0].action(n1);
        assertThat(sim.particles[0][0].flavor).isEqualTo(ParticleFlavor.EMPTY);

        // Scenario 2: BARRIER does nothing
        sim.particles[1][0] = new Particle(ParticleFlavor.BARRIER);
        Map<Direction, Particle> n2 = sim.getNeighbors(1, 0);
        sim.particles[1][0].action(n2);
        assertThat(sim.particles[1][0].flavor).isEqualTo(ParticleFlavor.BARRIER);

        // Scenario 3: SAND falls when DOWN is EMPTY (SAND at (0,1) -> (0,0))
        sim.particles[0][1] = new Particle(ParticleFlavor.SAND);
        sim.particles[0][0] = new Particle(ParticleFlavor.EMPTY);
        Map<Direction, Particle> n3 = sim.getNeighbors(0, 1);
        sim.particles[0][1].action(n3);
        assertThat(sim.particles[0][1].flavor).isEqualTo(ParticleFlavor.EMPTY);
        assertThat(sim.particles[0][0].flavor).isEqualTo(ParticleFlavor.SAND);

        // Scenario 4: SAND does not fall when DOWN is BARRIER
        sim.particles[0][1] = new Particle(ParticleFlavor.SAND);
        sim.particles[0][0] = new Particle(ParticleFlavor.BARRIER);
        Map<Direction, Particle> n4 = sim.getNeighbors(0, 1);
        sim.particles[0][1].action(n4);
        assertThat(sim.particles[0][1].flavor).isEqualTo(ParticleFlavor.SAND);
        assertThat(sim.particles[0][0].flavor).isEqualTo(ParticleFlavor.BARRIER);

    }

    @Test
    public void testTickWithFlow() {
        // Arrange:
        // Col 0: Stacked Sand (s) on Barrier -> Should be Stable
        // Col 2: Water (w) on Barrier -> Should Flow
        // Col 4: Sand (s) in Air -> Should Fall
        String startState = """
            s...s
            s.w..
            bbbbb
            """;

        // Possibility 1: Water stays put (or moves Right then Left)
        // Sand falls.
        String expectStay = """
            s....
            s.w.s
            bbbbb
            """;

        // Possibility 2: Water flows Left.
        // Sand falls.
        String expectLeft = """
            s....
            sw..s
            bbbbb
            """;

        // Possibility 3: Water flows Right ONCE (Right then Stay).
        // Sand falls.
        String expectRightSingle = """
            s....
            s..ws
            bbbbb
            """;

        // Possibility 4: Water flows Right TWICE (Right then Right).
        // Water ends up under the Sand (at 4,1), blocking the Sand at (4,2).
        String expectRightDouble = """
            s...s
            s...w
            bbbbb
            """;

        int countStay = 0;
        int countLeft = 0;
        int countRightSingle = 0;
        int countRightDouble = 0;

        // Act: Run 1000 simulations
        for (int i = 0; i < 1000; i++) {
            ParticleSimulator sim = fromBoardString(startState);
            sim.tick();
            String result = sim.toString().trim();

            if (result.equals(expectStay.trim())) {
                countStay += 1;
            } else if (result.equals(expectLeft.trim())) {
                countLeft += 1;
            } else if (result.equals(expectRightSingle.trim())) {
                countRightSingle += 1;
            } else if (result.equals(expectRightDouble.trim())) {
                countRightDouble += 1;
            } else {
                throw new AssertionError("Unexpected board state:\n" + result);
            }
        }

        // Assert:
        // 1. Left (~33%): > 240 is safe.
        assertThat(countLeft).isGreaterThan(240);

        // 2. Stay (~44%): 1/3 (Stay) + 1/9 (Right-then-Left) = 4/9. > 240 is safe.
        assertThat(countStay).isGreaterThan(240);

        // 3. Right Single (~11%): 1/3 (Right) * 1/3 (Stay) = 1/9.
        // Expected ~111. Threshold 50 is safe.
        assertThat(countRightSingle).isGreaterThan(50);

        // 4. Right Double (~11%): 1/3 (Right) * 1/3 (Right) = 1/9.
        // Expected ~111. Threshold 50 is safe.
        assertThat(countRightDouble).isGreaterThan(50);
    }

    @Test
    public void testFallingWaterDoesNotFlow() {
        // Arrange:
        // Water (w) suspended in the center.
        // It has empty space below it (so it MUST fall).
        // It has empty space to the sides (so it COULD flow, if logic was wrong).
        String startState = """
            ...
            .w.
            ...
            bbb
            """;

        // Expected Behavior:
        // The water drops exactly one spot (to the center bottom).
        // It should NOT move Left or Right after falling.
        String expectedState = """
            ...
            ...
            .w.
            bbb
            """;

        for (int i = 0; i < 100; i++) {
            ParticleSimulator sim = fromBoardString(startState);
            sim.tick();

            String result = sim.toString().trim();
            assertThat(result).isEqualTo(expectedState.trim());
        }
    }

    @Test
    public void testGrow() {
        String startState = """
        ...
        .p.
        bbb
        """.trim();


        // The list of REQUIRED growth outcomes
        List<String> expectedGrowthStates = new ArrayList<>();

        expectedGrowthStates.add("""
        ...
        .p.
        bbb
        """.trim()); // no growth

        expectedGrowthStates.add("""
        ...
        pp.
        bbb
        """.trim()); // Left

        expectedGrowthStates.add("""
        .p.
        .p.
        bbb
        """.trim()); // Up

        expectedGrowthStates.add("""
        pp.
        .p.
        bbb
        """.trim()); // Up + Left

        expectedGrowthStates.add("""
        ...
        .pp
        bbb
        """.trim()); // Right

        expectedGrowthStates.add("""
        ..p
        .pp
        bbb
        """.trim()); // Right + Up

        expectedGrowthStates.add("""
        .p.
        .pp
        bbb
        """.trim()); // Up, Right (fall)

        expectedGrowthStates.add("""
        .pp
        .pp
        bbb
        """.trim()); // Right, Up, Left



        // --- ACT ---
        Set<String> observedStates = new HashSet<>();

        for (int i = 0; i < 10000; i++) {
            ParticleSimulator sim = fromBoardString(startState);
            sim.tick();
            observedStates.add(sim.toString().trim());
        }

        // --- ASSERT 1: CHECK FOR MISSING STATES ---
        for (String expected : expectedGrowthStates) {
            assertWithMessage("""
        Test Failed: A required growth state was never observed.
        Missing State:
        %s
        """, expected)
                    .that(observedStates)
                    .contains(expected);
        }

        // --- ASSERT 2: CHECK FOR UNEXPECTED (INVALID) STATES ---

        // Create a "White List" of all valid outcomes (Growth + No Change)
        Set<String> validStates = new HashSet<>(expectedGrowthStates);

        for (String observed : observedStates) {
            assertWithMessage("""
        Test Failed: An invalid/impossible state was generated.
        Unexpected State:
        %s
        """, observed)
                    .that(validStates)
                    .contains(observed);
        }
    }







}
