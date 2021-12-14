package coinhelper.manager;


import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import coinhelper.orm.CandleMin;

public class CandleMinManager extends AbstractManager
{
	private Logger log = LogManager.getLogger(CandleMinManager.class);
	
	private BeanPropertyRowMapper<CandleMin> candleMinRowMapper = new BeanPropertyRowMapper<CandleMin>(CandleMin.class);
	
	public void save(CandleMin candleMin) throws DataAccessException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		super.insert(candleMin, candleMinRowMapper);
	}
	
	public void save(List<CandleMin> candleMinList) throws Exception
	{
		super.insert(candleMinList, candleMinRowMapper);
	}
	
}
