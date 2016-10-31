import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import JonCommands.Key;

/**
 * ARTest3 is the entry point for the Java implementation of an ARDrone 2.0 controller.
 * Written by Jonathan Roche late March 2015
 * 
 * This class includes capabilities for streaming video from the drone and controlling drone movements
 * The basic control scheme is:
 *     w, a, s and d for forward and backward movement as well as strafing left and right
 *     up, down, left and right on the arrow keys for altitude control and rotational control.
 *     q is used to either take off or land (I suggest only landing when either in a bad situation or really close to the ground
 * 
 * This class is only a graphical interface between the user and the actual controller
 * 
 */
public class ARTest3 extends JFrame implements ARTest{
	private static final long serialVersionUID = 1L; //required by JFrame
	static ARTest3 window=null; //The instance of ARTest3 created by the main method
	BufferedImage curImg=null;  //The current frame received from the drone and sent to this class for display
	JPanel daPics=null;  //The JPanel to hold everything inside the JFrame
	Communicator coms=null;  //The actual device that communicates with the virtual ARDrone
	NavData NAVDATA=null;
	//the following arrays contain data about the state and location of the buttons telling the user what key is pressed
	boolean[] pressed={false,false,false,false,false,false,false,false,false};
	String[] chars={"invalid","w","a","s","d","up","left","down","right"};
	int[] xCord={0,200,100,200,400,1300,1200,1300,1500};
	int[] yCord={0,100,100,200,100,100,100,200,100};
	
		JButton navStart =new JButton("Press to start Nav Data Handler");
	
	public ARTest3(){
		super("Controller");//Set title
		this.addKeyListener(new ThisListener(this)); //Adds a key listener to the window to detect key presses and releases
		navStart.addChangeListener(new ButtonListener(navStart));

		//some basic graphics operations
		daPics=new DrawPanel();
		add(daPics);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1700, 700);
		setVisible(true);
		this.requestFocus();

		// Start up the communications engine
		coms = new Communicator(this);
		coms.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Turn on the navigation data handler
		try {
			coms.drone2.connectNavData();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	public static void main(String[] args) {
		window =new ARTest3();
		//yeah, that's all here for now
	}
	
	//this method just makes my life easier when repainting the screen, it allows for multiple buffers to eliminate lag and screen flickering
	protected void initComponents() {
		remove(daPics);
		daPics=new DrawPanel();
		add(daPics);
		this.invalidate();
		daPics.repaint();
		this.revalidate();		
	}
	/**
	 * The following class is specific for use just here and contains a pain method specific to drawing out the UI here
	 */
	class DrawPanel extends JPanel{
		private static final long serialVersionUID = 1L;
		public DrawPanel(){
            setPreferredSize(new Dimension(1700,400));
		}
		@Override
        public void paint(Graphics g) {
        	super.paint(g);
	    	for(int i=1;i<9;i++){
                BufferedImage start=null;
    			try {    
    				java.net.URL zero=this.getClass().getResource("/images/"+chars[i]+(!pressed[i]? "":"pressed")+".png");
    				start = ImageIO.read(zero);
    			} catch (IOException e1) {
    				e1.printStackTrace();
    			}
	    		g.drawImage(start, xCord[i], yCord[i], null);
	    	}
	    	if(coms.takenOff){
	    		g.drawString("Press 'Q' to land", 10, 10);
	    	}
	    	else if(!coms.takenOff){
	    		g.drawString("Press 'Q' \n to take off", 10, 10);
	    	}
	    	if(NAVDATA!=null)
	    		g.drawString(NavData.toString(NAVDATA),20,20);
	    	g.drawImage(curImg, 550, 50, 600, 600, null);
    		add(navStart);
    		navStart.setBounds(100, 10, 250, 50);
    		navStart.setVisible(true);
	    }
	}
	
	/**
	 * This class just holds some custom code for dealing with key strokes
	 */
	class ThisListener extends KeyAdapter{
		int[] codes={999,87,65,83,68,38,37,40,39,81};
		String[] chars={"invalid","w","a","s","d","up","left","down","right","q"};
		ARTest3 parent=null;
		public ThisListener(ARTest3 parent){
			this.parent=parent;
		}
		private int indexOf(int keyCode, int[] codes2) {
			for(int i=codes2.length-1;i>0;i--){
				if(codes2[i]==keyCode)
					return i;
			}
			return 0;
		}
		@Override
		public void keyPressed(KeyEvent e){
			if(indexOf(e.getKeyCode(),codes)<=8)
				pressed[indexOf(e.getKeyCode(),codes)]=true;
			coms.addKeyStroke(new Key(chars[indexOf(e.getKeyCode(),codes)],Key.KEY_PRESSED));//Tells the communications class that a certain key has been pressed
			parent.initComponents();
		}
		@Override
		public void keyReleased(KeyEvent e){
			if(indexOf(e.getKeyCode(),codes)<=8){
				pressed[indexOf(e.getKeyCode(),codes)]=false;
				coms.addKeyStroke(new Key(chars[indexOf(e.getKeyCode(),codes)],Key.KEY_RELEASED));//Tells the communications class that a certain key has been released
			}
			parent.initComponents();
		}
	}
	//This little method is used only by the virtual instance of the drone to give a new image to the graphics and tell it to show that image.
	@Override
	public void notifyOfImg(BufferedImage curImg) {
		//System.out.println("getting IMG");
		this.curImg=curImg;
		this.initComponents();		
	}
	public void weGotData(NavData nd) {
		NavData.printState(nd);		
		NAVDATA=nd;
	}
	class ButtonListener implements ChangeListener{
		JButton but=null;
		public ButtonListener(JButton but){
			this.but=but;
		}
		@Override
		public void stateChanged(ChangeEvent e) {
			if(but.isEnabled()){
				try {
					coms.drone2.connectNavData();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				but.setEnabled(false);
			}
		}
	}
}
