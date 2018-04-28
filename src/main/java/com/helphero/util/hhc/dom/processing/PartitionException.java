package com.helphero.util.hhc.dom.processing;

/**
 * Convenience class for throwing partition level exceptions.
 * @author jcharles
 */
public class PartitionException extends Exception
{
    private static final long serialVersionUID = 1997753363232807900L;

    public PartitionException()
    {
    }
 
    public PartitionException(String message)
    {
    	super(message);
    }
 
    public PartitionException(Throwable cause)
    {
    	super(cause);
    }
 
    public PartitionException(String message, Throwable cause)
    {
    	super(message, cause);
    }
 
    public PartitionException(String message, Throwable cause,
                                           boolean enableSuppression, boolean writableStackTrace)
    {
    	super(message, cause, enableSuppression, writableStackTrace);
    }
}
