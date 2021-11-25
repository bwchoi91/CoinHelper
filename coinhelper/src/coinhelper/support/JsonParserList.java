package coinhelper.support;

import java.util.List;

import coinhelper.object.CandleMin;
import coinhelper.object.Coin;
import coinhelper.object.Ticker;

import com.fasterxml.jackson.core.type.TypeReference;

public class JsonParserList {
	public JsonParser marketAllParser;
	public JsonParser candleMinParser;
	public JsonParser tickerParser;
	
	public void init()
	{
		marketAllParser = new JsonParser(new TypeReference<List<Coin>>(){});
		candleMinParser = new JsonParser(new TypeReference<List<CandleMin>>(){});
		tickerParser = new JsonParser(new TypeReference<List<Ticker>>(){});
	}
	
	public Object stringToObject_markAllParser(String str)
	{
		return this.marketAllParser.stringToObject(str);
	}
	
	public Object stringToObject_candleMinParser(String str)
	{
		return this.candleMinParser.stringToObject(str);
	}

	public Object stringToObject_tickerParser(String str)
	{
		return this.tickerParser.stringToObject(str);
	}
	
}
