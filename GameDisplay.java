import javax.imageio.ImageIO;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GameDisplay {
    private static final int ROWS = 10;
    private static final int COLUMNS = 10;
    private static final float ZOOM_FACTOR = 0.01f;

    public static final int GAME_SIZE = 64;
    public static final int SPACES_PER_BOMB = 8;

    private final Map<FieldState, BufferedImage> textures;

    private float viewLeft, viewTop, viewRight, viewBottom;

    private int mouseCoordinateX;
    private int mouseCoordinateY;

    private final GameLogic gameLogic;

    private int mouseX;
    private int mouseY;
    private int mouseTranslationSourceX;
    private int mouseTranslationSourceY;
    private int mouseTranslationDestinationX;
    private int mouseTranslationDestinationY;
    private boolean translationMode;

    private int canvasWidth;
    private int canvasHeight;

    private final Vector<Coordinate> coordinatesToReveal;

    public GameDisplay() {
        this.viewLeft = -9.5f;
        this.viewTop = -9.5f;
        this.viewRight = 9.5f;
        this.viewBottom = 9.5f;

        this.mouseX = 0;
        this.mouseY = 0;
        this.mouseTranslationSourceX = 0;
        this.mouseTranslationSourceY = 0;
        this.mouseTranslationDestinationX = 0;
        this.mouseTranslationDestinationY = 0;

        this.canvasWidth = 0;
        this.canvasHeight = 0;

        this.translationMode = false;

        this.mouseCoordinateX = 0;
        this.mouseCoordinateY = 0;

        this.textures = new HashMap<>();
        this.textures.put(FieldState.NONE, loadTexture("resource/texture/black.jpg"));
        this.textures.put(FieldState.ERROR_STATE, loadTexture("resource/texture/explodiert.jpg"));
        this.textures.put(FieldState.BLANK, loadTexture("resource/texture/blank.jpg"));
        this.textures.put(FieldState.EMPTY, loadTexture("resource/texture/leer.jpg"));
        this.textures.put(FieldState.ONE, loadTexture("resource/texture/eins.jpg"));
        this.textures.put(FieldState.TWO, loadTexture("resource/texture/zwei.jpg"));
        this.textures.put(FieldState.THREE, loadTexture("resource/texture/drei.jpg"));
        this.textures.put(FieldState.FOUR, loadTexture("resource/texture/vier.jpg"));
        this.textures.put(FieldState.FIVE, loadTexture("resource/texture/fuenf.jpg"));
        this.textures.put(FieldState.SIX, loadTexture("resource/texture/sechs.jpg"));
        this.textures.put(FieldState.SEVEN, loadTexture("resource/texture/sieben.jpg"));
        this.textures.put(FieldState.EIGHT, loadTexture("resource/texture/acht.jpg"));
        this.textures.put(FieldState.UNREVEALED_BOMB, loadTexture("resource/texture/blank.jpg"));
        this.textures.put(FieldState.REVEALED_BOMB, loadTexture("resource/texture/explodiert.jpg"));
        this.textures.put(FieldState.FLAG, loadTexture("resource/texture/fahne.jpg"));

        this.gameLogic = new GameLogic();
        this.gameLogic.setSeed(0);
        this.gameLogic.generateProceduralGame();
        //this.gameLogic.generateSquareGame(GAME_SIZE, GAME_SIZE, (int) (GAME_SIZE * GAME_SIZE / SPACES_PER_BOMB));

        this.coordinatesToReveal = new Vector<>();
    }

    private BufferedImage loadTexture(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }

        return null;
    }

    private float viewWidth() {
        return this.viewRight - this.viewLeft;
    }

    private float viewHeight() {
        return this.viewBottom - this.viewTop;
    }

    private float viewLeft() {
        if (translationMode) {
            return this.viewLeft + (float) (this.mouseTranslationSourceX - this.mouseX) / this.canvasWidth * this.viewWidth();
        }

        return this.viewLeft;
    }

    private float viewTop() {
        if (translationMode) {
            return this.viewTop + (float) (this.mouseTranslationSourceY - this.mouseY) / this.canvasHeight * this.viewHeight();
        }

        return this.viewTop;
    }

    private float viewRight() {
        if (translationMode) {
            return this.viewRight + (float) (this.mouseTranslationSourceX - this.mouseX) / this.canvasWidth * this.viewWidth();
        }

        return this.viewRight;
    }

    private float viewBottom() {
        if (translationMode) {
            return this.viewBottom + (float) (this.mouseTranslationSourceY - this.mouseY) / this.canvasHeight * this.viewHeight();
        }

        return this.viewBottom;
    }

    public void processInput(Canvas canvas, KeyManager keyManager, MouseManager mouseManager) {
        this.canvasWidth = canvas.getWidth();
        this.canvasHeight = canvas.getHeight();

        this.mouseX = mouseManager.mouseX();
        this.mouseY = mouseManager.mouseY();

        this.mouseCoordinateX = (int) Math.floor((float) mouseX / canvas.getWidth() * this.viewWidth() + this.viewLeft());
        this.mouseCoordinateY = (int) Math.floor((float) mouseY / canvas.getHeight() * this.viewHeight() + this.viewTop());

        if (keyManager.hasNextKey()) {
            int key = keyManager.nextKey();

            if (key == KeyEvent.VK_N) {
                this.gameLogic.setSeed(this.gameLogic.seed() + 1);
                this.gameLogic.generateSquareGame(GAME_SIZE, GAME_SIZE, (int) (GAME_SIZE * GAME_SIZE / SPACES_PER_BOMB));
            }

            if (key == KeyEvent.VK_M) {
                this.gameLogic.setSeed(this.gameLogic.seed() + 1);
                this.gameLogic.generateProceduralGame();
            }
        }

        if (mouseManager.hasNextPressedButton()) {
            int button = mouseManager.nextPressedButton();

            if (button == MouseEvent.BUTTON2) {
                this.mouseTranslationSourceX = mouseManager.mouseX();
                this.mouseTranslationSourceY = mouseManager.mouseY();
                this.translationMode = true;
            }

            if (button == MouseEvent.BUTTON1) {
                final Coordinate coordinate = new Coordinate(this.mouseCoordinateX, this.mouseCoordinateY);
                final Vector<Coordinate> coordinatesToReveal = this.gameLogic.interactAt(coordinate);

                if (coordinatesToReveal != null) {
                    this.coordinatesToReveal.addAll(coordinatesToReveal);
                }
            }

            if (button == MouseEvent.BUTTON3) {
                final Coordinate coordinate = new Coordinate(this.mouseCoordinateX, this.mouseCoordinateY);

                this.gameLogic.setFlagAt(coordinate);
            }
        }

        if (mouseManager.hasNextReleasedButton()) {
            if (mouseManager.nextReleasedButton() == MouseEvent.BUTTON2) {
                this.mouseTranslationDestinationX = mouseManager.mouseX();
                this.mouseTranslationDestinationY = mouseManager.mouseY();
                this.translationMode = false;

                final float viewWidth = this.viewWidth();
                final float viewHeight = this.viewHeight();

                this.viewLeft += (float) (this.mouseTranslationSourceX - this.mouseTranslationDestinationX) / this.canvasWidth * viewWidth;
                this.viewTop += (float) (this.mouseTranslationSourceY - this.mouseTranslationDestinationY) / this.canvasHeight * viewHeight;
                this.viewRight += (float) (this.mouseTranslationSourceX - this.mouseTranslationDestinationX) / this.canvasWidth * viewWidth;
                this.viewBottom += (float) (this.mouseTranslationSourceY - this.mouseTranslationDestinationY) / this.canvasHeight * viewHeight;
            }
        }

        if (mouseManager.hasNextScroll()) {
            final int scroll = mouseManager.nextScroll();

            this.viewLeft -= scroll * this.viewWidth() * ZOOM_FACTOR;
            this.viewTop -= scroll * this.viewHeight() * ZOOM_FACTOR;
            this.viewRight += scroll * this.viewWidth() * ZOOM_FACTOR;
            this.viewBottom += scroll * this.viewHeight() * ZOOM_FACTOR;
        }
    }

    public void display(Canvas canvas, Graphics graphics) {
        drawBackground(canvas, graphics);
        drawGame(canvas, graphics);
        drawCursor(canvas, graphics);
        drawBezels(canvas, graphics);
    }

    /**
     * Zeichnet um das Feld über dem der Maus-Cursor sich befindet einen roten Rahmen.
     */
    private void drawCursor(Canvas canvas, Graphics graphics) {
        final int mouseFieldX = (int) (originX(canvas) + canvas.getWidth() / this.viewWidth() * this.mouseCoordinateX);
        final int mouseFieldY = (int) (originY(canvas) + canvas.getHeight() / this.viewHeight() * this.mouseCoordinateY);
        final int mouseFieldWidth = (int) (canvas.getWidth() / this.viewWidth());
        final int mouseFieldHeight = (int) (canvas.getHeight() / this.viewHeight());

        graphics.setColor(Color.RED);
        graphics.drawRect(mouseFieldX, mouseFieldY, mouseFieldWidth, mouseFieldHeight);
    }

    private void drawBezels(Canvas canvas, Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, (int) (canvas.getWidth() * 0.05) + 1, canvas.getHeight() + 1);
        graphics.fillRect(0, 0, canvas.getWidth() + 1, (int) (canvas.getHeight() * 0.05) + 1);
        graphics.fillRect((int) (canvas.getWidth() * 0.95), 0, (int) (canvas.getWidth() * 0.05 + 1), canvas.getHeight() + 1);
        graphics.fillRect(0, (int) (canvas.getHeight() * 0.95), canvas.getWidth() + 1, (int) (canvas.getHeight() * 0.05) + 1);
    }

    private void drawGame(Canvas canvas, Graphics graphics) {
        for (int row = topMostVisibleRow(); row < bottomMostVisibleRow(); ++row) {
            for (int column = leftMostVisibleColumn(); column < rightMostVisibleColumn(); ++column) {
                final Coordinate coordinate = new Coordinate(column, row);
                final FieldState state = this.gameLogic.fieldInfoAt(coordinate);

                this.drawField(canvas, graphics, coordinate, state);
            }
        }
    }

    private void drawField(Canvas canvas, Graphics graphics, Coordinate coordinate, FieldState state) {
        final int fieldX = (int) (originX(canvas) + canvas.getWidth() / this.viewWidth() * coordinate.x());
        final int fieldY = (int) (originY(canvas) + canvas.getHeight() / this.viewHeight() * coordinate.y());
        final int fieldWidth = (int) (canvas.getWidth() / this.viewWidth());
        final int fieldHeight = (int) (canvas.getHeight() / this.viewHeight());

        graphics.drawImage(texture(state), fieldX, fieldY, fieldWidth + 1, fieldHeight + 1, null);
    }

    private static void drawBackground(Canvas canvas, Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void clean() {

    }

    public void update() {
        Vector<Coordinate> newToReveal = new Vector<>();

        for (int batch = 0; batch < 12; batch++) {
            if (!this.coordinatesToReveal.isEmpty()) {
                Coordinate coordinate = this.coordinatesToReveal.get(0);

                Vector<Coordinate> result = this.gameLogic.interactAt(coordinate);

                if (result != null) {
                    newToReveal.addAll(result);
                }

                this.coordinatesToReveal.remove(0);
            }
        }

        this.coordinatesToReveal.addAll(newToReveal);
    }

    private float originX(Canvas canvas) {
        return -this.viewLeft() / this.viewWidth() * canvas.getWidth();
    }

    private float originY(Canvas canvas) {
        return -this.viewTop() / this.viewHeight() * canvas.getHeight();
    }

    private int rightMostVisibleColumn() {
        return (int) Math.floor(this.viewRight()) + 1;
    }

    private int leftMostVisibleColumn() {
        return (int) Math.ceil(this.viewLeft()) - 1;
    }

    private int bottomMostVisibleRow() {
        return (int) Math.floor(this.viewBottom()) + 1;
    }

    private int topMostVisibleRow() {
        return (int) Math.ceil(this.viewTop()) - 1;
    }

    private BufferedImage texture(FieldState state) {
        return this.textures.get(state);
    }
}
