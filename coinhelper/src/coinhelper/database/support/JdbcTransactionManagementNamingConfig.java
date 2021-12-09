package coinhelper.database.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JdbcTransactionManagementNamingConfig implements TransactionConfigurer
{
	BeanFactory beanFactory;
	
	@Bean
	public PlatformTransactionManager transactionManager()
	{
		return (PlatformTransactionManager)beanFactory.getBean(DataSourceTransactionManager.class);
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
