package coinhelper.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;

import coinhelper.database.DomainObjectPackageable;
import coinhelper.database.JpaConfig;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSource {
	
	public static DataSource dataSource;
	
	private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;

	@Autowired
	public JpaConfig jpaConfig;
	
	/*static
	{
		String driver = "org.firebirdsql.jdbc.FBDriver";
		String url = "jdbc:firebirdsql:localhost/3050:D:\\BlueOS\\DB\\CIMPC.FDB?charSet=UTF8";
		String user = "SYSDBA";
		String pwd = "masterkey";
		
		config.setJdbcUrl(url);
		config.setUsername(user);
		config.setPassword(pwd);
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("preStmCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		ds = new HikariDataSource(config);
		
	}*/
	
	public DataSource()
	{
		dataSource = this;
	}
	
	public static DataSource get()
	{
		return dataSource;
	}
	
/*	public static Connection getConnection() throws SQLException
	{
		return ds.getConnection();
	}*/

	public void connect() throws SQLException
	{
		
		jpaConfig.entityManagerFactory();
		
//		getConnection();
		
/*		Connection con = null;
		String driver = "org.firebirdsql.jdbc.FBDriver";
		String url = "jdbc:firebirdsql:localhost/3050:D:\\BlueOS\\DB\\CIMPC.FDB?charSet=UTF8";
		String user = "SYSDBA";
		String pwd = "masterkey";

		
		try{
		Class.forName(driver);
		con = DriverManager.getConnection(url, user, pwd);
		System.out.println("DB 연결완료");
		} catch (Exception e) {
		e.printStackTrace();
		}*/
		
	}
}
