
import java.util.Random;
import java.util.Vector;

public class GameLogic {
    private long seed;

    private final Vector<Coordinate> bombs;
    private final Vector<Coordinate> revealed;
    private final Vector<Coordinate> fields;
    private final Vector<Coordinate> flags;

    private int minX, minY;
    private int maxX, maxY;

    private Random random;
    private boolean procedural;

    public GameLogic() {
        this.seed = 0;
        this.bombs = new Vector<>();
        this.revealed = new Vector<>();
        this.fields = new Vector<>();
        this.flags = new Vector<>();

        this.minX = 0;
        this.minY = 0;
        this.maxX = 0;
        this.maxY = 0;

        this.procedural = false;

        Random random = new Random(this.seed);
    }

    public void generateSquareGame(int width, int height, int bombs) {
        if (bombs >= width * height) {
            System.err.println("Error: bombs >= width * height");
            System.exit(1);
        }

        this.minX = 0;
        this.minY = 0;
        this.maxX = width - 1;
        this.maxY = height - 1;

        this.bombs.clear();
        this.revealed.clear();
        this.fields.clear();

        for (int row = 0; row < height; ++row) {
            for (int column = 0; column < width; ++column) {
                final Coordinate coordinate = new Coordinate(column, row);

                this.setFieldAt(coordinate);
            }
        }

        Random random = new Random(this.seed);

        for (int index = 0; index < bombs; ++index) {
            Coordinate coordinate = new Coordinate(random.nextInt(width), random.nextInt(height));

            while (this.bombs.contains(coordinate)) {
                coordinate = new Coordinate(random.nextInt(width), random.nextInt(height));
            }

            this.setBombAt(coordinate);
        }

        this.random = new Random(this.seed);
        this.procedural = false;
    }

    public void generateProceduralGame() {
        this.minX = 0;
        this.minY = 0;
        this.maxX = 0;
        this.maxY = 0;

        this.bombs.clear();
        this.revealed.clear();
        this.fields.clear();

        for (int row = this.minY; row <= this.maxY; ++row) {
            for (int column = this.minX; column <= this.maxX; ++column) {
                final Coordinate coordinate = new Coordinate(column, row);

                this.setFieldAt(coordinate);
            }
        }

        this.random = new Random(this.seed);
        this.procedural = true;
    }

    private void setBombAt(Coordinate coordinate) {
        if (this.fields.contains(coordinate)) {
            if (this.bombs.contains(coordinate)) {
                return;
            }

            this.bombs.add(coordinate);
        } else {
            this.setFieldAt(coordinate);
            this.setBombAt(coordinate);
        }
    }

    private void setFieldAt(Coordinate coordinate) {
        if (this.fields.contains(coordinate)) {
            return;
        }

        this.fields.add(coordinate);
    }

    public FieldState fieldInfoAt(Coordinate coordinate) {
        if (!this.fields.contains(coordinate)) {
            return FieldState.NONE;
        }

        boolean bomb = this.bombs.contains(coordinate);
        boolean revealed = this.revealed.contains(coordinate);
        boolean flagged = this.flags.contains(coordinate);

        if (!revealed) {
            if (flagged) {
                return FieldState.FLAG;
            }

            if (bomb) {
                return FieldState.UNREVEALED_BOMB;
            } else {
                return FieldState.BLANK;
            }
        } else {
            if (bomb) {
                return FieldState.REVEALED_BOMB;
            } else {
                int unsafeNeighbors = this.unsafeNeighborsAt(coordinate);

                return switch (unsafeNeighbors) {
                    case 0 -> FieldState.EMPTY;
                    case 1 -> FieldState.ONE;
                    case 2 -> FieldState.TWO;
                    case 3 -> FieldState.THREE;
                    case 4 -> FieldState.FOUR;
                    case 5 -> FieldState.FIVE;
                    case 6 -> FieldState.SIX;
                    case 7 -> FieldState.SEVEN;
                    case 8 -> FieldState.EIGHT;
                    default -> FieldState.ERROR_STATE;
                };
            }
        }
    }

    public Vector<Coordinate> interactAt(Coordinate coordinate) {
        if (!this.fields.contains(coordinate)) {
            return null;
        }

        if (this.revealed.contains(coordinate)) {
            return null;
        }

        this.revealed.add(coordinate);

        if (this.bombs.contains(coordinate)) {
            // TODO: IMPLEMENT GAME OVER
            return null;
        }

        if (this.procedural) {
            for (Coordinate neighbor : this.neighborCoordinatesAt(coordinate)) {
                if (!this.fields.contains(neighbor)) {
                    setFieldAt(neighbor);

                    if (this.random.nextInt(8) == 0) {
                        setBombAt(neighbor);
                    }
                }
            }
        }

        if (unsafeNeighborsAt(coordinate) == 0) {
            Vector<Coordinate> neighborCoordinates = this.neighborCoordinatesAt(coordinate);

            Vector<Coordinate> coordinatesToReveal = new Vector<>(9);

            for (Coordinate neighbor : neighborCoordinates) {
                if (this.revealed.contains(neighbor)) {
                    continue;
                }

                coordinatesToReveal.add(neighbor);
            }

            return coordinatesToReveal;
        }

        return null;
    }

    private int unsafeNeighborsAt(Coordinate coordinate) {
        final Vector<Coordinate> neighborCoordinates = this.neighborCoordinatesAt(coordinate);

        return filterCoordinatesWithBombs(neighborCoordinates.stream().filter(this.bombs::contains).count());
    }

    private int filterCoordinatesWithBombs(long neighborCoordinates) {
        return (int) neighborCoordinates;
    }

    private Vector<Coordinate> neighborCoordinatesAt(Coordinate coordinate) {
        final Vector<Coordinate> neighborCoordinates = new Vector<>(8);
        neighborCoordinates.add(new Coordinate(coordinate.x() + 1, coordinate.y() + 1));
        neighborCoordinates.add(new Coordinate(coordinate.x() + 1, coordinate.y()));
        neighborCoordinates.add(new Coordinate(coordinate.x() + 1, coordinate.y() - 1));
        neighborCoordinates.add(new Coordinate(coordinate.x() - 1, coordinate.y() + 1));
        neighborCoordinates.add(new Coordinate(coordinate.x() - 1, coordinate.y()));
        neighborCoordinates.add(new Coordinate(coordinate.x() - 1, coordinate.y() - 1));
        neighborCoordinates.add(new Coordinate(coordinate.x(), coordinate.y() + 1));
        neighborCoordinates.add(new Coordinate(coordinate.x(), coordinate.y() - 1));

        return neighborCoordinates;
    }

    public int minY() {
        return this.minY;
    }

    public int maxY() {
        return this.maxY;
    }

    public int minX() {
        return this.minX;
    }

    public int maxX() {
        return this.maxX;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int seed() {
        return (int) this.seed;
    }

    public void setFlagAt(Coordinate coordinate) {
        if (!this.fields.contains(coordinate)) {
            return;
        }

        if (this.flags.contains(coordinate)) {
            return;
        }

        if (this.bombs.contains(coordinate)) {
            this.flags.add(coordinate);
        }
    }
}
