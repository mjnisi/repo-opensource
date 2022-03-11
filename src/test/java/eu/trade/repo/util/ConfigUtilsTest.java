package eu.trade.repo.util;

import static org.mockito.Mockito.when;
import static org.mockito.Matchers.*;
import static junit.framework.Assert.*;

import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ConfigUtilsTest {

	@Mock
	private Configuration config;
	
	private static final String PROP_NAME = "testPropertyName";
	
	@Before
	public void initMocks() { 
		MockitoAnnotations.initMocks(this); 
	}
	
	@Test
	public void testGetIntPositive() throws Exception {
		
		when(config.getInt(anyString(), anyInt())).thenReturn(23);
		int result = ConfigUtils.getIntPositive(config, PROP_NAME, 2);
		assertEquals(23, result);
		
		when(config.getInt(anyString(), anyInt())).thenReturn(-23);
		result = ConfigUtils.getIntPositive(config, PROP_NAME, 2);
		assertEquals(2, result);
	}
	
	@Test
	public void testGetPercentage() throws Exception {
		
		when(config.getDouble(anyString(), anyDouble())).thenReturn(23.0);
		double result = ConfigUtils.getPercentage(config, PROP_NAME, 0.2);
		assertEquals(0.2, result);
		
		when(config.getDouble(anyString(), anyDouble())).thenReturn(0.8);
		result = ConfigUtils.getPercentage(config, PROP_NAME, 0.2);
		assertEquals(0.8, result);
		
		when(config.getDouble(anyString(), anyDouble())).thenReturn(-23.0);
		result = ConfigUtils.getPercentage(config, PROP_NAME, 0.2);
		assertEquals(0.2, result);
		
		when(config.getDouble(anyString(), anyDouble())).thenReturn(-23.0);
		result = ConfigUtils.getPercentage(config, PROP_NAME, 5.0);
		assertEquals(0.0, result);
	}

}
