package coinhelper.support;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import coinhelper.service.DataService;

public class CoinHelperUtils {
	
	public static final String SIMPLE_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	private static Logger log = LogManager.getLogger(CoinHelperUtils.class);
	
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
	
	public static Document makeDocumentFilePath(String filePath)
	{
		try
		{
			SAXBuilder builder = new SAXBuilder();
			File file = new File(filePath);
			
			if(file != null && file.exists() == true)
			{
				return builder.build(file);
			}
		}
		
		catch(Exception e)
		{
			log.error(e.getMessage(), e);
		}
		
		return null;
	}
}
