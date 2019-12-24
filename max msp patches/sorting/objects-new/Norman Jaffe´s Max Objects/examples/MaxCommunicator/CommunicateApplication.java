/*
	Application that uses a TalkToMax object to interact with the Max
	TCP/IP server objects
*/

import java.io.*;
import MaxMessage;
import TalkToMax;

public class CommunicateApplication
{
    public static String getCommandWord
        (String value)
    {
        int endOfCommand = value.indexOf(' ');
        
        if (endOfCommand == -1)
        {
            return value;
        }
        
        return value.substring(0, endOfCommand);
    } // getCommandWord
    
    public static MaxMessage assembleQuery
        (String value)
    {
        MaxMessage holder = new MaxMessage();
        int        end = value.length();
        int        walker = 0;
        
        // Get the next token from the string
        while (end > 0)
        {
            float   asFloat = 0;
            int     asInt = 0;
            boolean wasFloat = false;
            boolean wasInt = false;
            
            while (walker < end)
            {
                char temp = value.charAt(walker);
                
                if (temp == '\\')
                {
                    walker++;
                }
                else if (temp == ' ')
                {
                    break;
                }
                
                walker++;
            }
            String token = value.substring(0, walker);
            
            // Try to determine the kind of token:
            try
            {
                asInt = Integer.valueOf(token).intValue();
                wasInt = true;
            }
            catch (NumberFormatException en)
            {
                // Not recognized as an integer
            }
            if (! wasInt)
            {
                try
                {
                    asFloat = Float.valueOf(token).floatValue();
                    wasFloat = true;
                }
                catch (NumberFormatException ef)
                {
                    // Not recognized as a float
                }
            }
            if (wasFloat)
            {
                holder.add(asFloat);
            }
            else if (wasInt)
            {
                holder.add(asInt);
            }
            else
            {
                holder.add(token);
            }
            if (end > walker)
            {
                value = value.substring(walker + 1).trim();
                end = value.length();
                walker = 0;
            }
            else
            {
                break;
            }
        }
        return holder;
    } // assembleQuery
    
	public static void main
	    (String args[])
	{
	    InputStreamReader inputReader = new InputStreamReader(System.in);
	    BufferedReader    input = new BufferedReader(inputReader);
	    TalkToMax         portal = new TalkToMax();
	    String            host = portal.getAddress();
	    short             port = portal.getPort();
	    boolean           okSoFar;
	    
	    do
	    {
	        okSoFar = false;
    	    try
    	    {
    	        System.out.print("Host name or address: ");
    	        host = input.readLine().trim();
    	        System.out.print("Host port: ");
    	        port = Short.parseShort(input.readLine().trim());
    	        okSoFar = true;
    	    }
    	    catch (java.io.IOException ee)
    	    {
    	        System.out.println("Bad host name, address or port number");
    	    }
	    }
	    while (! okSoFar);
	    portal.setAddress(host);
	    portal.setPort(port);
	    while (okSoFar)
	    {
	        try
	        {
	            System.out.print("Command: ");
	            String commandLine = input.readLine().trim();
	            String commandWord = getCommandWord(commandLine);
	           
	            if ((commandWord.equalsIgnoreCase("quit")) ||
	                (commandWord.equalsIgnoreCase("stop")))
	            {
	                okSoFar = false;
	            }
	            else if (commandWord.equalsIgnoreCase("connect"))
	            {
	                if (portal.isConnected())
	                {
	                    System.out.println("already connected");
	                }
	                else if (portal.connect())
	                {
	                    System.out.println("connected");
	                }
	            }
	            else if (commandWord.equalsIgnoreCase("disconnect"))
	            {
	                if (! portal.isConnected())
	                {
	                    System.out.println("not connected");
	                }
	                else if (portal.disconnect())
	                {
	                    System.out.println("disconnected");
	                }
	            }
	            else if (commandWord.equalsIgnoreCase("port"))
	            {
	                if (portal.isConnected())
	                {
	                    System.out.println("disconnect before changing port");
	                }
	                else
	                {
    	                commandLine = commandLine.substring(commandWord.length()).trim();
                        port = Short.parseShort(commandLine);
                        portal.setPort(port);
                        System.out.println("host port changed");
                    }
	            }
	            else if (commandWord.equalsIgnoreCase("receive"))
	            {
	                if (portal.isConnected())
	                {
	                    MaxMessage response = new MaxMessage();
	                    
	                    if (portal.receiveReply(response))
	                    {
	                        System.out.println("received: " + response);
	                    }
	                }
	                else
	                {
	                    System.out.println("not connected");
	                }
	            }
	            else if (commandWord.equalsIgnoreCase("send"))
	            {
	                if (portal.isConnected())
	                {
    	                // Parse the rest of the line
    	                commandLine = commandLine.substring(commandWord.length()).trim();
    	                MaxMessage message = assembleQuery(commandLine);
    	                
    	                if (portal.sendQuery(message))
    	                {
    	                    System.out.println("sent: " + commandLine);
    	                }
	                }
	                else
	                {
	                    System.out.println("not connected");
	                }
	            }
	            else if (commandWord.equalsIgnoreCase("server"))
	            {
	                if (portal.isConnected())
	                {
	                    System.out.println("disconnect before changing server");
	                }
	                else
	                {
    	                commandLine = commandLine.substring(commandWord.length()).trim();
    	                portal.setAddress(commandLine);
    	                System.out.println("host address changed");
	                }
	            }
	            else if (commandWord.equalsIgnoreCase("status"))
	            {
	                System.out.println(portal.isConnected() ? "connected" : "not connected");
	            }
	            else
	            {
	                System.out.println("unknown command: " + commandWord);
	                System.out.println("Known commands:");
	                System.out.println("  connect");
	                System.out.println("  disconnect");
	                System.out.println("  port <number>");
	                System.out.println("  quit");
	                System.out.println("  receive");
	                System.out.println("  send <message>");
	                System.out.println("  server <address>");
	                System.out.println("  status");
	                System.out.println("  stop");
	            }
	        }
	        catch (java.io.IOException ee)
	        {
                System.out.println("Exception: " + ee.getMessage());
	            System.out.println("Problem with the command");
	        }
		}
		if (portal.isConnected())
		{
		    portal.disconnect();
		}
		System.out.println("All done.");
		System.exit(0);
	} // main
	
}
