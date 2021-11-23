package coinhelper.support;

public class ServiceUrlList {
	public static final String SERVICE_URL = "https://api.upbit.com";
	
	public static final String GET_ACCOUNT = "/v1/accounts";
	public static final String GET_MARKET_ALL = "/v1/market/all";
	public static final String GET_COIN_INFO = "/v1/ticker/?markets=";
	public static final String GET_ORDER = "/v1/orders";
	public static final String GET_CHECK_ORDER = "/v1/order";
	
	public static String getURL_MarketAll()
	{
		return String.format("%s%s", SERVICE_URL, GET_MARKET_ALL);
	}
}
