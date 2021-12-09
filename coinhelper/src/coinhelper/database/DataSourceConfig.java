package coinhelper.database;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mchange.lang.IntegerUtils;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig{

	public final String USER = "jdbc.user";
	public final String PASSWORD = "jdbc.password";
	public final String JDBCURL = "jdbc.driverClass";
	
	public final String MIN_POOL_SIZE = "jdbc.minPoolSize";
	public final String MAX_POOL_SIZE = "jdbc.maxPoolSize";
	public final String IDLE_CONNECTION_TEST_PERIOD_IN_MINUTES = "jdbc.idleConnectionTestPeriodInMinutes";
	public final String IDLE_MAX_AGE_IN_MINUTES = "jdbc.idleMaxAgeInMinutes";
	public final String INITIAL_POOL_SIZE = "jdbc.initialPoolSize";
	
	@Autowired
	public Environment environment;
	
	@Bean(destroyMethod="close")
	public DataSource dataSource()
	{
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(environment.getProperty("jdbc.url"));
		dataSource.setDriverClassName(environment.getProperty("jdbc.driverClass"));
		dataSource.setUsername(environment.getProperty("jdbc.user"));
		dataSource.setPassword(environment.getProperty("jdbc.password"));
		
		dataSource.setMaximumPoolSize(IntegerUtils.parseInt(environment.getProperty("jdbc.maxPoolSize"), 10));
		
		return dataSource;
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate()
	{
		return new JdbcTemplate(dataSource());
	}
}
