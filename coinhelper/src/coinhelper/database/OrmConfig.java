package coinhelper.database;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.util.Assert;

@Configuration
public class OrmConfig extends DataSourceConfig
{
	private static final Logger log = LoggerFactory.getLogger(OrmConfig.class);
	
	protected String[] packagesToScan;
	
	public void setOrmPackagesInfo(String[] ormList)
	{
		if(ormList == null)
		{
			log.info("ormList is null. you do not use orm. you can use jdbc.");
			return;
		}
		
		packagesToScan = ormList;
	}
	
	@Bean
	public HibernateExceptionTranslator hibernateExceptionTranslator()
	{
		return new HibernateExceptionTranslator();
	}
	
	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation()
	{
		return new PersistenceExceptionTranslationPostProcessor();
	}
	
	protected Properties hibernateProperties()
	{
		Assert.notNull(environment.getProperty("hibernate.dialect"), "you must define 'hibernate.dialect' property. you check database-yourDatabase.properties file");

		Properties properties = new Properties();
		properties.put("hibernate.dialect", environment.getProperty("hibernate.dialect"));
		properties.put("hibernate.hbm2ddl.auto", environment.getProperty("hibernate.hbm2ddl.auto", "validate"));
		properties.put("hibernate.connection.autocommit", environment.getProperty("hibernate.connection.autocommit", "false"));
		properties.put("hibernate.show_sql", environment.getProperty("hibernate.show_sql", "true"));
		properties.put("hibernate.format_sql", environment.getProperty("hibernate.format_sql", "true"));
		properties.put("hibernate.generate_statistics", environment.getProperty("hibernate.generate_statistics", "true"));
		
		if (environment.getProperty("hibernate.default_schema") == null)
		{
			log.info("hibernate.default_schema property is null. but ok.");
		}
		else
		{
			properties.put("hibernate.default_schema", environment.getProperty("hibernate.default_schema"));
		}
		
		properties.put("hibernate.cache.use_query_cache", Boolean.valueOf(false));
		properties.put("hibernate.cache.use_second_level_cache", Boolean.valueOf(false));
		properties.put("hibernate.query.startup_check", Boolean.valueOf(false));
		return properties;
	}
}
