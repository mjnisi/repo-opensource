package eu.trade.repo.test.util;

import java.util.Date;

import com.ibm.icu.util.Calendar;

import eu.trade.repo.util.ICurrentDate;

public class TestCurrentDate implements ICurrentDate {

	private Date date;
	
	public TestCurrentDate() {
		reset();
	}
	
	@Override
	public Date getDate() {
		Date d = new Date(date.getTime());
		increment();
		return d;
	}

	@Override
	public void reset() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0);
		date = c.getTime();
	}
	
	private void increment() {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, 1);
		date = c.getTime();
	}

}
