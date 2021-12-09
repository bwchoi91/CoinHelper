package coinhelper.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ImportResource({"file:config/beanObject.xml"})
@PropertySource({"file:config/database.properties"})
public class BeanObjectConfig {

}
