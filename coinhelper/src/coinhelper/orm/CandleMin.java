package coinhelper.orm;

import java.beans.ConstructorProperties;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@Entity
@Table
public class CandleMin implements Serializable{

	@Id
	@Column(nullable = false)
	public String market;
	
	@Id
	@Column(nullable = false)
	public String candleDateTimeUTC;
	
	@Column
	public String candleDateTimeKST;
	
	@Column
	public float openingPrice;
	
	@Column
	public float highPrice;
	
	@Column
	public float lowPrice;
	
	@Column
	public float tradePrice;
	
	@Column
	public long timestamp;
	
	@Column
	public float candleAccTradePrice;
	
	@Column
	public float candleAccTradevolume;
	
	@Column
	public int unit;
	
	@ConstructorProperties({"market", "candle_date_time_utc", "candle_date_time_kst", "opening_price", 
		"high_price", "low_price", "trade_price", "timestamp", "candle_acc_trade_price", "candle_acc_trade_volume", "unit"})
	public CandleMin(String market, String candleDateTimeUTC, String candleDateTimeKST, float openingPrice, 
			float highPrice, float lowPrice, float tradePrice, long timestamp, float candleAccTradePrice, 
			float candleAccTradevolume, int unit) 
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
	
	public CandleMin(String market)
	{
		this.market = market;
	}
}
