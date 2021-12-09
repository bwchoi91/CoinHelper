package coinhelper.database.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

public class HibernateTransactionManagementNamingConfig implements TransactionConfigurer
{
	BeanFactory beanFactory;
	
	@Bean
	public PlatformTransactionManager transactionManager()
	{
		return (PlatformTransactionManager)beanFactory.getBean(HibernateTransactionManager.class);
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
