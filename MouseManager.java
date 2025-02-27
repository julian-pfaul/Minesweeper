import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayDeque;
import java.util.Queue;

public class MouseManager extends MouseAdapter {
    private int mouseX;
    private int mouseY;

    private final Queue<Integer> pressedButtonQueue;
    private final Queue<Integer> releasedButtonQueue;

    private final Queue<Integer> scrollQueue;

    public MouseManager() {
        this.mouseX = 0;
        this.mouseY = 0;
        this.pressedButtonQueue = new ArrayDeque<>();
        this.releasedButtonQueue = new ArrayDeque<>();
        this.scrollQueue = new ArrayDeque<>();
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public boolean hasNextPressedButton() {
        return !pressedButtonQueue.isEmpty();
    }

    public int nextPressedButton() {
        if(pressedButtonQueue.isEmpty()) {
            return MouseEvent.NOBUTTON;
        }

        return pressedButtonQueue.poll();
    }

    public boolean hasNextReleasedButton() {
        return !releasedButtonQueue.isEmpty();
    }

    public int nextReleasedButton() {
        if(releasedButtonQueue.isEmpty()) {
            return MouseEvent.NOBUTTON;
        }

        return releasedButtonQueue.poll();
    }

    public boolean hasNextScroll() {
        return !scrollQueue.isEmpty();
    }

    public int nextScroll() {
        if(scrollQueue.isEmpty()){
            return 0;
        }

        return scrollQueue.poll();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        this.pressedButtonQueue.add(e.getButton());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        this.releasedButtonQueue.add(e.getButton());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        this.scrollQueue.add(e.getUnitsToScroll());
    }
}
