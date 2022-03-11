package eu.trade.repo.util;

import java.util.Date;

public class CurrentDate implements ICurrentDate{

	public Date getDate(){
		return new Date();
	}

	@Override
	public void reset() {
	}
	
}
