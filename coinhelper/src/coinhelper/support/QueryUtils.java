package coinhelper.support;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class QueryUtils {
	public static final String INSERT_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
	public static final String DELETE_QUERY = "DELETE FROM %s WHERE %s";

	public static boolean findAnnotationField(Field field, Class<? extends Annotation> targetAnnotation)
	{
		Annotation[] annotations = field.getDeclaredAnnotations();
		for(Annotation annotation : annotations)
		{
			if(annotation.annotationType() == targetAnnotation)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static StringBuilder createSelectQueryPrimaryColumn(final Object arg) throws IllegalArgumentException, IllegalAccessException
	{
		StringBuilder sb = new StringBuilder("SELECT ");
		
		sb.append(createPrimaryKeyName(arg));
		sb.append("\nFROM ").append(arg.getClass().getSimpleName());
		sb.append("\nwhere ");
		sb.append(QueryUtils.createPrimaryKey(arg)).append("\n");
		
		return sb;
	}
	
	public static StringBuilder createPrimaryKeyName(final Object arg) throws IllegalArgumentException, IllegalAccessException
	{
		StringBuilder condition = new StringBuilder();
		
		Field[] fields = arg.getClass().getDeclaredFields();
		for(Field field : fields)
		{
			if(findAnnotationField(field, Id.class))
			{
				field.setAccessible(true);
				Object value = field.get(arg);
				
				if (value != null)
				{
					condition.append(field.getName().toUpperCase()).append(", ");
				}
			}
		}
		
		condition.delete(condition.length() - 2, condition.length());
		return condition;
	}
	
	public static StringBuilder createPrimaryKey(final Object arg) throws IllegalArgumentException, IllegalAccessException
	{
		StringBuilder condition = new StringBuilder();
		
		Field[] fields = arg.getClass().getDeclaredFields();
		for(Field field : fields)
		{
			if(findAnnotationField(field, Id.class))
			{
				field.setAccessible(true);
				Object value = field.get(arg);
				
				if (value != null)
				{
					condition.append(field.getName().toUpperCase()).append("='").append(value).append("' and ");
				}
			}
		}
		
		condition.delete(condition.length() - 5, condition.length());
		return condition;
	}
	
	public static String createInsertQuery(final Object arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		StringBuilder columnNames = new StringBuilder();
		StringBuilder columnValues = new StringBuilder();
		
		List<String> excludeColumnName = QueryUtils.findTransientColumnNames(arg);
		
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(arg.getClass());
		for (PropertyDescriptor pd : pds)
		{
			Method readMethod = pd.getReadMethod();
			if (readMethod != null)
			{
				String name = pd.getName();
				if (StringUtils.equals(name, "class"))
				{
					continue;
				}
				else if(excludeColumnName.contains(name))
				{
					continue;
				}
					
				Object value = readMethod.invoke(arg);
				if (value != null)
				{
					if(value instanceof Boolean)
					{
						value = Boolean.parseBoolean(value.toString()) ? "1" : "0";
					}
					else if (value instanceof Date)
					{
						value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date) value);
					}
						
//					columnValues.insert(columnValues.length(), String.format("'%s', ", value));
					columnValues.insert(columnValues.length(), "'" + value + "', ");
						
//					columnNames.insert(columnNames.length(), String.format("%s, ", name.toUpperCase()));
					columnNames.insert(columnNames.length(), name.toUpperCase() + ", ");
				}
			}
		}
		
		columnNames.delete(columnNames.length() - 2, columnNames.length() -1);
		columnValues.delete(columnValues.length() - 2, columnValues.length() -1);
		
//		String test = String.format(INSERT_QUERY, arg.getClass().getSimpleName().toUpperCase(), columnNames, columnValues);
		
		return String.format(INSERT_QUERY, arg.getClass().getSimpleName().toUpperCase(), columnNames, columnValues);
	}
	
	public static String createUpdateQuery(Object arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ");
		query.append(arg.getClass().getSimpleName());
		query.append(" SET ");
		
		List<String> excludeColumnName = QueryUtils.findTransientColumnNames(arg);
		
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(arg.getClass());
		for (PropertyDescriptor pd : pds)
		{
			Method readMethod = pd.getReadMethod();
			if (readMethod != null)
			{
				String name = pd.getName();
				if (StringUtils.equals(name, "class"))
				{
					continue;
				}
				else if(excludeColumnName.contains(name))
				{
					continue;
				}
					
				Object value = readMethod.invoke(arg);
				if(value == null)
				{
					query.append(name).append("=null, ");
				}
				else
				{
					if(value instanceof Boolean)
					{
						value = Boolean.parseBoolean(value.toString()) ? "1" : "0";
					}
					else if(value instanceof Date)
					{
						value = Calendar.getInstance().getTime();
					}
					
					query.append(name).append("='").append(value).append("', ");
				}
			}
		}
		
		query.delete(query.length() - 2, query.length()); // 마지막 ", " 지우기
		
		query.append(" WHERE ");
		query.append(QueryUtils.createPrimaryKey(arg));
		
		return query.toString();
	}
	
	private static List<String> findTransientColumnNames (final Object arg)
	{
		
		List<String> list = Lists.newArrayList();
		List<String> tempList = Lists.newArrayList();
		
		// 1
		tempList = QueryUtils.findTransientColumnNames(arg.getClass());
		if(tempList == null)
		{
			return list;
		}
		
		list.addAll(tempList);
		
		// 2
		tempList = QueryUtils.findTransientColumnNames(arg.getClass().getSuperclass());
		if(tempList == null)
		{
			return list;
		}
		
		list.addAll(tempList);
		
		// 3
		tempList = QueryUtils.findTransientColumnNames(arg.getClass().getSuperclass().getSuperclass());
		if(tempList == null)
		{
			return list;
		}
		
		list.addAll(tempList);
		
		// 4
		tempList = QueryUtils.findTransientColumnNames(arg.getClass().getSuperclass().getSuperclass().getSuperclass());
		if(tempList == null)
		{
			return list;
		}
		
		list.addAll(tempList);
		
		// 5
		tempList = QueryUtils.findTransientColumnNames(arg.getClass().getSuperclass().getSuperclass().getSuperclass().getSuperclass());
		if(tempList == null)
		{
			return list;
		}
		
		list.addAll(tempList);
		
		// 6
		tempList = QueryUtils.findTransientColumnNames(arg.getClass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getSuperclass());
		if(tempList == null)
		{
			return list;
		}
		
		list.addAll(tempList);
		
		// 7
		tempList = QueryUtils.findTransientColumnNames(arg.getClass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getSuperclass());
		if(tempList == null)
		{
			return list;
		}
		
		list.addAll(tempList);
		
		return list;
	}
	
	private static List<String> findTransientColumnNames(Class<? extends Object> obj)
	{
		List<String> list = Lists.newArrayList();
		
		if(obj == null)
		{
			return null;
		}
		
		for(Field field : obj.getDeclaredFields())
		{
			if(findAnnotationField(field, Transient.class))
			{
				list.add(field.getName());
			}
		}
		
		return list;
	}
	
	public static Map<String, String> createPrimaryKeyForMap(final Object arg) throws IllegalArgumentException, IllegalAccessException
	{
		Map<String, String> map = Maps.newConcurrentMap();
		
		Field[] fields = arg.getClass().getDeclaredFields();
		for(Field field : fields)
		{
			if(findAnnotationField(field, Id.class))
			{
				field.setAccessible(true);
				Object value = field.get(arg);
				
				if (value != null)
				{
//					condition.append(field.getName().toUpperCase()).append("='").append(value).append("' and ");
					map.put(field.getName().toUpperCase(), value.toString());
				}
			}
		}
		
		return map;
	}
}
