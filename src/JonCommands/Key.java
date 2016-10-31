package JonCommands;

public class Key {
	public static final int KEY_PRESSED=1;
	public static final int KEY_RELEASED=2;
	public String keyVal="";
	public int eventAction=0;
	public Key(String keyVal, int eventAction){
		this.keyVal=keyVal;
		this.eventAction=eventAction;
	}
	
}
