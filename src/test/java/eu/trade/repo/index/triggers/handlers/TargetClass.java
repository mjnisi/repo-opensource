package eu.trade.repo.index.triggers.handlers;

import eu.trade.repo.index.triggers.IndexParamName;
import eu.trade.repo.index.triggers.StreamChangeType;
import eu.trade.repo.index.triggers.annotation.RegisterStreamChange;
import eu.trade.repo.index.triggers.annotation.StreamChangeParam;
import eu.trade.repo.index.triggers.annotation.TriggerIndex;

public class TargetClass {


	@TriggerIndex
	public void methodToTriggerIndex(String repositoryId){

	}

	@RegisterStreamChange(StreamChangeType.INSERT)
	public void methodToListenStreamChanges(@StreamChangeParam(IndexParamName.OBJECT_ID)final Integer objectId, @StreamChangeParam(IndexParamName.STREAM_SIZE)int inSize){

	}
}
