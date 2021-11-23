package coinhelper.log;

public interface LogListener {
	public void appendedLog(String loggerName, String msg);
}