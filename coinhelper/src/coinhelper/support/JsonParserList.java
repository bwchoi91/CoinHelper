package coinhelper.support;

import java.util.List;

import coinhelper.object.Coin;

import com.fasterxml.jackson.core.type.TypeReference;

public class JsonParserList {
	public JsonParser marketAllParser;
	
	public void init()
	{
		marketAllParser = new JsonParser(new TypeReference<List<Coin>>(){});
	}
	
	public Object stringToObject_markAllParser(String str)
	{
		return this.marketAllParser.stringToObject(str);
	}
}
