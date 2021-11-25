package coinhelper.object;

import java.beans.ConstructorProperties;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
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
	public Queue<Float> priceQueue;
	public int queueSize;
	
	@ConstructorProperties({"market", "korean_name", "english_name"})
	public Coin(String market, String koreanName, String englishName)
	{
		this.market = market;
		this.korean_name = koreanName;
		this.english_name = englishName;
		
		//Default
		priceQueue = Queues.newConcurrentLinkedQueue();
		queueSize = 20;
	}
	
	public void pushPrice(float price)
	{
		priceQueue.add(price);
		
		if(priceQueue.size() > queueSize)
		{
			priceQueue.peek();
		}
	}
}
