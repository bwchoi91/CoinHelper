package coinhelper.log;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;

import javax.swing.Timer;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;


public class UserFileAppender extends AppenderSkeleton implements Appender{
	
	// 150328. FileSize에 따라서 Index가 추가 되는 기능 추가 by jbkong
	protected long count = 0;
	protected long maxFileSize = 0;	
	protected int currnetFileIndex = 0;
	protected boolean iniFlag = false;
	protected String lastDate = "";
	protected FileOutputStream out;
	protected FileChannel outChannel;

	protected String filePattern;
	protected String datePattern;
	protected boolean enableEvent;
	private String lastLogedFileName = "";
	protected int deleteHour = -1;
	protected int recordingDays;
	protected int deleteCycleDays;

	private Timer hourTimer;
	private Timer dayTimer;
	
	public void setMaxFileSize(String maxFileSize){
		try {
			this.maxFileSize = Utils.convertFileSize(maxFileSize);
		}
		catch (NumberFormatException ex) {
			this.maxFileSize = -1;
		}
	}
	
	public void setDeleteHour(String hour){
		try {
			deleteHour = Integer.parseInt(hour);
		}
		catch (NumberFormatException ex) {
			deleteHour = -1;
		}
	}

	public void setDeleteCycleDays(String days){
		try {
			deleteCycleDays = Integer.parseInt(days);
			int msec = 3600000 * 24 * deleteCycleDays;
			dayTimer.setDelay(msec);
		}
		catch (NumberFormatException ex) {
			deleteHour = -1;
		}
	}

	public void setRecordingDays(String day){
		try {
			recordingDays = Integer.parseInt(day);
		}
		catch (NumberFormatException ex) {
			deleteHour = -1;
		}
	}

	public void setFilePattern(String pattern){
		this.filePattern = pattern;
	}

	public void setDatePattern(String pattern){
		this.datePattern = pattern;
	}
	
	public UserFileAppender() {
		hourTimer = new Timer(3600000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(isDeleteHour()){
					deleteLogs();
				}
			}
		});
		dayTimer = new Timer(3600000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(deleteCycleDays > 0){
					deleteLogs();
				}
			}
		});
		hourTimer.start();
		dayTimer.start();
	}

	private boolean isDeleteHour(){
		Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.HOUR_OF_DAY) == deleteHour){
			return true;
		}else{
			return false;
		}
	}
	
	private void deleteLogs(){
		if(recordingDays < 1){
			return;
		}
		long day = System.currentTimeMillis() - (3600000l * 24l * (long)recordingDays);
		
		String pattern = createPattern(".*");
		
		File[] files = getFile().getParentFile().listFiles();
		for(File file : files){
			if(file.lastModified() <= day){
				if(file.getAbsolutePath().matches(pattern)){
					file.delete();
				}
			}
		}
	}
	
	private int getLastFileNumber(){
		int lastNumber = 0;
		int tempNumber = 0;
		String lastFileName = null;
		
		String pattern = createPattern(Utils.getCurrentTime(datePattern) + ".*");
		
		File[] files = getFile().getParentFile().listFiles();
		for(File file : files){
			if(file.getAbsolutePath().matches(pattern)){
				lastFileName = file.getName();
				if(lastFileName != null) {
					tempNumber = Integer.parseInt(lastFileName.substring(lastFileName.lastIndexOf("_") + 1, lastFileName.lastIndexOf(".")));
					if(lastNumber < tempNumber) {
						lastNumber = tempNumber;
					}
				}
			}
		}
		iniFlag = true;
		
		return lastNumber + 1;
	}
	
	private String getFilePath(){
		
		if(filePattern == null){
			return null;
		}
		String time = Utils.getCurrentTime(datePattern);
		
		if(lastDate.equals(time) == false) {
			currnetFileIndex = 1;
			lastDate = Utils.getCurrentTime(datePattern);
		}
		
		time = time + "_" + currnetFileIndex;
		String fname = filePattern.replaceAll("%%","\u0626");
		fname = fname.replaceAll("%d",time);
		fname = fname.replaceAll("\u0626","%");
		return fname;
	}
	
	private File getFile(){
		String fname = getFilePath();
		File file = new File(fname);
		if(!file.exists()){
			file.getParentFile().mkdirs();
		}
		return file;
	}

	private String createPattern(String str) {
		String pattern = null;
		pattern = (new File(filePattern)).getAbsolutePath();
		pattern = pattern.replaceAll("\\\\",File.separator+File.separator+File.separator+File.separator);
		pattern = pattern.replaceAll("\\.","\\\\.").replaceAll("%d", str);
		return pattern;
	}
	
	@Override
	public void append(LoggingEvent loggingEvent){
		
		if (iniFlag == false) {
			currnetFileIndex = getLastFileNumber();
		}
		
		String msg = createMsg(loggingEvent);
		ELogManager.appendedLog(getName(), msg);
		
		if(countFileSize(msg) == true)
		{
			writeLog(msg);
		}
	}
	
	// Log Message 생성
	private String createMsg(LoggingEvent loggingEvent) {
		ThrowableInformation thw = loggingEvent.getThrowableInformation();
		String err = "";
		if (thw != null) {
			err += thw.getThrowable();
			StringWriter writer = new StringWriter();
			PrintWriter print = new PrintWriter(writer);
			thw.getThrowable().printStackTrace(print);
			err += writer.toString();
		}
		
		return getLayout().format(loggingEvent) + err;
	}

	// Create Index : 용량으로 파일 분할하기 위하여 추가.
	private Boolean countFileSize(String msg)
	{
		try {
			if(maxFileSize > 0)
			{
				this.count += msg.length();
				if(count > maxFileSize) {
					count = msg.length();
					currnetFileIndex += 1;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private void writeLog(String msg) {
		try {
 			String filepath = getFilePath();
			byte[] array = msg.getBytes("UTF-8");
			ByteBuffer buf = ByteBuffer.wrap(array);
			
			if(filepath == null){
				return;
			}
			
			if(!lastLogedFileName.equals(filepath)){
				if(out != null){
					outChannel.close();
					out.close();
				}
				
				try {
					out = new FileOutputStream(getFile(), true); // 이어쓰기를 허용한다.
					outChannel = out.getChannel();
				} catch (Exception e) {
					e.printStackTrace();
				}
				lastLogedFileName = filepath;
			}
			buf.put(array);
			buf.flip(); // buffer를 처음 위치로 되돌린다. buf.limit(buf.position()).position(0);
			
			outChannel.write(buf);
			buf.clear(); // buffer를 처음 생성했을 때 상태로 되돌린다. 
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public boolean requiresLayout() {
		return true;
	}

	@Override
	public void close() {
	}
}