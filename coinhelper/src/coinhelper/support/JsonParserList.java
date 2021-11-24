package coinhelper.support;

import java.util.List;

import coinhelper.object.CandleMin;
import coinhelper.object.Coin;

import com.fasterxml.jackson.core.type.TypeReference;

public class JsonParserList {
	public JsonParser marketAllParser;
	public JsonParser candleMinParser;
	
	public void init()
	{
		marketAllParser = new JsonParser(new TypeReference<List<Coin>>(){});
		candleMinParser = new JsonParser(new TypeReference<List<CandleMin>>(){});
	}
	
	public Object stringToObject_markAllParser(String str)
	{
		return this.marketAllParser.stringToObject(str);
	}
	
	public Object stringToObject_candleMinParser(String str)
	{
		return this.candleMinParser.stringToObject(str);
	}
	
	
}
