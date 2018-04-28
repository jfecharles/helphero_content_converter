package com.helphero.util.hhc.dom.processing;

/**
 * Convenience class for throwing TRansform exceptions.
 * @author jcharles
 */
public class TransformException extends Exception
{

    private static final long serialVersionUID = 1997753363232807999L;

    public TransformException()
    {
    }
 
    public TransformException(String message)
    {
    	super(message);
    }
 
    public TransformException(Throwable cause)
    {
    	super(cause);
    }
 
    public TransformException(String message, Throwable cause)
    {
    	super(message, cause);
    }
 
    public TransformException(String message, Throwable cause,
                                           boolean enableSuppression, boolean writableStackTrace)
    {
    	super(message, cause, enableSuppression, writableStackTrace);
    }
}
