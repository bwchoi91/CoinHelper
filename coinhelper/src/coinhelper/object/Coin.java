package coinhelper.object;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Coin {
	public String market;
	public String koreanName;
	public String englishName;
	
	@ConstructorProperties({"market", "korean_name", "english_name"})
	public Coin(String market, String koreanName, String englishName)
	{
		this.market = market;
		this.koreanName = koreanName;
		this.englishName = englishName;
	}
}
