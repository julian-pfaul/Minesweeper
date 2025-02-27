import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class GameFramework {
    public static final long SEED = 92161842;

    private static final boolean FULLSCREEN = true;
    private static final int DEFAULT_FRAME_WIDTH = 800;
    private static final int DEFAULT_FRAME_HEIGHT = 800;

    private static final long SECOND = 1000L;
    private static final int TARGET_FRAMES_PER_SECOND = 640;

    private final JFrame frame;
    private final Canvas canvas;

    private final KeyManager keyManager;
    private final MouseManager mouseManager;

    private long timer;

    private boolean running;

    private final GameDisplay game;

    public GameFramework() {
        this.frame = new JFrame();
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if(FULLSCREEN) {
            this.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            this.frame.setUndecorated(true);
        } else {
            this.frame.setSize(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
        }

        this.canvas = new Canvas();
        this.frame.add(this.canvas);

        this.keyManager = new KeyManager();
        this.canvas.addKeyListener(this.keyManager);

        this.mouseManager = new MouseManager();
        this.canvas.addMouseListener(this.mouseManager);
        this.canvas.addMouseMotionListener(this.mouseManager);
        this.canvas.addMouseWheelListener(this.mouseManager);

        this.frame.setVisible(true);

        this.canvas.createBufferStrategy(2);

        this.timer = 0L;

        this.running = false;

        this.game = new GameDisplay();
    }

    private void clean() {
        this.game.clean();
        this.frame.dispose();
    }


    public void run() {
        this.running = true;

        long time, last = System.currentTimeMillis();

        while (this.running) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
                this.running = false;
            }

            time = System.currentTimeMillis();

            final long delta = time - last;

            this.timer += delta;

            last = time;

            this.game.processInput(this.canvas, this.keyManager, this.mouseManager);

            if(this.timer >= SECOND / TARGET_FRAMES_PER_SECOND){
                this.game.update();

                BufferStrategy bufferStrategy = this.canvas.getBufferStrategy();
                Graphics graphics = bufferStrategy.getDrawGraphics();

                this.game.display(canvas, graphics);

                graphics.dispose();
                bufferStrategy.show();

                this.timer = 0;
            }
        }

        this.clean();
    }
}
