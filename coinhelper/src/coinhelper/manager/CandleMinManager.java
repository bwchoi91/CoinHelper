package coinhelper.manager;


import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import coinhelper.config.BeanObjectConfig;
import coinhelper.orm.CandleMin;

public class CandleMinManager extends AbstractManager<CandleMin>
{
	private Logger log = LogManager.getLogger(CandleMinManager.class);
	
	@Autowired
    private ApplicationContext context;

	private RowMapper<CandleMin> candleMinRowMapper = new BeanPropertyRowMapper<CandleMin>(CandleMin.class);
	
	public CandleMinManager()
	{

	}
	
	public void save(CandleMin candleMin) throws DataAccessException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
//		super.insert(candleMin, candleMinRowMapper);
	}
	
	public void save(List<CandleMin> candleMinList) throws Exception
	{
		//test
		try
		{
			super.insert(candleMinList, candleMinRowMapper);
		}
		catch(Exception e)
		{
			log.error(e);
		}
	}
	
}
