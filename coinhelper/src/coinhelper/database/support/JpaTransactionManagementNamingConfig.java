package coinhelper.database.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

public class JpaTransactionManagementNamingConfig implements TransactionConfigurer{
	BeanFactory beanFactory;
	
	@Bean
	public PlatformTransactionManager transactionManager()
	{
		return (PlatformTransactionManager)beanFactory.getBean(JpaTransactionManager.class);
	}
	
	public PlatformTransactionManager annotationDrivenTransactionManager()
	{
		return transactionManager();
	}
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException
	{
		this.beanFactory = beanFactory;
	}
}
