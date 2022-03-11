package eu.trade.repo.index.impl.ops;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.index.IndexOperatorFactory;
import eu.trade.repo.index.IndexPartOperator;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;

public class IndexOperatorFactoryTest extends BaseTestClass {
	@Autowired
	private IndexOperatorFactory operatorFactory;

	@Autowired
	private IndexPartOperator contentIndexOperator;

	@Autowired
	private IndexPartOperator metadataIndexOperator;

	@Test
	public void testGetOperator(){
		IndexPartOperator operator = operatorFactory.getOperator(IndexOperationType.CONTENT);
		assertEquals(contentIndexOperator, operator);

		operator = operatorFactory.getOperator(IndexOperationType.METADATA);
		assertEquals(metadataIndexOperator, operator);
	}
}
