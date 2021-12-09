package coinhelper.database.support;

import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;

import com.google.common.collect.Lists;

import coinhelper.database.DomainObjectPackageable;

public class DefaultOrmPackagesConfig implements OrmConfigurer, BeanFactoryAware
{
	ListableBeanFactory beanFactory;

	public String[] getOrmPackagesInfo()
	{
		Map<String, DomainObjectPackageable> domainObjets = BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, DomainObjectPackageable.class);

		List<String> allOrmPackages = Lists.newArrayList();
		for (DomainObjectPackageable packageable : domainObjets.values())
		{
			List<String> ormPackages = packageable.ormPackges();
			allOrmPackages.addAll(ormPackages);
		}
		
		return (String[])allOrmPackages.toArray(new String[allOrmPackages.size()]);
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException
	{
		if ((beanFactory instanceof ListableBeanFactory))
		{
			this.beanFactory = ((ListableBeanFactory)beanFactory);
			return;
		}

		throw new IllegalArgumentException("Cannot use SimpleOrmPackagesConfig without a ListableBeanFactory");
	}
}