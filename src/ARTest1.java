import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

//import com.codeminders.ardrone.ARDrone;

//HI 


public class ARTest1 implements ARTest
{
	public final static boolean MINE=true;
    private static final long CONNECT_TIMEOUT = 30000;

    /**
     * @param args
     */
    public static void main(String[] args)
    {	
    	System.out.println(KeyEvent.VK_Q);
    	System.exit(0);
    	//BasicUI ui=new BasicUI("DRONE STUFF");
        ARDroneJon drone2;
        //ARDrone drone;
        //new Thread(){public void run(){Scanner scan=new Scanner(System.in);String in=scan.next();System.exit(1);}}.start();
        //inserting fucktons of code here
        try
        {
        	if(MINE){
        		drone2=new ARDroneJon(null);
            	drone2.connect();
            	//drone2.waitForReady(CONNECT_TIMEOUT);
            	System.out.println("Clearing emergency signal");
            	drone2.clearEmergencySignal();
                Thread.sleep(3000);
                drone2.trim();
                System.err.println("Taking off");
                drone2.takeOff();
                Thread.sleep(10000);
                long start=System.currentTimeMillis();
                while(System.currentTimeMillis()-start<10000)
                	drone2.move(0,0,-1,0);
                System.err.println("Landing");
	            //drone2.land();
	            // Give it some time to land
                Thread.sleep(2000);
                System.exit(0);
        	}
//        	else{
//	            // Create ARDrone object,
//	            // connect to drone and initialize it.
//	        	drone = new ARDrone();
//	        	drone.connect();
//	        	drone.clearEmergencySignal();
//	            drone.disableAutomaticVideoBitrate();
//	        	drone.waitForReady(CONNECT_TIMEOUT);
//	            Thread.sleep(3000);
//	            drone.trim();
//	            System.err.println("Taking off");
//	            drone.takeOff();
//	            //drone.move(0, 0, (float) .01, 0);
//	            //drone.hover();
//	            Thread.sleep(1000);
//	            // Land
//	            System.err.println("Landing");
//	            drone.land();
//	            // Give it some time to land
//	            Thread.sleep(2000);
//	            
//	            // Disconnect from the done
//	            drone.disconnect();
//        	}

        } catch(Throwable e)
        {
            e.printStackTrace();
        }
        /*
        try
        {
            // Create ARDrone object,
            // connect to drone and initialize it.
            drone = new ARDrone();
            drone.connect();
            drone.clearEmergencySignal();
            drone.disableAutomaticVideoBitrate();
            // Wait until drone is ready
            drone.waitForReady(CONNECT_TIMEOUT);
            Thread.sleep(3000);
            // do TRIM operation
            drone.trim();

            // Take off
            System.err.println("Taking off");
            //drone.takeOff();
            drone.move(0, 0, (float) .01, 0);
            //drone.hover();

            // Fly a little :)
            Thread.sleep(5000);

            // Land
            System.err.println("Landing");
            drone.land();

            // Give it some time to land
            Thread.sleep(2000);
            
            // Disconnect from the done
            drone.disconnect();

        } catch(Throwable e)
        {
            e.printStackTrace();
        }
        */
    }

	@Override
	public void notifyOfImg(BufferedImage curImg) {
		// TODO Auto-generated method stub
		
	}
}
