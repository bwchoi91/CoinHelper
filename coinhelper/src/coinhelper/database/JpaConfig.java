package coinhelper.database;

import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class JpaConfig extends OrmConfig
{
	private static final Logger log = LoggerFactory.getLogger(JpaConfig.class);
	
	@Bean 
	public LocalContainerEntityManagerFactoryBean entityManagerFactory()
	{
		String[] ormList = {"coinhelper.orm"};
		this.setOrmPackagesInfo(ormList);
		
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(this.dataSource());
		
		HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
		hibernateJpaVendorAdapter.setShowSql(true);
		entityManagerFactory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
		
		entityManagerFactory.setJpaProperties(hibernateProperties());
		entityManagerFactory.setPackagesToScan(packagesToScan);
		entityManagerFactory.setJpaDialect(new HibernateJpaDialect());
		entityManagerFactory.afterPropertiesSet();
		
		return entityManagerFactory;
	}
	
	@Bean
	public JpaTransactionManager jpaTransactionManager(EntityManagerFactory emf)
	{
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		
		return transactionManager;
	}
}
