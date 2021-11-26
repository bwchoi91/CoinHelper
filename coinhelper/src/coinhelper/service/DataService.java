package coinhelper.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import lombok.Getter;

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
	
	public List<String> enableCoinList = Lists.newArrayList();
	
	@Getter
	public Thread coinServiceThread;
	
	public Map<String, Thread> candleMinThreadMap = Maps.newConcurrentMap();
	
	public DataService()
	{
		dataService = this;
	}
	
	public static DataService get()
	{
		return dataService;
	}
	
	
	public void setInitData()
	{
		for(String market : enableCoinList)
		{
			Thread thread = new Thread(new CandleMinRefreshThread(market, 3, 200));
			thread.setName(String.format("%s_CandleMinThread", market));
			thread.setDaemon(true);
			
			candleMinThreadMap.put(market, thread);
		}
    	
    	//Thread Setting
    	coinServiceThread = new Thread(new CoinServiceThread(3));
    	coinServiceThread.setName("CoinRefreshThread");
    	coinServiceThread.setDaemon(false);
    	//Start
    	coinServiceThread.start();
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

	        	this.setInitData();
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
        		Coin coin = coinListMap.get(candleMinList.get(0).getMarket());
        		if(coin == null)
        		{
        			log.error(String.format("Cannot found Coin. market=%s", coin.getMarket()));
        		}
        		
        		coin.addCandleMinList(candleMinList);
        		
        		log.info(String.format("CandleMinList Add Complete. market=%s", market));
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
		        		coin.addPriceList(price);
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
	
/*	public void bwchoiTest()
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
			sb.append(coin.getMarket()+" : ");
			
			for(int i=coin.getPriceList().size()-1; i >= 0; i--)
			{
				sb.append(coin.getPriceList().get(i) + " ");
			}
			
			log.info(sb);
		}
	}*/
	
	public class CoinServiceThread implements Runnable
	{
		public int cycleTime = 1;
		public int cycleCount = 0;
		
		public CoinServiceThread(int cycleTime)
		{
			if(cycleTime > 0)
			{
				this.cycleTime = cycleTime;
				this.cycleCount = cycleTime;
			}
		}
		
		/**
		 * Ticker
		 */
		public void setTickerData()
		{
			if(cycleTime > cycleCount)
			{
				setTickerMap(enableCoinList, false);
				cycleCount++;
			}
			else
			{
				setTickerMap(enableCoinList, true);
				cycleCount = 0;
			}
		}
		
		public void setCandleMin()
		{
			String errMaeket = StringUtils.EMPTY;
			try
			{
				for(String market : enableCoinList)
				{
					errMaeket = market;
					if(candleMinThreadMap.get(market).isAlive() == false)
					{
						candleMinThreadMap.get(market).start();
					}
					else
					{
						candleMinThreadMap.get(market).run();
					}
					
					Thread.sleep(100);
				}
			}
			catch(Exception e)
			{
				log.info(String.format("SetCandleMin Error. market=%s, %s", errMaeket, e.getStackTrace()));
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
					
					setCandleMin();
//					setTickerData();	//Coin Data Refresh
					
					/* Set Data Function List End*/
					
					compareTime = Calendar.getInstance().getTimeInMillis() - startTime;
					//bwchoi 임시 주석
//					if(compareTime < 1000)
//						Thread.sleep(1000 - compareTime);
//					else
//						Thread.sleep(1000);
					
					//bwchoi Test
					Thread.sleep(10000);
				}
				catch(Exception e)
				{
					log.info(String.format("CoinServiceThread Interrupt.\n%s", e.toString()));
				}
			}
		}
	}
	
	public class CandleMinRefreshThread implements Runnable
	{
		public String market;
		public int min;
		public int count;
		
		public CandleMinRefreshThread(String market, int min, int count)
		{
			this.market = market;
			this.min = min;
			this.count = count;
		}
		
		@Override
		public void run()
		{
			try
			{
				setCandleMinMap(market, min, count);
			}
			catch(Exception e)
			{
				log.info(String.format("CandleMinRefreshThread Interrupt. market=%s min=%s count=%s \n%s", this.market, this.min, this.count, e.toString()));
			}
		}
	}
}
