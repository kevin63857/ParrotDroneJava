import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
//import org.apache.log4j.Logger;
import JonCommands.*;


public class ARDroneJon {    
	/*
	 * Some fairly self explanatory variables and constants.
	 */
	private Thread                          video_reader;
	private Thread                          nav_data_reader;
    private static final int                CMD_QUEUE_SIZE    = 64;
    private State                           state             = State.DISCONNECTED;
    private Object                          state_mutex       = new Object();
    private static byte[]                   DEFAULT_DRONE_IP  = { (byte) 192, (byte) 168, (byte) 1, (byte) 1 };
    private InetAddress                     drone_addr;
    private DatagramSocket                  cmd_socket;
    private CommandQueue                    cmd_queue         = new CommandQueue(CMD_QUEUE_SIZE);
    private CommandSender                   cmd_sender;
    private Thread                          cmd_sending_thread;
    private boolean                         emergencyMode     = true;
    private Object                          emergency_mutex   = new Object();
	private BufferedImage curImg;
	
	/*
	 * This method is called by a video reader and it tells an ARTest program to go get the current image and do what ever it wants with that
	 */
	public void setCurrentImage(BufferedImage image) {
		this.curImg = image;
		
		parent.notifyOfImg(curImg);
		//System.out.println("Notified of IMG change");
		// TODO Auto-generated method stub
		
	}
    private ARTest3 parent;
    /*
     * This is the basic constructor, used for when the drone is hosting the network that it is connected to.
     * I have yet to have to use the other constructor, but it might be usefull if we were piggy packing on some other network for communication
     */
	public ARDroneJon(ARTest3 parent) throws UnknownHostException
    {
        this(parent, InetAddress.getByAddress(DEFAULT_DRONE_IP));
    }

    public ARDroneJon(ARTest3 parent,InetAddress drone_addr)
    {
    	this.parent=parent;
        this.drone_addr = drone_addr;
    }
    /*
     * This method MUST be called before attempting to control the drone
     * It establishes the basic connections and starts up the threads used for maintaing the queues of commands to be sent to the drone
     */
    public void connect() throws IOException
    {
        try
        {
        	System.out.println("Starting connection");
            cmd_socket = new DatagramSocket();
            //control_socket = new Socket(drone_addr, CONTROL_PORT);
            cmd_sender = new CommandSender(cmd_queue, this, drone_addr, cmd_socket);
            cmd_sending_thread = new Thread(cmd_sender);
            cmd_sending_thread.start();
            changeState(State.CONNECTING);

        	System.out.println("Connected");

        } catch(IOException ex)
        {
            changeToErrorState(ex);
            throw ex;
        }
    }
    /*
     * This connection call is seperate from the regualr connect incase you want to use the drone without video as I first did
     */
    public void connectVideo() throws IOException
    {
        video_reader = new VideoStreamerJon(this, drone_addr, 5555, 99999);
        video_reader.start();
    }
    public void connectNavData() throws IOException
    {
    	System.out.println("Drone nav protocols started");
    	nav_data_reader = new NavDataHandler(this, drone_addr, 5554, 4096);
    	nav_data_reader.start();
    }
    /*
         * Yeah, so this is the primary movemnent command code
         * Move the drone
         * 
         * @param left_right_tilt The left-right tilt (aka. "drone roll" or phi
         *            angle) argument is a percentage of the maximum inclination as
         *            configured here. A negative value makes the drone tilt to its
         *            left, thus flying leftward. A positive value makes the drone
         *            tilt to its right, thus flying rightward.
         * @param front_back_tilt The front-back tilt (aka. "drone pitch" or theta
         *            angle) argument is a percentage of the maximum inclination as
         *            configured here. A negative value makes the drone lower its
         *            nose, thus flying frontward. A positive value makes the drone
         *            raise its nose, thus flying backward. The drone translation
         *            speed in the horizontal plane depends on the environment and
         *            cannot be determined. With roll or pitch values set to 0, the
         *            drone will stay horizontal but continue sliding in the air
         *            because of its inertia. Only the air resistance will then make
         *            it stop.
         * @param vertical_speed The vertical speed (aka. "gaz") argument is a
         *            percentage of the maximum vertical speed as defined here. A
         *            positive value makes the drone rise in the air. A negative
         *            value makes it go down.
         * @param angular_speed The angular speed argument is a percentage of the
         *            maximum angular speed as defined here. A positive value makes
         *            the drone spin right; a negative value makes it spin left.
         * @throws IOException
         */
    public void move(float left_right_tilt, float front_back_tilt, float vertical_speed, float angular_speed) throws IOException
    {
        cmd_queue.add(new MoveCommand(true, left_right_tilt, front_back_tilt, vertical_speed, angular_speed));
    }
    
    /*
     * Call if you want the drone to hover in place
     * @todo this method needs some serious work and needs some integration with a NAV Data controller to be able to more acurately hover and prevent drone drift
     * 
     */
    public void hover() throws IOException
    {
        cmd_queue.add(new HoverCommand());
    }
    
    /*
     * I'm still not sure if this is even working, but what it ought to do is clear any emergency signals that the drone is holding on to
     * This method should be called if the previous run of the drone ended in error and the drone has red lights.  Still not sure if it works
     */
    public void clearEmergencySignal() throws IOException
    {
        synchronized(emergency_mutex)
        {
            if(isEmergencyMode())
                cmd_queue.add(new EmergencyCommand());
        }
    }
    
    /*
     * This lands the drone, but it it appears to do it very abruptly and potentially damaging to the drone.
     * @todo I need to change up this method so that it uses NAV data and a move command
     */
    public void land() throws IOException
    {
        cmd_queue.add(new LandCommand());
        changeState(State.LANDING);
    }
    /*
     * This is really depreciated and probably not needed any more, but I still have it in here just incase
     * The purpose of this method was to allow the user's code to wait for a connection and assure them that they don't send commands to a disconnected drone
     * It is not needed any longer because I changed how the drone connects and it occurs fairly instantaneously
     */
    public void waitForReady(long how_long) throws IOException
    {
        long since = System.currentTimeMillis();
        synchronized(state_mutex)
        {
            while(true)
            {
                if((System.currentTimeMillis() - since) >= how_long)
                {   // Timeout, too late
                    throw new IOException("Timeout connecting to ARDrone");
                } else if(state == State.DEMO)
                {
                    return; // OK! We are now connected
                } else if(state == State.ERROR || state == State.DISCONNECTED)
                {
                    throw new IOException("Connection Error");
                }

                long p = Math.min(how_long - (System.currentTimeMillis() - since), how_long);
                if(p > 0)
                {
                    try
                    {
                        state_mutex.wait(p);
                    } catch(InterruptedException e)
                    {
                        // Ignore
                    }
                }
            }
        }
    }
    /*
     * This flattens out the drone and sets all movement to zero
     */
    public void trim() throws IOException
    {
        cmd_queue.add(new FlatTrimCommand());
    }
    /*
     * This tells the drone to start flying and sets a hover height of 1 meter
     */
    public void takeOff() throws IOException
    {   cmd_queue.add(new TakeOffCommand());
	    changeState(State.TAKING_OFF);
    }
    
    
    
    /*
     * The following methods should not need to be used ever again, but I think they are still used a bit in some random methods in other classes, so they are still here
     * They are depreciated and the entire state system is not neccessary any more.
     */
    //START USELESS CODE
    public void changeToErrorState(Exception ex)
    {
        synchronized(state_mutex)
        {   //log.debug("State changed from " + state + " to " + State.ERROR + " with exception ", ex);
            state = State.ERROR;
            state_mutex.notifyAll();
        }
    }
    private void changeState(State newstate) throws IOException
    {
        if(newstate == State.ERROR)
            changeToErrorState(null);

        synchronized(state_mutex)
        {
            if(state != newstate)
            {
               // log.debug("State changed from " + state + " to " + newstate);
                state = newstate;
                state_mutex.notifyAll();
            }
        }
    }
    public boolean isEmergencyMode()
    {
        return emergencyMode;
    }
    
    
    
    public enum State
    {
        DISCONNECTED, CONNECTING, BOOTSTRAP, DEMO, ERROR, TAKING_OFF, LANDING
    }

    /*
     * This enum was copied right out of some Parrot resources and I honestly have no idea what it used for, but it used not not work without it, so I am leaving it for now
     */
    public enum ConfigOption
    {
        ACCS_OFFSET("control:accs_offset"), ACCS_GAINS("control:accs_gains"), GYROS_OFFSET("control:gyros_offset"), GYROS_GAINS(
                "control:gyros_gains"), GYROS110_OFFSET("control:gyros110_offset"), GYROS110_GAINS(
                "control:gyros110_gains"), GYRO_OFFSET_THR_X("control:gyro_offset_thr_x"), GYRO_OFFSET_THR_Y(
                "control:gyro_offset_thr_y"), GYRO_OFFSET_THR_Z("control:gyro_offset_thr_z"), PWM_REF_GYROS(
                "control:pwm_ref_gyros"), CONTROL_LEVEL("control:control_level"), SHIELD_ENABLE("control:shield_enable"), EULER_ANGLE_MAX(
                "control:euler_angle_max"), ALTITUDE_MAX("control:altitude_max"), ALTITUDE_MIN("control:altitude_min"), CONTROL_TRIM_Z(
                "control:control_trim_z"), CONTROL_IPHONE_TILT("control:control_iphone_tilt"), CONTROL_VZ_MAX(
                "control:control_vz_max"), CONTROL_YAW("control:control_yaw"), OUTDOOR("control:outdoor"), FLIGHT_WITHOUT_SHELL(
                "control:flight_without_shell"), BRUSHLESS("control:brushless"), AUTONOMOUS_FLIGHT(
                "control:autonomous_flight"), MANUAL_TRIM("control:manual_trim"), INDOOR_EULER_ANGLE_MAX(
                "control:indoor_euler_angle_max"), INDOOR_CONTROL_VZ_MAX("control:indoor_control_vz_max"), INDOOR_CONTROL_YAW(
                "control:indoor_control_yaw"), OUTDOOR_EULER_ANGLE_MAX("control:outdoor_euler_angle_max"), OUTDOOR_CONTROL_VZ_MAX(
                "control:outdoor_control_vz_max"), OUTDOOR_CONTROL_YAW("outdoor_control:control_yaw");

        private String value;

        private ConfigOption(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        
    }
    //END USELESS CODE

	public void navDataReceived(NavData nd) {
		parent.weGotData(nd);
		// TODO Auto-generated method stub
		
	}


}
