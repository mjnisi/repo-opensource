package eu.trade.repo.index.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;

public class PropertyWordObjectMapTest extends BaseTestClass{

	private PropertyWordObjectMap propertyWordObjectMap;
	private static final String WORD = "word";
	private static final Integer WORD_ID = 22;
	private static final Integer OBJECT_ID = 127;
	private static final Integer PROPERTY_ID = 1271;
	private static final Integer OBJ_TYPE_PROPERTY_ID = 52;
	private static final Integer WORD_OBJECT_ID = 99;
	
	
	@Before
	public void setup(){
		propertyWordObjectMap = new PropertyWordObjectMap();
	}
	
	@Test
	public void testGetIdFromTransientItem() throws Exception {
		//item from metadata
		TransientDTO item = new TransientDTO(WORD, 1, 1, 1, OBJECT_ID, PROPERTY_ID, OBJ_TYPE_PROPERTY_ID);
		WordObjectDTO dto = new WordObjectDTO(WORD_OBJECT_ID, WORD_ID, OBJECT_ID, PROPERTY_ID, OBJ_TYPE_PROPERTY_ID, 1);
				
		
		Integer result = propertyWordObjectMap.getIdFromTransientItem(null);
		assertTrue(null == result);
		
		result = propertyWordObjectMap.getIdFromTransientItem(item);
		assertTrue(null == result);
		
		propertyWordObjectMap.put(PROPERTY_ID, dto);
		result = propertyWordObjectMap.getIdFromTransientItem(item);
		assertEquals(dto.getId(), result);
		
	}
}
