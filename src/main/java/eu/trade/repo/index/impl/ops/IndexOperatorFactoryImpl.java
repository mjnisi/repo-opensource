package eu.trade.repo.index.impl.ops;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.trade.repo.index.IndexOperatorFactory;
import eu.trade.repo.index.IndexPartOperator;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;

public class IndexOperatorFactoryImpl  implements IndexOperatorFactory{

	private final Map<IndexOperationType, IndexPartOperator> operatorMap;
	
	public IndexOperatorFactoryImpl(List<IndexPartOperator> operatorList){
		operatorMap = new HashMap<IndexOperationType, IndexPartOperator>();
		if(null != operatorList){
			for (IndexPartOperator operator : operatorList) {
				operatorMap.put(operator.getOperationTypeSupported(), operator);
			}
		}
	}
	
	public IndexPartOperator getOperator(IndexOperationType operationType){
		return operatorMap.get(operationType);
	}
}
