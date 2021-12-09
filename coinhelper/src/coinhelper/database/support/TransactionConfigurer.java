package coinhelper.database.support;

import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

public abstract interface TransactionConfigurer extends TransactionManagementConfigurer, BeanFactoryAware
{
	
}
