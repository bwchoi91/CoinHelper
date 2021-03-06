package coinhelper.service;

import java.io.FileWriter;
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
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import coinhelper.manager.CandleMinManager;
import coinhelper.object.Coin;
import coinhelper.object.Ticker;
import coinhelper.orm.CandleMin;
import coinhelper.support.JsonParserList;
import coinhelper.support.ServiceUrlList;
import coinhelper.support.CoinHelperUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mchange.lang.FloatUtils;
import com.mchange.lang.IntegerUtils;

public class DataService {

	private Logger log = LogManager.getLogger(DataService.class);
	
	public static DataService dataService;
	
	@Autowired
	public JsonParserList jsonParserList;

	@Autowired
	public CandleMinManager candleMinManager;
	
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
//        			log.info(String.format("Put Complete. market=%s", coin.getMarket()));
	        		
        			//?????? ???????????? ????????? Enable(???????????? ??????????????? ??? ??????)
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
	
	public void setCandleMinMap(String market, int min, int count, String time)
	{
		try
		{
			HttpClient client = HttpClientBuilder.create().build();
			
	        HttpGet request = new HttpGet(ServiceUrlList.getURL_CandleMin(min, market, count, time));
	        request.setHeader("Accept", "application/json");

	        Object object = null;
	        while(true)
	        {
		        HttpResponse response = client.execute(request);
		        HttpEntity entity = response.getEntity();
		        
		        String entityStr = new String(EntityUtils.toByteArray(entity), "UTF-8");
	//	        entityStr = StringUtils.replace(entityStr, "timestamp", "timestamps");
	
				object = this.jsonParserList.stringToObject_candleMinParser(entityStr);
				
				if(object != null)
					break;
				
				Thread.sleep(500);
	        }
	        
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
        		
        		this.updateCandleMinDataDB(candleMinList);
	        }
		}
		catch(Exception e)
		{
			log.info(String.format("%s Error.\n%s", "Data Get Error", e.toString()));
		}
	}
	
	public void createCandleMinDateByXML()
	{
		try
		{
			// ?????? ????????????
			Document doc =  new Document();
			
			Element candleElement = new Element("candle");
			doc.addContent(candleElement);
			
			for(String market : this.enableCoinList)
			{
				Coin coin = this.coinListMap.get(market);
				if(coin == null)
				{
					log.info(String.format("Cannot found Coin. market=%s", market));
					continue;
				}
				
				if(coin.getCandleMinList().size() < 1)
				{
					log.info(String.format("Cannot found CandleMinList. market=%s", market));
					continue;
				}
	
				Element marketElement = new Element("market");
				marketElement.setAttribute("name", market);
				
				candleElement.addContent(marketElement);
			
				for(CandleMin candle : coin.getCandleMinList())
				{
					Element candleMinElement = new Element("candleMin");
					
					candleMinElement.addContent(new Element("candleDateTimeUTC").setText(candle.getCandleDateTimeUTC()));
					candleMinElement.addContent(new Element("candleDateTimeKST").setText(candle.getCandleDateTimeKST()));
					candleMinElement.addContent(new Element("openingPrice").setText(String.valueOf(candle.getOpeningPrice())));
					candleMinElement.addContent(new Element("highPrice").setText(String.valueOf(candle.getHighPrice())));
					candleMinElement.addContent(new Element("lowPrice").setText(String.valueOf(candle.getLowPrice())));
					candleMinElement.addContent(new Element("tradePrice").setText(String.valueOf(candle.getTradePrice())));
//					candleMinElement.addContent(new Element("timestamp").setText(String.valueOf(candle.getTimestamp())));
					candleMinElement.addContent(new Element("candleAccTradePrice").setText(String.valueOf(candle.getCandleAccTradePrice())));
					candleMinElement.addContent(new Element("candleAccTradevolume").setText(String.valueOf(candle.getCandleAccTradevolume())));
					candleMinElement.addContent(new Element("unit").setText(String.valueOf(candle.getUnit())));
					
					marketElement.addContent(candleMinElement);
				}

				FileWriter write = new FileWriter(String.format("./data/candleMinData_%s.xml", market));
				XMLOutputter output = new XMLOutputter();
				
				output.setFormat(Format.getPrettyFormat());
				
				output.output(doc, write);
				
				write.close();
			}
			
		}
		catch(Exception e)
		{
			log.error(e);
		}
	}
	
	public void getCandleMinDataByXML()
	{
		for(String market : this.enableCoinList)
		{
			Coin coin = this.coinListMap.get(market);
			if(coin == null)
			{
				log.error(String.format("Cannot found Coin. market=%s", market));
				continue;
			}
			
			String filePath = String.format("./data/candleMinData_%s.xml", market);
			
			Document doc = CoinHelperUtils.makeDocumentFilePath(filePath);
			if(doc == null)
			{
				log.info(String.format("Cannot found file. path=%s", filePath));
				return;
			}
			
			List<CandleMin> candleMinList = Lists.newArrayList();
			
			Element marketElement = doc.getRootElement().getChild("market");
			List<Element> candleMinElementList = marketElement.getChildren();
			
			for(Element candleMinElement : candleMinElementList)
			{
//				CandleMin candle = new CandleMin(market);
//				candle.setCandleDateTimeUTC(candleMinElement.getChild("candleDateTimeUTC").getText());
//				candle.setCandleDateTimeKST(candleMinElement.getChild("candleDateTimeKST").getText());
//				candle.setOpeningPrice(FloatUtils.parseFloat(candleMinElement.getChild("openingPrice").getText()));
//				candle.setHighPrice(FloatUtils.parseFloat(candleMinElement.getChild("highPrice").getText()));
//				candle.setLowPrice(FloatUtils.parseFloat(candleMinElement.getChild("lowPrice").getText()));
//				candle.setTradePrice(FloatUtils.parseFloat(candleMinElement.getChild("tradePrice").getText()));
////				candle.setTimestamp(Long.valueOf(candleMinElement.getChild("timestamp").getText()));
//				candle.setCandleAccTradePrice(FloatUtils.parseFloat(candleMinElement.getChild("candleAccTradePrice").getText()));
//				candle.setCandleAccTradevolume(FloatUtils.parseFloat(candleMinElement.getChild("candleAccTradevolume").getText()));
//				candle.setUnit(IntegerUtils.parseInt(candleMinElement.getChild("unit").getText(), 0));
				
//				candleMinList.add(candle);
			}
			
			coin.setCandleMinList(candleMinList);
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
	
	/* ??????
	 * 1. ?????? ???????????? ????????? ??? ??????(?????????) 
	 * 2. 1??? ????????? 3??? ????????? ??????
	 * */
	public void bwchoiTest()
	{
		log.info("Test 1 Start");
		List<CandleMin> resultCandleMinList = Lists.newArrayList();
		//3?????? ???????????? ?????? ?????? ?????????
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
				
				//?????? ?????????
				if(tradePrice <= 0)
				{
					tradePrice = candle.getTradePrice();
					continue;
				}
				//????????? ??? ????????? ????????? ?????? Candle ??????
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
				else	//?????? ????????? result ?????????
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
					+ "???????????? : %.2f ???????????? : %.2f ???????????? : %.2f ???????????? : %s, ???????????? : %s ???????????? : %s \n",  
					candle.getMarket(), candle.getCandleDateTimeKST(), candle.getOpeningPrice(), candle.getHighPrice(),
					candle.getLowPrice(), candle.getTradePrice(), tradeCompareRate, highCompareRate, lowCompareRate,
					tradeCompareResult, highCompareResult, lowCompareResult));
		}
		
		sb.append(String.format("TotalCount=%s tradeTrueCount=%s highTrueCount=%s lowTrueCount=%s\n", resultCandleMinList.size(), tradeTrueCount, highTrueCount, lowTrueCount));
		sb.append(String.format("tradeTrue102Over=%s highTrue102Over=%s", tradeTrueCount102, highTrueCount102));
		log.info(sb);
		
		log.info("Test 1 End");
	}
	
	/* ??????
	 * 1. ?????? ???????????? ????????? ??? ??????(?????????) 
	 * 2. 1??? ????????? 2??? ????????? ??????
	 * */
	public void bwchoiTest2()
	{
		log.info("Test 2 Start");
		
		List<CandleMin> resultCandleMinList = Lists.newArrayList();
		//3?????? ???????????? ?????? ?????? ?????????
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
				
				//?????? ?????????
				if(tradePrice <= 0)
				{
					tradePrice = candle.getTradePrice();
					continue;
				}
				//????????? ??? ????????? ????????? ?????? Candle ??????
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
				else	//?????? ????????? result ?????????
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
					+ "???????????? : %.2f ???????????? : %.2f ???????????? : %.2f ???????????? : %s, ???????????? : %s ???????????? : %s \n",  
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
	 * ??????
	 * 1. 5??????
	 * 2. ??? ????????????	(?????? 1) para0
	 * 3. ???????????? ???????????? ??????(?????? 2) para1
	 * 4. 3??? ???????????? ?????? 3?????? ?????? ???????????? (?????? 3,4,5)(???????????? ????????? ????????? ????????? ??????) para2
	 * 5. 4??? ???????????? 5?????? ????????? ?????? ???????????? ?????? Continue para3
	 * 6. Buy ??? ??? ????????? para4
	 * 7. Buy ??? ??? ????????? para5
	 */
	public void bwchoiTest3(int conditionCount, int... para)
	{
		log.info("bwchoiTest3 Start");
		
		List<CandleMin> resultCandleMinList = Lists.newArrayList();
		
		Map<String, List<CandleMin>> historyCandleMinListMap = Maps.newConcurrentMap();
		
		List<CandleMin> buyCandleMinList = Lists.newArrayList();
		List<CandleMin> goodSellCandleMinList = Lists.newArrayList();
		List<CandleMin> badSellCandleMinList = Lists.newArrayList();
		
		//3?????? ???????????? ?????? ?????? ?????????
		for(String market : enableCoinList)
		{
			List<CandleMin> candleMinList = coinListMap.get(market).getCandleMinList();

			Coin coin = this.coinListMap.get(market);
			if(coin == null)
			{
				log.error(String.format("Cannot found coin. market=%s", market));
				break;
			}
			
			float tradePrice = 0;
			int conditionNo = 0;
			
			List<CandleMin> historyCandleMinList = Lists.newArrayList();
			
			for(int i = candleMinList.size()-1; i >= 0; i--)
			{
				CandleMin candle = candleMinList.get(i);
				if(candle == null)
				{
					log.info(String.format("Cannot found CandleMin. position=%s", i));
					continue;
				}
				
				//?????? ?????????
				if(tradePrice <= 0)
				{
					tradePrice = candle.getTradePrice();
					coin.setBuy(false);
					
					continue;
				}
				
				if(coin.isBuy() == false)
				{
					//?????? ????????? ??????(Buy)
					if(conditionNo >= conditionCount)
					{
						resultCandleMinList.add(candle);
						historyCandleMinList.add(candle);
						
						List<CandleMin> copyHistory = Lists.newArrayList();
						copyHistory.addAll(historyCandleMinList);
						
						historyCandleMinListMap.put(candle.getMarket(), copyHistory);
						
						conditionNo = 0;
						historyCandleMinList.clear();
						
						buyCandleMinList.add(candle);
						
						//Buy Logic(now Test)
						
						coin.setBuyPrice(candle.getOpeningPrice());
						coin.setBuy(true);
						
						continue;
						
					}
					
					switch(this.buyCondition(candle, tradePrice, conditionNo, para))
					{
						case -1 : tradePrice = candle.getTradePrice(); historyCandleMinList.add(candle); break;	//Continue
						case 0 : conditionNo = 0; tradePrice = candle.getTradePrice(); historyCandleMinList.clear(); break;	//NG
						case 1 : conditionNo++; tradePrice = candle.getTradePrice(); historyCandleMinList.add(candle); break;	//OK
					}
				}
				else	//isBuy == true
				{
					
					int isSell = this.sellCondition(coin, candle, para);
					// 1:GoodSell, 0:NotSell, -1:BadSell
					if(isSell == 1)
					{
						goodSellCandleMinList.add(candle);

						coin.setBuyPrice(0);
						coin.setBuy(false);
					}
					else if(isSell == -1)
					{
						badSellCandleMinList.add(candle);

						coin.setBuyPrice(0);
						coin.setBuy(false);
					}
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Result.\n");
		for(CandleMin candle : resultCandleMinList)
		{
			List<CandleMin> historyCandleList = historyCandleMinListMap.get(candle.getMarket());
			if(historyCandleList.size() < 1)
			{
				log.error(String.format("Cannot found HistoryCandle. market=%s", candle.getMarket()));
				continue;
			}
			for(CandleMin historyCandle : historyCandleList)
			{
				sb.append(String.format("market=%s time=%s openingPrice=%.2f highPrice=%.2f lowPrice=%.2f tradePrice=%.2f \n",
						historyCandle.getMarket(), historyCandle.getCandleDateTimeKST(), historyCandle.getOpeningPrice(), historyCandle.getHighPrice(),
						historyCandle.getLowPrice(), historyCandle.getTradePrice()));
			}
		}
		
		sb.append(String.format("\nBuy List. count=%s\n", buyCandleMinList.size()));
		for(CandleMin candle : buyCandleMinList)
		{
			sb.append(String.format("market=%s time=%s BuyPrice=%.2f \n", candle.getMarket(), candle.getCandleDateTimeKST(), candle.getOpeningPrice()));
		}
		
		sb.append(String.format("\nGood Sell List. rate=%.2f count=%s\n", (100+para[4]*0.1), goodSellCandleMinList.size()));
		for(CandleMin candle : goodSellCandleMinList)
		{
			sb.append(String.format("market=%s time=%s HighPrice=%.2f LowPrice=%.2f\n", candle.getMarket(), candle.getCandleDateTimeKST(), candle.getHighPrice(), candle.getLowPrice()));
		}
		
		sb.append(String.format("\nBad Sell List. rate=%.2f count=%s\n", (100-para[5]*0.1), badSellCandleMinList.size()));
		for(CandleMin candle : badSellCandleMinList)
		{
			sb.append(String.format("market=%s time=%s LowPrice=%.2f HighPrice=%.2f \n", candle.getMarket(), candle.getCandleDateTimeKST(), candle.getLowPrice(), candle.getHighPrice()));
		}
		log.info(sb);

		log.info("bwchoiTest3 End");
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
	public int buyCondition(CandleMin candle, float tradePrice, int conditionNo, int... para)
	{
		/**
		 * ??????
		 * 1. 5??????
		 * 2. ??? ????????????	(?????? 1) para0
		 * 3. ???????????? ???????????? ??????(?????? 2) para1
		 * 4. 3??? ???????????? ?????? 3?????? ?????? ???????????? (?????? 3,4,5)(???????????? ????????? ????????? ????????? ??????) para2
		 * 5. 4??? ???????????? 5?????? ????????? ??????????????? Continue ?????? para3
		 */
		
		//First Condition
		if(conditionNo == 0)
		{
			float rate = (float)(1 + para[0]*0.01);
			
			//??? ????????????	(?????? 1) para0
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
			float rate = (float)(1 - para[1]*0.01);
			
			//???????????? ???????????? ??????(?????? 2) para1
			if(candle.getTradePrice() < tradePrice*rate)
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}
		
		//Third~ Condition
		else if(conditionNo == 2 || conditionNo == 3 || conditionNo == 4)
		{
			float rate = (float)(1 - para[2]*0.01);
			float upRate = (float)(1 + para[3]*0.001);
			
			//?????? 3?????? ?????? ???????????? (?????? 3,4,5)(???????????? ????????? ????????? ????????? ??????) para2
			if(candle.getTradePrice() < tradePrice*rate)
			{
				return 1;
			}
			else
			{
				// 5?????? ????????? ?????? ???????????? ?????? Continue
				// ?????????????????? 0.3% ????????? ?????? ????????? Continue
				if(candle.getTradePrice() < tradePrice*upRate && candle.getTradePrice() > tradePrice)
				{
					return -1;
				}
				// ??????????????????  para2 ???????????? ??? ????????? Continue
				else if(candle.getTradePrice() > tradePrice*rate && candle.getTradePrice() < tradePrice)
				{
					return -1;
				}
				else
				{
					return 0;
				}
			}
		}
		
		return 0;
	}
	
	public int sellCondition(Coin coin, CandleMin candle, int... para)
	{
		float goodSell = (float) (1 + para[4]*0.001);
		float badSell = (float) (1 - para[5]*0.001);
		int isSell = 0;
		
		//??????
		if(candle.getHighPrice() > coin.getBuyPrice() * goodSell)
		{
			isSell = 1;
		}
		
		//??????
		else if(candle.getLowPrice() < coin.getBuyPrice() * badSell)
		{
			isSell = -1;
		}
		
		return isSell;
	}
	
	public void updateCandleMinDataDB(List<CandleMin> candleMinList)
	{
		try
		{
			this.candleMinManager.save(candleMinList);
			
			log.info(String.format("CandleMinData Update Completed. market=%s", candleMinList.get(0).getMarket()));
		}
		catch(Exception e)
		{
			log.error(e);
		}
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
		
		
		/**
		 * CandleMin
		 */
		public void setCandleMin()
		{
			String errMaeket = StringUtils.EMPTY;
			List<Thread> threadList = Lists.newArrayList();
			try
			{
				for(String market : enableCoinList)
				{
					errMaeket = market;
					
					Thread thread = new Thread(new CandleMinRefreshThread(market, 5, 200));
					thread.setName(String.format("%s_CandleMinThread", market));
					thread.setDaemon(true);
					
					threadList.add(thread);
					
					thread.start();
					
					Thread.sleep(150);
					
					//test
					break;
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
					
					//Data Get By Upbit
					setCandleMin();
					//Data Get By xmlFile
//					getCandleMinDataByXML();
					
					log.info(String.format("Candle Set Completed."));
//					createCandleMinDateByXML();

					//(??????, ???????????????, ??????, ??????, ??????)
					//?????? ???(5), ?????????(x%), ???????????? ?????????(x%), ?????????(x%), skip????????????(0.x%),?????????(0.x%), ?????????(0.x%)
					bwchoiTest3(5, 1, 1, 1, 3, 20, 30);
//					bwchoiTest3(5, 1, 1, 1, 3, 20, 50);
					
					/* Set Data Function List End*/
					
					compareTime = Calendar.getInstance().getTimeInMillis() - startTime;
					//bwchoi ?????? ??????
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
			//1000??? ?????? 1??? : 44
			int findCount = 0;
			
			Calendar calendar = CoinHelperUtils.getCalendar();
			calendar.setTime(CoinHelperUtils.getCurrentTime());
			
			//Set UTC Time
			calendar.add(Calendar.HOUR, -9);
			
			while(true)
			{
				try
				{	
					String candleTime = CoinHelperUtils.getTimeToString(calendar.getTime());
					
					setCandleMinMap(market, min, count, candleTime);
					
					calendar.add(Calendar.MINUTE, -(min*count));
					
					if(findCount > 44)
					{
//						updateCandleMinDataDB(market);
						break;
					}
					findCount++;
					//test
//					Thread.sleep(1000);
				}
				catch(Exception e)
				{
//					log.info(String.format("CandleMinRefreshThread Interrupt. market=%s min=%s count=%s \n%s", this.market, this.min, this.count, e.toString()));
					try {
						Thread.sleep(300);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
//						e1.printStackTrace();
					}
				}
			}
		}
	}
}
