/*
	Class to construct a message object
*/

import java.io.*;
import java.util.*;
import MaxConstants;

public interface MaxSendableValue
{
    public void convert
        (DataOutputStream output,
        boolean           withTag)
        throws java.io.IOException;
        
    public short size();

}

public class MaxFloatValue implements MaxSendableValue
{
    private float fValue;
    
    public MaxFloatValue
        (float value)
    {
        fValue = value;
    } // constructor
    
    public void convert
        (DataOutputStream output,
        boolean           withTag)
        throws java.io.IOException
    {
        if (withTag)
        {
            output.writeByte(MaxConstants.A_FLOAT);
        }
        output.writeInt(Float.floatToIntBits(fValue));
    } // convert

    public short size()
    {
        return 4;
    } // size
    
    public String toString()
    {
        return Float.toString(fValue);
    } // toString

}

public class MaxIntValue implements MaxSendableValue
{
    private int fValue;
    
    public MaxIntValue
        (int value)
    {
        fValue = value;
    } // constructor
    
    public void convert
        (DataOutputStream output,
        boolean           withTag)
        throws java.io.IOException
    {
        if (withTag)
        {
            output.writeByte(MaxConstants.A_LONG);
        }
        output.writeInt(fValue);
    } // convert

    public short size()
    {
        return 4;
    } // size
 
    public String toString()
    {
        return Integer.toString(fValue);
    } // toString

}

public class MaxStringValue implements MaxSendableValue
{
    private String fValue;
    
    public MaxStringValue
        (String value)
    {
        fValue = value;
    } // constructor
    
    public void convert
        (DataOutputStream output,
        boolean           withTag)
        throws java.io.IOException
    {
        if (withTag)
        {
            output.writeByte(MaxConstants.A_SYM);
        }
        output.writeShort(fValue.length() + 1);
        output.writeBytes(fValue);
        output.writeByte(0); // guarantee the trailing null
    } // convert

    public short size()
    {
        return (short) (fValue.length() + 3);
    } // size
    
    public String toString()
    {
        return fValue;
    } // toString

}

public class MaxMessage
{
    private boolean fIsMonotype;
    private boolean fSawInt;
    private boolean fSawFloat;
    private boolean fSawSymbol;
    private short   fNumBytes;
    private Vector  fElements;
    
    public MaxMessage()
    {
        fSawInt = false;
        fSawFloat = false;
        fSawSymbol = false;
        fIsMonotype = true;
        fNumBytes = 0;
        fElements = new Vector();
    } // constructor
    
    public boolean add
        (float value)
    {
        if (fIsMonotype)
        {
            if (fSawInt)
            {
                fIsMonotype = false;
                fSawInt = false;
            }
            else if (fSawSymbol)
            {
                fIsMonotype = false;
                fSawSymbol = false;
            }
            else
            {
                fSawFloat = true;
            }
        }
        MaxFloatValue newValue = new MaxFloatValue(value);
        
        fNumBytes += newValue.size();
        fElements.addElement(newValue);
        return true;
    } // add
    
    public boolean add
        (int value)
    {
        if (fIsMonotype)
        {
            if (fSawFloat)
            {
                fIsMonotype = false;
                fSawFloat = false;
            }
            else if (fSawSymbol)
            {
                fIsMonotype = false;
                fSawSymbol = false;
            }
            else
            {
                fSawInt = true;
            }
        }
        MaxIntValue newValue = new MaxIntValue(value);
        
        fNumBytes += newValue.size();
        fElements.addElement(newValue);
        return true;
    } // add
    
    public boolean add
        (String value)
    {
        if (fIsMonotype)
        {
            if (fSawInt)
            {
                fIsMonotype = false;
                fSawInt = false;
            }
            else if (fSawFloat)
            {
                fIsMonotype = false;
                fSawFloat = false;
            }
            else
            {
                fSawSymbol = true;
            }
        }
        MaxStringValue newValue = new MaxStringValue(value);
        
        fNumBytes += newValue.size();
        fElements.addElement(newValue);
        return true;
    } // add
    
    public Vector elements()
    {
        return fElements;
    } // elements

    public boolean fromByteArray
        (byte[] buffer,
        short   howMany)
    {
        boolean okSoFar = true;
        
        try
        {
            ByteArrayInputStream sequence = new ByteArrayInputStream(buffer);
            DataInputStream      reader = new DataInputStream(sequence);
            
            fNumBytes = 0;
            fElements = new Vector();
            short inCount = reader.readShort();
            short sanityCheck = reader.readShort();
            byte  groupTag = reader.readByte();
            
            if ((inCount < 0) ||
                (sanityCheck != (short) (- (howMany + inCount))) ||
                ((groupTag != MaxConstants.A_LONG) &&
                    (groupTag != MaxConstants.A_FLOAT) &&
                    (groupTag != MaxConstants.A_SYM) &&
                    (groupTag != MaxConstants.A_GIMME)))
            {
                System.out.println("Bad message received");
            }
            else
            {
                for (short ii = 0;
                    (ii < inCount) &&
                        okSoFar;
                    ii++)
                {
                    byte tag = groupTag;
                    
                    if (groupTag == MaxConstants.A_GIMME)
                    {
                        tag = reader.readByte();
                    }
                    switch (tag)
                    {
                        case MaxConstants.A_LONG:
                            add(reader.readInt());
                            break;
                        
                        case MaxConstants.A_FLOAT:
                            add(Float.intBitsToFloat(reader.readInt()));
                            break;
                        
                        case MaxConstants.A_SYM:
                            {
                                short temp1 = reader.readShort();
                                
                                if (temp1 > 0)
                                {
                                    byte[] temp2 = new byte[temp1];
                                    
                                    reader.read(temp2);
                                    add(new String(temp2));
                                }
                                else
                                {
                                    System.out.println("Bad string length seen");
                                    okSoFar = false;
                                }
                            }
                            break;
                        
                        default:
                            System.out.println("Bad tag in data");
                            okSoFar = false;
                            break;
                        
                    }
                }
            }
        }
        catch (java.io.IOException ee)
        {
            System.out.println("Exception: " + ee.getMessage());
            System.out.println("Problem extracting from the byte vector of a message");
            okSoFar = false;
        }
        if (! okSoFar)
        {
            fNumBytes = 0;
            fElements = new Vector();
        }
        return okSoFar;
    } // fromByteArray
    
    public byte[] toByteArray()
    {
        try
        {
            ByteArrayOutputStream collector = new ByteArrayOutputStream();
            DataOutputStream      writer = new DataOutputStream(collector);
            byte                  tag = MaxConstants.A_GIMME;
            short                 howMany = (short) fElements.size();
        
            writer.writeShort(howMany);
            if (fIsMonotype)
            {
                if (fSawFloat)
                {
                    tag = MaxConstants.A_FLOAT;
                }
                else if (fSawInt)
                {
                    tag = MaxConstants.A_LONG;
                }
                else if (fSawSymbol)
                {
                    tag = MaxConstants.A_SYM;
                }
            }
            else
            {
                fNumBytes += howMany; // account for the tags
            }
            short sanityCheck = (short) (-(fNumBytes + howMany + 5));
            
            writer.writeShort(sanityCheck);
            writer.writeByte(tag);
            // Now we send out the good stuff        
            for (short ii = 0;
                ii < fElements.size();
                ii++)
            {
                MaxSendableValue toUse = (MaxSendableValue) fElements.elementAt(ii);
                
                toUse.convert(writer, ! fIsMonotype);
            }
            return collector.toByteArray();
        }
        catch (java.io.IOException ee)
        {
            System.out.println("Exception: " + ee.getMessage());
            System.out.println("Problem generating the byte vector for a message");
            return null;
        }
    } // toByteArray
    
    public String toString()
    {
        String accumulator = new String();
        
        for (short ii = 0;
            ii < fElements.size();
            ii++)
        {
            MaxSendableValue toUse = (MaxSendableValue) fElements.elementAt(ii);
            
            accumulator += toUse.toString() + " ";
        }
        return accumulator.trim();
    } // toString
 
}
