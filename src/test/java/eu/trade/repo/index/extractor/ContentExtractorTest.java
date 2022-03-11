package eu.trade.repo.index.extractor;

import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseLobTestClass;
import eu.trade.repo.delegates.JDBCLobDelegate;

public class ContentExtractorTest extends BaseLobTestClass {

	@Autowired
	JDBCLobDelegate lobDelegate;
	
	@Ignore
	@Test
	public void testExtractContent() throws Exception {
		//TODO
		
		//Probar que recoge el stream y contar el numero de tokens esperados
	}

	@Ignore
	@Test (expected=CmisStreamNotSupportedException.class)
	public void testExtractContentFromInputStreamCorrupted() throws Exception {
		//TODO
	}

	
	
}
