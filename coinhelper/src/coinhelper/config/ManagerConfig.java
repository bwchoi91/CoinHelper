package coinhelper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import coinhelper.manager.CandleMinManager;

@Configuration
public class ManagerConfig {

	@Bean
	public CandleMinManager candleMinManager()
	{
		return new CandleMinManager();
	}
}
