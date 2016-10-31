import java.io.IOException;

import JonCommands.Key;
public class Communicator extends Thread{
	public static final int MOVE_PERCENT=1000;
	ARTest3 parent=null;
	boolean takenOff=false;
	public Communicator(ARTest3 parent){
		this.parent=parent;
	}
	ARDroneJon drone2=null;
	public void run(){
		try {
			drone2=new ARDroneJon(parent);
			drone2.connect();
			if(parent instanceof ARTest3  || true){
				System.out.println("connecting video");
				drone2.connectVideo();
			}
			drone2.clearEmergencySignal();
			drone2.trim();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void addKeyStroke(Key e){
		String[] chars={"invalid","w","a","s","d","up","left","down","right","q"};
		int keyThing=indexOf(e.keyVal,chars);
		try{
			if(e.eventAction==Key.KEY_RELEASED){
				drone2.hover();
				drone2.trim();
			}
			switch(e.keyVal){
			case "w":
				drone2.move(-1*MOVE_PERCENT,0,0,0);
			break;
			case "s":
				drone2.move(MOVE_PERCENT,0,0,0);
			break;
			case "a":
				drone2.move(0,-1*MOVE_PERCENT,0,0);
			break;
			case "d":
				drone2.move(0,MOVE_PERCENT,0,0);
			break;
			case "up":
				drone2.move(0,0,MOVE_PERCENT,0);
			break;
			case "down":
				drone2.move(0,0,-1*MOVE_PERCENT,0);
			break;
			case "left":
				drone2.move(0,0,0,-1*MOVE_PERCENT);
			break;
			case "right":
				drone2.move(0,0,0,MOVE_PERCENT);
			break;
			case "q":
				if(!takenOff){
					drone2.clearEmergencySignal();
					drone2.trim();
					drone2.takeOff();
					takenOff=true;
				}
				else{
					drone2.land();
					takenOff=false;
				}
			break;
			case "invalid":
				drone2.hover();
			break;
			}
		}
		catch(Exception e2){
			
		}
	}
	private int indexOf(String keyVal, String[] chars) {
		for(int i=chars.length-1;i>0;i--){
			if(chars[i]==keyVal)
				return i;
		}
		return 0;
	}
}
