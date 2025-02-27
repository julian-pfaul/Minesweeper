import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Queue;

public class KeyManager extends KeyAdapter {
    private final Queue<Integer> keyQueue;

    public KeyManager() {
        this.keyQueue = new ArrayDeque<>();
    }

    public boolean hasNextKey() {
        return !keyQueue.isEmpty();
    }

    public int nextKey() {
        if(keyQueue.isEmpty()){
            return KeyEvent.VK_UNDEFINED;
        }

        return keyQueue.poll();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        keyQueue.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        super.keyReleased(e);
    }
}
