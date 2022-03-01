package coinhelper.database;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class HibernateConfig extends OrmConfig
{
	private static final Logger log = LoggerFactory.getLogger(HibernateConfig.class);
	
	@Bean
	public LocalSessionFactoryBean sessionFactory()
	{
		if((packagesToScan == null) || (packagesToScan.length == 0))
		{
			log.info("you define hibernate. but you do not define Entity pojo path. so you will do not hibernate orm");
		}
		
		LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
		sessionFactory.setDataSource(this.dataSource());
		sessionFactory.setPackagesToScan(packagesToScan);
		sessionFactory.setHibernateProperties(this.hibernateProperties());
		
		return sessionFactory;
	}
	
	@Bean
	public HibernateTransactionManager hibernateTransactionManager(SessionFactory sessionFactory)
	{
		HibernateTransactionManager transactionManager = new HibernateTransactionManager();
		transactionManager.setSessionFactory(sessionFactory);
		
		return transactionManager;
	}
}
