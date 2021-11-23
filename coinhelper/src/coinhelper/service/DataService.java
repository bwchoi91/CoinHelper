package coinhelper.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import coinhelper.object.Coin;
import coinhelper.support.JsonParserList;
import coinhelper.support.ServiceUrlList;

import com.google.common.collect.Maps;

public class DataService {

	private Logger log = LogManager.getLogger(DataService.class);
	
	public static DataService dataService;
	
	@Autowired
	public JsonParserList jsonParserList;
	
	public Map<String, Coin> coinListMap = Maps.newConcurrentMap();
	
	public DataService()
	{
		dataService = this;
	}
	
	public static DataService get()
	{
		return dataService;
	}
	
	public void setCoinListMap()
	{
		try
		{
	        HttpClient client = HttpClientBuilder.create().build();
	
	        HttpGet request = new HttpGet(ServiceUrlList.getURL_MarketAll());
	        request.setHeader("Content-Type", "application/json");
	
	        HttpResponse response = client.execute(request);
	        HttpEntity entity = response.getEntity();
	        
			Object object = this.jsonParserList.stringToObject_markAllParser(new String(EntityUtils.toByteArray(entity), "UTF-8"));
	        
	        List<Coin> marketAllList = (List<Coin>) object;
	        
	        if(marketAllList.size() > 0)
	        {
	        	if(coinListMap.size() > 0)
	        	{
	        		log.info(String.format("Current CoinListMap Clear"));
	        		coinListMap.clear();
	        	}
	        	
	        	for(Coin coin : marketAllList)
	        	{
	        		//원화 거래하는 코인만 설정
	        		if(StringUtils.contains(coin.getMarket(), "KRW-"))
	        		{
	        			coinListMap.put(coin.getMarket(), coin);
	        			log.info(String.format("Put Complete. market=%s", coin.getMarket()));
	        		}
	        	}
	        }

	        
		}
		catch(Exception e)
		{
			log.error(String.format("SetCoinListMap Error.\n%s", e.toString()));
		}
	}
}
