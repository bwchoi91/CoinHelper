package coinhelper.support;

public class ServiceUrlList {
	public static final String SERVICE_URL = "https://api.upbit.com";
	
	public static final String GET_MARKET_ALL = "/v1/market/all";
	public static final String GET_CANDLE_MIN = "/v1/candles/minutes";
	public static final String GET_TICKER = "/v1/ticker";
	
	
	public static String getURL_MarketAll()
	{
		return String.format("%s%s", SERVICE_URL, GET_MARKET_ALL);
	}
	
	public static String getURL_CandleMin(int unit, String market, int count)
	{
		return String.format("%s%s/%s?market=%s&count=%s", SERVICE_URL, GET_CANDLE_MIN, unit, market, count);
	}

	public static String getURL_ticker(StringBuilder market)
	{
		return String.format("%s%s?markets=%s", SERVICE_URL, GET_TICKER, market);
	}
}
