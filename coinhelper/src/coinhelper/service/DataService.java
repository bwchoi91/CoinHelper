package coinhelper.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import lombok.Getter;

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
		/*for(String market : enableCoinList)
		{
			Thread thread = new Thread(new CandleMinRefreshThread(market, 3, 200));
			thread.setName(String.format("%s_CandleMinThread", market));
			thread.setDaemon(true);
			thread.start();
			
			break;
		}*/
    	
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
        		
//        		log.info(String.format("CandleMinList Add Complete. market=%s", market));
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
	
	/* 조건
	 * 1. 이전 시간보다 종가가 큰 경우(상승봉) 
	 * 2. 1의 조건이 3번 연속인 경우
	 * */
	public void bwchoiTest()
	{
		log.info("Test 1 Start");
		List<CandleMin> resultCandleMinList = Lists.newArrayList();
		//3분봉 리스트로 조건 체크 해보기
		for(String market : enableCoinList)
		{
			List<CandleMin> candleMinList = coinListMap.get(market).getCandleMinList();

			float tradePrice = 0;
			int[] testResult = {0, 0, 0};	//0 : false(not test) 1 : true(test true)
			
			for(int i = candleMinList.size()-1; i >= 0; i--)
			{
				CandleMin candle = candleMinList.get(i);
				if(candle == null)
				{
					log.info(String.format("Cannot found CandleMin. position=%s", i));
					continue;
				}
				
				//처음 한바퀴
				if(tradePrice <= 0)
				{
					tradePrice = candle.getTradePrice();
					continue;
				}
				//조건이 다 맞춰져 있으면 해당 Candle 출력
				if(testResult[0] == 1 && testResult[1] == 1 && testResult[2] == 1)
				{
					resultCandleMinList.add(candle);
					for(int count = 0; count < testResult.length; count++)
					{
						testResult[count] = 0;
					}
				}
			
				if(candle.getTradePrice() > tradePrice)
				{
					for(int count = 0; count < testResult.length; count++)
					{
						if(testResult[count] == 0)
						{
							testResult[count] = 1;
							break;
						}
					}
				}
				else	//조건 실패시 result 초기화
				{
					for(int count = 0; count < testResult.length; count++)
					{
						testResult[count] = 0;
					}
				}
				
				tradePrice = candle.getTradePrice();
			}
		}

		//100 Over
		int tradeTrueCount = 0;
		int highTrueCount = 0;
		int lowTrueCount = 0;
		
		//102 Over
		int tradeTrueCount102 = 0;
		int highTrueCount102 = 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("Result.\n");
		for(CandleMin candle : resultCandleMinList)
		{
			float tradeCompareRate = (candle.getTradePrice() / candle.getOpeningPrice()) * 100;
			float highCompareRate = (candle.getHighPrice() / candle.getOpeningPrice()) * 100;
			float lowCompareRate = (candle.getLowPrice() / candle.getOpeningPrice()) * 100;
			
			boolean tradeCompareResult = tradeCompareRate > 100 ? true : false;
			boolean highCompareResult = highCompareRate > 100 ? true : false;
			boolean lowCompareResult = lowCompareRate > 100 ? true : false;

			if(tradeCompareResult == true)
				tradeTrueCount++;
			if(highCompareResult == true)
				highTrueCount++;
			if(lowCompareResult == true)
				lowTrueCount++;
				
			if(tradeCompareRate >= 102)
				tradeTrueCount102++;
			if(highTrueCount102 >= 102)
				highTrueCount102++;
			
			sb.append(String.format("market=%s time=%s openingPrice=%.2f highPrice=%.2f lowPrice=%.2f tradePrice=%.2f "
					+ "종가비교 : %.2f 고가비교 : %.2f 저가비교 : %.2f 종가상승 : %s, 고가상승 : %s 저가상승 : %s \n",  
					candle.getMarket(), candle.getCandleDateTimeKST(), candle.getOpeningPrice(), candle.getHighPrice(),
					candle.getLowPrice(), candle.getTradePrice(), tradeCompareRate, highCompareRate, lowCompareRate,
					tradeCompareResult, highCompareResult, lowCompareResult));
		}
		
		sb.append(String.format("TotalCount=%s tradeTrueCount=%s highTrueCount=%s lowTrueCount=%s\n", resultCandleMinList.size(), tradeTrueCount, highTrueCount, lowTrueCount));
		sb.append(String.format("tradeTrue102Over=%s highTrue102Over=%s", tradeTrueCount102, highTrueCount102));
		log.info(sb);
		
		log.info("Test 1 End");
	}
	
	/* 조건
	 * 1. 이전 시간보다 종가가 큰 경우(상승봉) 
	 * 2. 1의 조건이 2번 연속인 경우
	 * */
	public void bwchoiTest2()
	{
		log.info("Test 2 Start");
		
		List<CandleMin> resultCandleMinList = Lists.newArrayList();
		//3분봉 리스트로 조건 체크 해보기
		for(String market : enableCoinList)
		{
			List<CandleMin> candleMinList = coinListMap.get(market).getCandleMinList();

			float tradePrice = 0;
			int[] testResult = {0, 0};	//0 : false(not test) 1 : true(test true)
			
			for(int i = candleMinList.size()-1; i >= 0; i--)
			{
				CandleMin candle = candleMinList.get(i);
				if(candle == null)
				{
					log.info(String.format("Cannot found CandleMin. position=%s", i));
					continue;
				}
				
				//처음 한바퀴
				if(tradePrice <= 0)
				{
					tradePrice = candle.getTradePrice();
					continue;
				}
				//조건이 다 맞춰져 있으면 해당 Candle 출력
				if(testResult[0] == 1 && testResult[1]  == 1)
				{
					resultCandleMinList.add(candle);
					for(int count = 0; count < testResult.length; count++)
					{
						testResult[count] = 0;
					}
				}
			
				if(candle.getTradePrice() > tradePrice)
				{
					for(int count = 0; count < testResult.length; count++)
					{
						if(testResult[count] == 0)
						{
							testResult[count] = 1;
							break;
						}
					}
				}
				else	//조건 실패시 result 초기화
				{
					for(int count = 0; count < testResult.length; count++)
					{
						testResult[count] = 0;
					}
				}
				
				tradePrice = candle.getTradePrice();
			}
		}

		//100 Over
		int tradeTrueCount = 0;
		int highTrueCount = 0;
		int lowTrueCount = 0;
		
		//102 Over
		int tradeTrueCount102 = 0;
		int highTrueCount102 = 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("Result.\n");
		for(CandleMin candle : resultCandleMinList)
		{
			float tradeCompareRate = (candle.getTradePrice() / candle.getOpeningPrice()) * 100;
			float highCompareRate = (candle.getHighPrice() / candle.getOpeningPrice()) * 100;
			float lowCompareRate = (candle.getLowPrice() / candle.getOpeningPrice()) * 100;
			
			boolean tradeCompareResult = tradeCompareRate > 100 ? true : false;
			boolean highCompareResult = highCompareRate > 100 ? true : false;
			boolean lowCompareResult = lowCompareRate > 100 ? true : false;

			if(tradeCompareResult == true)
				tradeTrueCount++;
			if(highCompareResult == true)
				highTrueCount++;
			if(lowCompareResult == true)
				lowTrueCount++;
			
			if(tradeCompareRate >= 102)
				tradeTrueCount102++;
			if(highTrueCount102 >= 102)
				highTrueCount102++;
			
			sb.append(String.format("market=%s time=%s openingPrice=%.2f highPrice=%.2f lowPrice=%.2f tradePrice=%.2f "
					+ "종가비교 : %.2f 고가비교 : %.2f 저가비교 : %.2f 종가상승 : %s, 고가상승 : %s 저가상승 : %s \n",  
					candle.getMarket(), candle.getCandleDateTimeKST(), candle.getOpeningPrice(), candle.getHighPrice(),
					candle.getLowPrice(), candle.getTradePrice(), tradeCompareRate, highCompareRate, lowCompareRate,
					tradeCompareResult, highCompareResult, lowCompareResult));
		}
		
		sb.append(String.format("TotalCount=%s tradeTrueCount=%s highTrueCount=%s lowTrueCount=%s\n", resultCandleMinList.size(), tradeTrueCount, highTrueCount, lowTrueCount));
		sb.append(String.format("tradeTrue102Over=%s highTrue102Over=%s", tradeTrueCount102, highTrueCount102));
		log.info(sb);

		log.info("Test 2 End");
	}
	
	/**
	 * 조건
	 * 1. 5분봉
	 * 2. 큰 양봉일때(3% 이상)	(조건 1)
	 * 3. 2의 조건에서 다음 3개의 봉이 음봉일때 (조건 2,3,4)
	 * 4. 3의 조건에서 연속된 5분봉 의 차가 없을때는 묶어서 하나로 취급
	 * 5. 3의 조건에서 5분봉 변화가 없을때(상승은 제외)는 조건 Continue
	 */
	public void bwchoiTest3(int conditionCount, int condition1Para, int condition2Para, int condition3Para, int condition4Para)
	{
		log.info("Test 2 Start");
		
		List<CandleMin> resultCandleMinList = Lists.newArrayList();
		//3분봉 리스트로 조건 체크 해보기
		for(String market : enableCoinList)
		{
			List<CandleMin> candleMinList = coinListMap.get(market).getCandleMinList();

			float tradePrice = 0;

			int conditionNo = 0;
			for(int i = candleMinList.size()-1; i >= 0; i--)
			{
				CandleMin candle = candleMinList.get(i);
				if(candle == null)
				{
					log.info(String.format("Cannot found CandleMin. position=%s", i));
					continue;
				}
				
				//처음 한바퀴
				if(tradePrice <= 0)
				{
					tradePrice = candle.getTradePrice();
					continue;
				}
				
				//모든 조건 만족
				if(conditionNo >= conditionCount)
				{
					resultCandleMinList.add(candle);
					conditionNo = 0;
				}
				
				switch(this.condition(candle, tradePrice, conditionNo))
				{
					case -1 : break;	//Continue
					case 0 : conditionNo = 0; break;	//NG
					case 1 : conditionNo++; break;	//OK
				}
				
				tradePrice = candle.getTradePrice();
			}
		}

		//100 Over
		int tradeTrueCount = 0;
		int highTrueCount = 0;
		int lowTrueCount = 0;
		
		//102 Over
		int tradeTrueCount102 = 0;
		int highTrueCount102 = 0;
		
		StringBuilder sb = new StringBuilder();
		sb.append("Result.\n");
		for(CandleMin candle : resultCandleMinList)
		{
			float tradeCompareRate = (candle.getTradePrice() / candle.getOpeningPrice()) * 100;
			float highCompareRate = (candle.getHighPrice() / candle.getOpeningPrice()) * 100;
			float lowCompareRate = (candle.getLowPrice() / candle.getOpeningPrice()) * 100;
			
			boolean tradeCompareResult = tradeCompareRate > 100 ? true : false;
			boolean highCompareResult = highCompareRate > 100 ? true : false;
			boolean lowCompareResult = lowCompareRate > 100 ? true : false;

			if(tradeCompareResult == true)
				tradeTrueCount++;
			if(highCompareResult == true)
				highTrueCount++;
			if(lowCompareResult == true)
				lowTrueCount++;
			
			if(tradeCompareRate >= 102)
				tradeTrueCount102++;
			if(highTrueCount102 >= 102)
				highTrueCount102++;
			
			sb.append(String.format("market=%s time=%s openingPrice=%.2f highPrice=%.2f lowPrice=%.2f tradePrice=%.2f "
					+ "종가비교 : %.2f 고가비교 : %.2f 저가비교 : %.2f 종가상승 : %s, 고가상승 : %s 저가상승 : %s \n",  
					candle.getMarket(), candle.getCandleDateTimeKST(), candle.getOpeningPrice(), candle.getHighPrice(),
					candle.getLowPrice(), candle.getTradePrice(), tradeCompareRate, highCompareRate, lowCompareRate,
					tradeCompareResult, highCompareResult, lowCompareResult));
		}
		
		sb.append(String.format("TotalCount=%s tradeTrueCount=%s highTrueCount=%s lowTrueCount=%s\n", resultCandleMinList.size(), tradeTrueCount, highTrueCount, lowTrueCount));
		sb.append(String.format("tradeTrue102Over=%s highTrue102Over=%s", tradeTrueCount102, highTrueCount102));
		log.info(sb);

		log.info("Test 2 End");
	}
	
	/**
	 * @param candle
	 * @param tradePrice
	 * @param conditionCount
	 * @return
	 * 
	 * -1 : Continue
	 *  0 : NG
	 *  1 : OK
	 */
	public int condition(CandleMin candle, float tradePrice, int conditionNo, int... para)
	{
		/**
		 * 조건
		 * 1. 5분봉
		 * 2. 큰 양봉일때(3% 이상)	(조건 1)
		 * 3. 2의 조건에서 다음 3개의 봉이 음봉일때 (조건 2,3,4)
		 * 4. 3의 조건에서 연속된 5분봉 의 차가 없을때는 묶어서 하나로 취급
		 * 5. 3의 조건에서 5분봉 변화가 없을때(상승은 제외)는 조건 Continue
		 */
		
		float rate = (float)(1 + para[conditionNo]*0.01);
		
		//First Condition
		if(conditionNo == 0)
		{
			if(candle.getTradePrice() > tradePrice*rate)
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}
		//Seconds Condition
		else if(conditionNo == 1)
		{
			
		}
		
		return 0;
	}
	
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
			List<Thread> threadList = Lists.newArrayList();
			try
			{
				for(String market : enableCoinList)
				{
					errMaeket = market;
					
					Thread thread = new Thread(new CandleMinRefreshThread(market, 3, 200));
					thread.setName(String.format("%s_CandleMinThread", market));
					thread.setDaemon(true);
					
					threadList.add(thread);
					
					thread.start();
					
					Thread.sleep(150);
				}
				
				for(Thread thread : threadList)
				{
					thread.join();
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
					log.info(String.format("Candle Set Completed."));

					bwchoiTest();
					bwchoiTest2();
					//Candle Data로 분석 하는 로직
					
//					
					/* Set Data Function List End*/
					
					compareTime = Calendar.getInstance().getTimeInMillis() - startTime;
					//bwchoi 임시 주석
//					if(compareTime < 1000)
//						Thread.sleep(1000 - compareTime);
//					else
//						Thread.sleep(1000);
					
					//bwchoi Test
					break;
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
			while(true)
			{
				try
				{
					setCandleMinMap(market, min, count);
					break;
				}
				catch(Exception e)
				{
					log.info(String.format("CandleMinRefreshThread Interrupt. market=%s min=%s count=%s \n%s", this.market, this.min, this.count, e.toString()));
					try {
						Thread.sleep(300);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}
}
