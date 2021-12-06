package coinhelper.object;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Coin {
	
	//Common
	public String market;
	public String korean_name;
	public String english_name;
	
	//Ticker
	public float currentTradePrice;
	public List<Float> priceList = Lists.newArrayList();
	public int queueSize;
	
	//Candle
	public List<CandleMin> candleMinList = Lists.newArrayList();
	
	//BuySell
	public boolean isBuy = false;
	public float buyPrice;
	public float sellPrice;
	
	@ConstructorProperties({"market", "korean_name", "english_name"})
	public Coin(String market, String koreanName, String englishName)
	{
		this.market = market;
		this.korean_name = koreanName;
		this.english_name = englishName;
		
		//Ticker Default
		queueSize = 20;
	}
	
	/**
	 * Ticker
	 * @param price
	 */
	public void addPriceList(float price)
	{
		priceList.add(price);
		
		if(priceList.size() > queueSize)
		{
			priceList.remove(0);
		}
	}
	
	/**
	 * Ticker
	 */
	public void clearPriceList()
	{
		priceList.clear();
	}
	
	/**
	 * CandleMin
	 * @param candleMinList
	 */
	public void addCandleMinList(List<CandleMin> candleMinList)
	{
//		if(this.candleMinList.size() > 0)
//			candleMinList.clear();
		
		this.candleMinList.addAll(candleMinList);
	}
	
}
