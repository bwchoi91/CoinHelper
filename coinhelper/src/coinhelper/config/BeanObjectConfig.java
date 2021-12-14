package coinhelper.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import coinhelper.database.EnableOrm;
import coinhelper.database.JpaConfig;

@Configuration
@ImportResource({"file:config/beanObject.xml"})
@PropertySource({"file:config/database.properties"})
@EnableOrm
@Import({JpaConfig.class, ManagerConfig.class})
public class BeanObjectConfig {

}
