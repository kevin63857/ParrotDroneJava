package JonCommands;

public class EmergencyCommand extends RefCommand
{
    public EmergencyCommand()
    {
        value |= (1<<8);
    }
}
