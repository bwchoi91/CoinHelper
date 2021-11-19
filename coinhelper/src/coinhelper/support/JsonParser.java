package coinhelper.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {
	
	private TypeReference typeReference;
	private ObjectMapper objectMapper = new ObjectMapper();

	public JsonParser(TypeReference typeReference)
	{
		this.typeReference = typeReference;
	}
	
	public String objectToString(Object obj)
	{
		try
		{
			String s = this.objectMapper.writeValueAsString(obj);

			return s;

		} catch (Exception ex)
		{
			System.out.println(ex.toString());
		}

		return null;
	}
	
	public Object stringToObject(String s)
	{
		try
		{
			return this.objectMapper.readValue(s, typeReference);
		} catch (Exception ex)
		{
			System.out.println(ex.toString());
		}

		return null;
	}
	
}