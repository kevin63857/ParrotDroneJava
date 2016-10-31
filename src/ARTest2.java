import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import JonCommands.Key;


public class ARTest2 extends JFrame implements ARTest{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static ARTest2 window=null;
	boolean[] pressed={false,false,false,false,false,false,false,false,false};
	String[] chars={"invalid","w","a","s","d","up","left","down","right"};
	int[] xCord={0,200,100,200,400,800,700,800,1000};
	int[] yCord={0,100,100,200,100,100,100,200,100};
	JPanel daPics=null;
	Communicator coms=null;
	public ARTest2(){
		super("BUTTONS");
		this.addKeyListener(new ThisListener(this));
		daPics=new DrawPanel();
		add(daPics);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 400);
		setVisible(true);
		this.requestFocus();
		//coms = new Communicator(this);
		coms.start();
	}
	public static void main(String[] args) {
		window =new ARTest2();
	}
	protected void initComponents() {
		remove(daPics);
		daPics=new DrawPanel();
		add(daPics);
		this.invalidate();
		daPics.repaint();
		this.revalidate();		
	}
	class DrawPanel extends JPanel{
		private static final long serialVersionUID = 1L;
		public DrawPanel(){
            setPreferredSize(new Dimension(1200,400));
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
	    		g.drawString("Press 'Q' to take off", 10, 10);
	    	}
	    }
	}
	class ThisListener extends KeyAdapter{
		int[] codes={999,87,65,83,68,38,37,40,39,81};
		String[] chars={"invalid","w","a","s","d","up","left","down","right","q"};
		ARTest2 parent=null;
		public ThisListener(ARTest2 parent){
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
			coms.addKeyStroke(new Key(chars[indexOf(e.getKeyCode(),codes)],Key.KEY_PRESSED));
			parent.initComponents();
		}
		@Override
		public void keyReleased(KeyEvent e){
			if(indexOf(e.getKeyCode(),codes)<=8)
				pressed[indexOf(e.getKeyCode(),codes)]=false;
			parent.initComponents();
		}
	}
	@Override
	public void notifyOfImg(BufferedImage curImg) {
		// TODO Auto-generated method stub
		
	}

}