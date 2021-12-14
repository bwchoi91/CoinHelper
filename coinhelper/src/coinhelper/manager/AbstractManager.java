package coinhelper.manager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import coinhelper.support.QueryUtils;

public class AbstractManager<T> {
	
	private Logger log = LogManager.getLogger(this.getClass());
	
	@Autowired
	public JdbcTemplate jdbcTemplate;
	
	protected void insert(T t, BeanPropertyRowMapper<T> rowMapper) throws DataAccessException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		if(this.isNotExistRow(t, rowMapper))
		{
			String sql = QueryUtils.createInsertQuery(t);
			this.jdbcTemplate.update(sql);
		}
		else
		{
			log.debug(String.format("Exist Row TableName=%s, PrimaryKey=[%s]", t.getClass().getSimpleName(), QueryUtils.createPrimaryKey(t)));
			this.jdbcTemplate.update(QueryUtils.createUpdateQuery(t));
		}
	}
	protected void insert(List<T> list, BeanPropertyRowMapper<T> rowMapper) throws Exception 
	{
		try
		{
			if(list.isEmpty())
			{
				log.debug("empty list");
				return;
			}
			
			List<T> existRowList = this.existRowList(list, rowMapper);
			if(existRowList == null || existRowList.isEmpty())
			{
				this.batchInsert(list);
			}
		}
		catch(Exception e)
		{
			log.error(e);
		}
	}

	private void batchInsert(List<T> list) throws Exception
	{
		int indexer = 0;
		String[] sqls = new String[list.size()];
		StringBuilder sb = new StringBuilder(String.format("Insert %s Size=%s\n", list.get(0).getClass().getSimpleName(), list.size()));
			
		for(Object obj : list)
		{
			sqls[indexer] = QueryUtils.createInsertQuery(obj);
			sb.append(sqls[indexer++]).append("\n");
		}
			
		this.batchUpdate(sqls);
		log.debug(sb);
	}
	
	private void batchUpdate(String[] arguments) throws Exception
	{
		try
		{
//			this.transactionManager.begin();
			
			this.jdbcTemplate.batchUpdate(arguments);
			
//			this.transactionManager.commit();
		}
		catch (Exception e)
		{
//			this.transactionManager.rollback();
			throw e;
		}
	}
	
	protected boolean isNotExistRow(T t, BeanPropertyRowMapper<T> rowMapper) throws IllegalArgumentException, IllegalAccessException
	{
		StringBuilder sb = QueryUtils.createSelectQueryPrimaryColumn(t);
		List<T> list = this.jdbcTemplate.query(sb.toString(), rowMapper);
		if(list.isEmpty())
		{
			return true;
		}
		
		return false;
	}
	
	protected List<T> existRowList(List<T> list, BeanPropertyRowMapper<T> rowMapper) throws IllegalArgumentException, IllegalAccessException
	{
		if(list.isEmpty())
		{
			log.debug("empty list");
			return null;
		}
		
		Object obj = list.get(0);
		
		StringBuilder sb = new StringBuilder("SELECT ");
		
		sb.append(QueryUtils.createPrimaryKeyName(obj));
		sb.append(" FROM ").append(obj.getClass().getSimpleName());
		sb.append(" where");
		
		Map<String, List<String>> primaryKeyMap = Maps.newConcurrentMap();
		
		for(T t : list)
		{
			Map<String, String> map = QueryUtils.createPrimaryKeyForMap(t);
			if(map.isEmpty())
			{
				continue;
			}
			
			for(Entry<String, String> entrySet : map.entrySet())
			{
				List<String> valueList = primaryKeyMap.get(entrySet.getKey());
				if(valueList == null)
				{
					valueList = Lists.newArrayList();
				}
				
				if(valueList.contains(entrySet.getValue()))
				{
					continue;
				}
				
				valueList.add(entrySet.getValue());
				primaryKeyMap.put(entrySet.getKey(), valueList);
			}
		}
		
		for(Entry<String, List<String>> entrySet : primaryKeyMap.entrySet())
		{
			if(entrySet.getValue().isEmpty())
				continue;
			
			sb.append(" ");
			sb.append(entrySet.getKey()).append(" in (");
			
			for(String value : entrySet.getValue())
			{
				sb.append("'");
				sb.append(value);
				sb.append("',");
			}
			
			sb.delete(sb.length() - 1, sb.length());
			sb.append(")");
			sb.append(" and");
		}
		
		sb.delete(sb.length() - 4, sb.length());

		log.info(sb);
		return this.jdbcTemplate.query(sb.toString(), rowMapper);
	}

}
