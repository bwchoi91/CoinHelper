package coinhelper.support;

public class ServiceUrlList {
	public static final String SERVICE_URL = "https://api.upbit.com";
	
	public static final String GET_MARKET_ALL = "/v1/market/all";
	public static final String GET_CANDLE_MIN = "/v1/candles/minutes";
	
	
	public static String getURL_MarketAll()
	{
		return String.format("%s%s", SERVICE_URL, GET_MARKET_ALL);
	}
	
	public static String getURL_CANDLE_MIN(int unit, String market, int count)
	{
		return String.format("%s%s/%s?market=%s&count=%s", SERVICE_URL, GET_CANDLE_MIN, unit, market, count);
	}
}
