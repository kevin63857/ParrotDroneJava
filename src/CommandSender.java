


import java.io.IOException;
import java.net.*;

import JonCommands.*;

public class CommandSender implements Runnable
{
    private static final int CMD_PORT = 5556;

    private CommandQueue     cmd_queue;
    private ARDroneJon          drone;
    private InetAddress      drone_addr;
    private DatagramSocket   cmd_socket;
    private int              sequence = 1;


    public CommandSender(CommandQueue cmd_queue, ARDroneJon drone, InetAddress drone_addr, DatagramSocket cmd_socket)
    {
        this.cmd_queue = cmd_queue;
        this.drone = drone;
        this.drone_addr = drone_addr;
        this.cmd_socket = cmd_socket;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                DroneCommand c = cmd_queue.take();
                if(c instanceof QuitCommand)
                {
                    // Terminating
                    break;
                }

                if(c instanceof ATCommand)
                {
                    ATCommand cmd = (ATCommand) c;
                    //if(!(c instanceof KeepAliveCommand) && !(c instanceof MoveCommand) && !(c instanceof HoverCommand) && c.getStickyCounter()==0)
                    byte[] pdata = cmd.getPacket(sequence++); 
                    System.out.println("Sending DatagramPacket with info: ");
                    for(byte b:pdata){
                    	System.out.print((char)b);
                    }
                    System.out.println();
                    if(c instanceof HoverCommand){
                    	System.out.println("This is a hover command");
                    }
                    DatagramPacket p = new DatagramPacket(pdata, pdata.length, drone_addr, CMD_PORT);
                    cmd_socket.send(p);
                }
            } catch(InterruptedException e)
            {
                // ignoring
            } catch(IOException e)
            {
                drone.changeToErrorState(e);
                break;
            }
        }
    }

}
