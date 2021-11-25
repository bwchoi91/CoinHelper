package coinhelper.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import coinhelper.object.CandleMin;
import coinhelper.object.Coin;
import coinhelper.object.Ticker;
import coinhelper.support.JsonParserList;
import coinhelper.support.ServiceUrlList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mchange.lang.FloatUtils;

public class DataService {

	private Logger log = LogManager.getLogger(DataService.class);
	
	public static DataService dataService;
	
	@Autowired
	public JsonParserList jsonParserList;
	
	public Map<String, Coin> coinListMap = Maps.newConcurrentMap();
	public Map<String, List<CandleMin>> candleMinMap = Maps.newConcurrentMap();
	
	public List<String> enableCoinList = Lists.newArrayList();
	
	public Thread tickerMapRefreshThread;
	
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
        			coinListMap.put(coin.getMarket(), coin);
        			log.info(String.format("Put Complete. market=%s", coin.getMarket()));
	        		
        			//원화 거래하는 코인만 Enable(조건으로 설정하도록 할 예정)
	        		if(StringUtils.contains(coin.getMarket(), "KRW-"))
	        		{
	        			enableCoinList.add(coin.getMarket());

	        		}
	        	}

	        	//Thread Setting
	        	tickerMapRefreshThread = new Thread(new TickerRefreshThread(3));
	        	tickerMapRefreshThread.setName("TickerRefreshThread");
	        	tickerMapRefreshThread.setDaemon(false);
	        	//Start
	        	tickerMapRefreshThread.start();
	        	
	        }
		}
		catch(Exception e)
		{
			log.error(String.format("%s Error.\n%s", this.getClass().getEnclosingMethod().getName(), e.toString()));
		}
	}
	
	public void setCandleMinMap(String market, int min, int count)
	{
		try
		{
			HttpClient client = HttpClientBuilder.create().build();
			
	        HttpGet request = new HttpGet(ServiceUrlList.getURL_CandleMin(min, market, count));
	        request.setHeader("Accept", "application/json");
	        
	        HttpResponse response = client.execute(request);
	        HttpEntity entity = response.getEntity();
	        
			Object object = this.jsonParserList.stringToObject_candleMinParser(new String(EntityUtils.toByteArray(entity), "UTF-8"));
	        
	        List<CandleMin> candleMinList = (List<CandleMin>) object;
	        
	        if(candleMinList.size() > 0)
	        {
	        	candleMinList.get(0).getMarket();
        		candleMinMap.put(candleMinList.get(0).getMarket(), candleMinList);
        		
        		//log
        		for(CandleMin candleMin : candleMinList)
        		{
        			log.info(String.format("time=%s, openPrice=%s, highPrice=%s, lowPrice=%s, tradePrice=%s, min=%s", candleMin.getCandleDateTimeKST(), candleMin.getOpeningPrice(), candleMin.getHighPrice(), candleMin.getLowPrice(), candleMin.getTradePrice(), candleMin.getUnit()));
        		}
	        }
		}
		catch(Exception e)
		{
			log.info(String.format("%s Error.\n%s", this.getClass().getEnclosingMethod().getName(), e.toString()));
		}
	}
	
	public void setTickerMap(List<String> marketList, boolean isPush)
	{
		try
		{
			StringBuilder str = new StringBuilder();
			
			for(String market : marketList)
			{
				str.append(market);
				str.append(",");
			}
			
			str.deleteCharAt(str.length()-1);
			
			HttpClient client = HttpClientBuilder.create().build();
			
	        HttpGet request = new HttpGet(ServiceUrlList.getURL_ticker(str));
	        request.setHeader("Accept", "application/json");
	        
	        HttpResponse response = client.execute(request);
	        HttpEntity entity = response.getEntity();
	        
			Object object = this.jsonParserList.stringToObject_tickerParser(new String(EntityUtils.toByteArray(entity), "UTF-8"));
	        
	        List<Ticker> tickerList = (List<Ticker>) object;
	        
	        for(Ticker ticker : tickerList)
	        {
	        	Coin coin = coinListMap.get(ticker.getMarket());
	        	float price = FloatUtils.parseFloat(ticker.getTrade_price(), 0);
	        	
	        	if(price != 0)
	        	{
		        	coin.setCurrentTradePrice(price);
		        	if(isPush)
		        	{
		        		coin.pushPrice(price);
		        	}
		        	
//		        	log.info(String.format("%s : %.4f", coin.getMarket(), coin.getCurrentTradePrice()));
	        	}
	        }
		}
		catch(Exception e)
		{
			log.info(String.format("%s Error.\n%s", this.getClass().getEnclosingMethod().getName(), e.toString()));
		}
	}
	
	public void bwchoiTest()
	{
		for(String market : enableCoinList)
		{
			Coin coin = coinListMap.get(market);
			if(coin == null)
			{
				log.error(String.format("Cannot found Coin. market=%s", market));
				continue;
			}
			StringBuilder sb = new StringBuilder();
			sb.append(coin.getMarket()+"\n");
			
			enableCoinList.
		}
	}
	
	public class TickerRefreshThread implements Runnable
	{
		public int pushTime = 1;
		public int pushCount = 0;
		
		public TickerRefreshThread(int pushTime)
		{
			if(pushTime > 0)
				this.pushTime = pushTime;
		}
		
		public void setCoinData()
		{
			if(pushTime > pushCount)
			{
				setTickerMap(enableCoinList, false);
				pushCount++;
			}
			else
			{
				setTickerMap(enableCoinList, true);
				pushCount = 0;
			}
		}
		
		@Override
		public void run()
		{
			long startTime;
			long compareTime;
			
			while(true)
			{
				try
				{
					startTime = Calendar.getInstance().getTimeInMillis();
					
					/* Set Data Function List Start*/
					
					setCoinData();

					/* Set Data Function List End*/
					
					compareTime = Calendar.getInstance().getTimeInMillis() - startTime;
					if(compareTime < 1000)
						Thread.sleep(1000 - compareTime);
					else
						Thread.sleep(1000);
				}
				catch(Exception e)
				{
					log.info(String.format("tickerRefreshThread Interrupt.\n%s", e.toString()));
				}
			}
		}
	}
}
