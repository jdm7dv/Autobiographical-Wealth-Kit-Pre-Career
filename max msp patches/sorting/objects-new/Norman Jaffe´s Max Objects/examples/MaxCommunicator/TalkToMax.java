/*
	Class to send and receive from Max
*/

import java.io.*;
import java.net.*;
import MaxMessage;

public class TalkToMax
{

    private boolean     fConnected;
    private InetAddress fHost;
    private short       fPort;
    private Socket      fSocket;

    public TalkToMax()
    {
        fConnected = false;
        fPort = 8192;
        setAddress("localhost");
    } // constructor

    public boolean connect()
    {
        boolean okSoFar = true;
        
        try
        {
            fSocket = new Socket(fHost, fPort);
            fConnected = true;
        }
        catch (IOException ee)
        {
            System.out.println("Exception: " + ee.getMessage());
            System.out.println("Unable to connect to host");
            okSoFar = false;
        }
        return okSoFar;
    } // connect
    
    public boolean disconnect()
    {
        fSocket = null;
        fConnected = false;
        return true;
    } // disconnect
    
    public String getAddress()
    {
        return fHost.toString();
    } // getAddress
    
    public short getPort()
    {
        return fPort;
    } // getPort
    
    public boolean isConnected()
    {
        return fConnected;
    } // isConnected
    
    public boolean receiveReply
        (MaxMessage wasReceived)
    {
        boolean okSoFar = false;
        
        try
        {           
            InputStream     aStream = fSocket.getInputStream();
            DataInputStream input = new DataInputStream(aStream);
            byte[]          buffer = new byte[30000];
            short           howMany = (short) input.read(buffer);
            
            okSoFar = wasReceived.fromByteArray(buffer, howMany);
        }
        catch (Exception ee)
        {
            System.out.println("Exception: " + ee.getMessage());
            System.out.println("Problem receiving message");
        }
        return okSoFar;
    } // receiveReply
    
    public boolean sendQuery
        (MaxMessage toSend)
    {
        boolean okSoFar = false;
        
        try
        {           
            OutputStream     aStream = fSocket.getOutputStream();
            DataOutputStream output = new DataOutputStream(aStream);
            
            output.write(toSend.toByteArray());
            okSoFar = true;
        }
        catch (Exception ee)
        {
            System.out.println("Exception: " + ee.getMessage());
            System.out.println("Problem sending message");
        }
        return okSoFar;
    } // sendQuery
    
    public boolean setAddress
        (String server)
    {
        boolean okSoFar = true;
        
        try
        {
            fHost = InetAddress.getByName(server);
            System.out.println("Host: " + fHost);
        }
        catch (java.net.UnknownHostException ee)
        {
            System.out.println("Exception: " + ee.getMessage());
            System.out.println("Bad host name or address given");
            okSoFar = false;
        }
        return okSoFar;
    } // setAddress
    
    public boolean setPort
        (short port)
    {
        fPort = port;
        return true;
    } // setPort
 
}
