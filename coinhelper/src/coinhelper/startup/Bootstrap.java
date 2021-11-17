package coinhelper.startup;

import java.awt.EventQueue;

import javax.swing.UIManager;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import coinhelper.config.BeanObjectConfig;

public class Bootstrap 
{
	
	public static void main(String[] args) 
	{
		try 
		{ 
			String jTattoo1 = "com.jtattoo.plaf.smart.SmartLookAndFeel";
			String jTattoo2 = "com.jtattoo.plaf.mcwin.McWinLookAndFeel";
			
			UIManager.setLookAndFeel(jTattoo1); 
		} catch (Exception e) 
		{ 
			
		} 
		
		ApplicationContext context = new AnnotationConfigApplicationContext(BeanObjectConfig.class);
		
//		FunctionService.get().getAccount("f41xbtoxO9miB7kXkzZD71Oss3ohrZgm5L1lU3eP", "ucLrePikgeXT7y0bqHEYylxNeg7tA0ilEkq7xxVu");

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
