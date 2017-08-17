import java.awt.AWTException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommandInterpreter {

	private MouseController mover;

	private Map<String, Runnable> commands;
	
	public CommandInterpreter() throws AWTException{
		mover = new MouseController();
		commands = new HashMap();
		setCommands();
	}

	private void setCommands() {
		commands.put("right click", () -> mover.rightClick());
		commands.put("left click", () -> mover.leftClick());
		commands.put("scroll up", () -> mover.scrollUp());
		commands.put("scroll down", () -> mover.scrollDown());
	}

	public void interpretCommand(String command) throws IOException{
		executeCommand(command.trim());
	}
	
	private void executeCommand(String command){
		if(command.contains(",")) {
			mover.moveMouse(command);
		}
		else{
			commands.get(command).run();
		}
	}
}