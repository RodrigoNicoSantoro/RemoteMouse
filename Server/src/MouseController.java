import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class MouseController {

    private Robot robot;

    public MouseController() throws AWTException {
        robot = new Robot();
    }

    public void leftClick() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void rightClick() {
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

    public void moveMouse(String command) {
        float newX = Float.parseFloat(command.split(",")[0]);//extract movement in x direction
        float newY = Float.parseFloat(command.split(",")[1]);//extract movement in y direction
        Point point = MouseInfo.getPointerInfo().getLocation(); //Get current mouse position
        float currentX = point.x;
        float currentY = point.y;
        robot.mouseMove((int) (currentX + newX), (int) (currentY + newY));//Move mouse pointer to new location
    }

    public void scrollUp() {
        robot.mouseWheel(1);
    }

    public void scrollDown() {
        robot.mouseWheel(-1);
    }
}