package coinhelper.support;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
	public static final String SIMPLE_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	public static Date getCurrentTime()
	{
		return Calendar.getInstance().getTime();
	}
	
	public static Calendar getCalendar()
	{
		return Calendar.getInstance();
	}
	
	public static String getCurrentTimeToString()
	{
		return new SimpleDateFormat(SIMPLE_DATETIME_FORMAT).format(getCurrentTime());
	}
	
	public static String getTimeToString(Date time)
	{
		return new SimpleDateFormat(SIMPLE_DATETIME_FORMAT).format(time);
	}
}
