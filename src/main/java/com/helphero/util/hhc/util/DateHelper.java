package com.helphero.util.hhc.util;

import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Convenience class to return a formatted data tmime
 * @author jcharles
 */
public abstract class DateHelper {

	public DateHelper() {
	}
	
	/**
	 * Get the formatted data time for now.
	 * @return String 
	 */
	public static String getFormattedDateTimeNow()
	{
		Date date = new Date();
		return date.toString();
	}

}
