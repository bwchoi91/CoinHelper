package coinhelper.service;

import java.util.List;

import coinhelper.object.Coin;

import com.google.common.collect.Lists;

public class DataService {

	public static DataService dataService;
	
	public List<Coin> coinList = Lists.newArrayList();
	
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
		
	}
}
