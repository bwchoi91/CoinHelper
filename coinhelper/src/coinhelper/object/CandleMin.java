package coinhelper.object;

import java.beans.ConstructorProperties;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class CandleMin{

	public String market;
	public String candleDateTimeUTC;
	public String candleDateTimeKST;
	public double openingPrice;
	public double highPrice;
	public double lowPrice;
	public double tradePrice;
	public long timestamp;
	public double candleAccTradePrice;
	public double candleAccTradevolume;
	public int unit;
	
	@ConstructorProperties({"market", "candle_date_time_utc", "candle_date_time_kst", "opening_price", 
		"high_price", "low_price", "trade_price", "timestamp", "candle_acc_trade_price", "candle_acc_trade_volume", "unit"})
	public CandleMin(String market, String candleDateTimeUTC, String candleDateTimeKST, double openingPrice, 
			double highPrice, double lowPrice, double tradePrice, long timestamp, double candleAccTradePrice, 
			double candleAccTradevolume, int unit) 
	{
		this.market = market;
		this.candleDateTimeUTC = candleDateTimeUTC;
		this.candleDateTimeKST = candleDateTimeKST;
		this.openingPrice = openingPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.tradePrice = tradePrice;
		this.timestamp = timestamp;
		this.candleAccTradePrice = candleAccTradePrice;
		this.candleAccTradevolume = candleAccTradevolume;
		this.unit = unit;
	}
	
}
