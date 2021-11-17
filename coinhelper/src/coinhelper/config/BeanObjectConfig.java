package coinhelper.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"file:config/beanObject.xml"})
public class BeanObjectConfig {

}
