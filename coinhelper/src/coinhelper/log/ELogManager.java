package coinhelper.log;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

public class ELogManager {
	private static final Logger logger = LogManager.getLogger(ELogManager.class);
//	private static Vector vLogListener = new Vector();
	private static List<LogListener> logListener = Lists.newArrayList();
	
//	public ELogManager() {
//	}

//	public static void configuration(String envFile) {
//		PropertyConfigurator.configure(envFile);
//	}

//	public static EASLogger getLogger(Class clss) {
//		return Logger.getLogger(clss);
//	}

//	public static EASLogger getLogger(Object obj) {
//		return Logger.getLogger(obj.getClass());
//	}

//	private void logSample() {
//		logger.fatal("Fatal");
//		logger.error("Error");
//		logger.warn("Warn");
//		logger.debug("Debug");
//		logger.info("Info");
//	}

	public static synchronized void addLogListener(LogListener listener) {
//		vLogListener.addElement(listener);
		logListener.add(listener);
	}

	public static synchronized void removeLogListener(LogListener listener) {
//		vLogListener.removeElement(listener);
		logListener.remove(listener);
	}

	public static synchronized void removeLogListenerAll() {
//		vLogListener.removeAllElements();
		logListener.clear();
	}

	protected static synchronized void appendedLog(String loggerName, String msg) {
//		for (int i = 0; i < vLogListener.size(); i++) {
//			LogListener lst = (LogListener) vLogListener.get(i);
//			if (lst != null) {
//				lst.appendedLog(loggerName, msg);
//			}
//		}
		
		for(LogListener listener : logListener)
		{
			listener.appendedLog(loggerName, msg);
		}
		
	}
}