package coinhelper.database;

import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import coinhelper.database.support.DefaultOrmPackagesConfig;
import coinhelper.database.support.HibernateTransactionManagementNamingConfig;
import coinhelper.database.support.JdbcTransactionManagementNamingConfig;
import coinhelper.database.support.JpaTransactionManagementNamingConfig;

public class OrmConfigurationSelector implements ImportSelector{

	public String[] selectImports(AnnotationMetadata importingClassMetadata)
	{
		Map<String, Object> metadata = importingClassMetadata.getAnnotationAttributes(EnableOrm.class.getName());
		
		OrmType ormType = (OrmType)metadata.get("ormType");
		Boolean isTrxNaming = (Boolean)metadata.get("trxManagerNaming");
		
		if(ormType == OrmType.Hibernate)
			return hibernateConfiguration(isTrxNaming);
		
		if(ormType == OrmType.Jpa)
			return jpaConfiguration(isTrxNaming);
		
		return jdbcConfiguration(isTrxNaming);
	}
	
	protected String[] jdbcConfiguration(Boolean isTrxNaming)
	{
		String[] configs = {JdbcConfig.class.getName()};
		
		if(isTrxNaming.booleanValue())
		{
			configs = (String[])ArrayUtils.add(configs, JdbcTransactionManagementNamingConfig.class.getName());
		}
		
		return configs;
	}
	

	protected String[] jpaConfiguration(Boolean isTrxNaming)
	{
		String[] configs = {DefaultOrmPackagesConfig.class.getName(), JpaConfig.class.getName()};
		
		if(isTrxNaming.booleanValue())
		{
			configs = (String[])ArrayUtils.add(configs, JpaTransactionManagementNamingConfig.class.getName());
		}
		
		return configs;
	}
	
	protected String[] hibernateConfiguration(Boolean isTrxNaming)
	{
		String[] configs = {DefaultOrmPackagesConfig.class.getName(), HibernateConfig.class.getName()};
		
		if(isTrxNaming.booleanValue())
		{
			configs = (String[])ArrayUtils.add(configs, HibernateTransactionManagementNamingConfig.class.getName());
		}
		
		return configs;
	}
}
