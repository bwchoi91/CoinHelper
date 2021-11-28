package coinhelper.log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
	static public Long convertFileSize(String fileSize) {
		Long changedSize = 0L;
		if(fileSize.toUpperCase().contains("KB")) {
			fileSize = fileSize.toUpperCase().replaceAll("KB", "");
			changedSize = Long.parseLong(fileSize) * 1024L;
		}
		
		else if(fileSize.toUpperCase().contains("MB")) {
			fileSize = fileSize.toUpperCase().replace("MB", "");
			changedSize = Long.parseLong(fileSize) * 1024L * 1024L;
		}
		
		else if(fileSize.toUpperCase().contains("GB")) {
			fileSize = fileSize.toUpperCase().replaceAll("GB", "");
			changedSize = Long.parseLong(fileSize) * 1024L * 1024L * 1024L;
		}
		else {
			changedSize = Long.parseLong(fileSize);
		}
		return changedSize;
	}
	
	static public String getCurrentTime(String datePattern) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat fm = new SimpleDateFormat(datePattern);
		String time = fm.format(cal.getTime());
		return time;
	}
}
