package io.github.applecommander.bastools.api.shapes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class VectorShape implements Shape {
    public static VectorShape from(ByteBuffer buf) {
        Objects.requireNonNull(buf);
        VectorShape shape = new VectorShape();

        VectorCommand[] commands = VectorCommand.values();
        while (buf.hasRemaining()) {
            int code = Byte.toUnsignedInt(buf.get());
            if (code == 0) break;
            
            int vector1 = code & 0b111;
            int vector2 = (code >> 3) & 0b111;
            int vector3 = (code >> 6) & 0b011;  // Cannot plot
            
            shape.vectors.add(commands[vector1]);

            if (vector2 != 0 || vector3 != 0) {
                shape.vectors.add(commands[vector2]);
                
                if (vector3 != 0) {
                    shape.vectors.add(commands[vector3]);
                }
            }
        }
        return shape;
    }
    
	public final List<VectorCommand> vectors = new ArrayList<>();
	
	public VectorShape moveUp()    { return add(VectorCommand.MOVE_UP);    } 
	public VectorShape moveRight() { return add(VectorCommand.MOVE_RIGHT); } 
	public VectorShape moveDown()  { return add(VectorCommand.MOVE_DOWN);  } 
	public VectorShape moveLeft()  { return add(VectorCommand.MOVE_LEFT);  } 
	public VectorShape plotUp()    { return add(VectorCommand.PLOT_UP);    } 
	public VectorShape plotRight() { return add(VectorCommand.PLOT_RIGHT); } 
	public VectorShape plotDown()  { return add(VectorCommand.PLOT_DOWN);  } 
	public VectorShape plotLeft()  { return add(VectorCommand.PLOT_LEFT);  }
	
	private VectorShape add(VectorCommand vectorCommand) {
		this.vectors.add(vectorCommand);
		return this;
	}
	
	public void appendShortCommands(String line) {
	    for (char cmd : line.trim().toCharArray()) {
	        switch (cmd) {
	        case 'u': moveUp();    break;
	        case 'd': moveDown();  break;
	        case 'l': moveLeft();  break;
	        case 'r': moveRight(); break;
            case 'U': plotUp();    break;
            case 'D': plotDown();  break;
            case 'L': plotLeft();  break;
            case 'R': plotRight(); break;
            default:
                if (Character.isWhitespace(cmd)) {
                    // whitespace is allowed
                    continue;
                }
                throw new RuntimeException("Unknown command: " + cmd);
	        }
	    }
	}
	
	public void appendLongCommands(String line) {
	    Queue<String> tokens = new LinkedList<>(Arrays.asList(line.split("\\s+")));
	    while (!tokens.isEmpty()) {
	        String command = tokens.remove();
	        int count = 1;
	        String checkNumber = tokens.peek();
	        if (checkNumber != null && checkNumber.matches("\\d+")) count = Integer.parseInt(tokens.remove());
	        
	        for (int i=0; i<count; i++) {
	            switch (command.toLowerCase()) {
	            case "moveup":    moveUp();    break;
	            case "movedown":  moveDown();  break;
	            case "moveleft":  moveLeft();  break;
	            case "moveright": moveRight(); break;
	            case "plotup":    plotUp();    break;
	            case "plotdown":  plotDown();  break;
	            case "plotleft":  plotLeft();  break;
	            case "plotright": plotRight(); break;
	            default:
	                throw new RuntimeException("Unknown command: " + command);
	            }
	        }
	    }
	}
	
	@Override
	public boolean isEmpty() {
	    return vectors.isEmpty();
	}

	@Override
	public BitmapShape toBitmap() {
	    BitmapShape shape = new BitmapShape();
	    
	    int x = 0;
	    int y = 0;
	    for (VectorCommand command : vectors) {
	        if (command.plot) {
                while (y < 0) {
                    shape.insertRow();
                    y += 1;
                } 
                while (y >= shape.getHeight()) {
                    shape.addRow();
                }
	            while (x < 0) {
	                shape.insertColumn();
	                x += 1;
	            } 
	            while (x >= shape.getWidth()) {
	                shape.addColumn();
	            }
	            shape.plot(x,y);
	        }
	        x += command.xmove;
	        y += command.ymove;
	    }
	    
		return shape;
	}

	@Override
	public VectorShape toVector() {
		return this;
	}
}
