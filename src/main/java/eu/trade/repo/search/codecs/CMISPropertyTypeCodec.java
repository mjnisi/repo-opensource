package eu.trade.repo.search.codecs;

public interface CMISPropertyTypeCodec {
	
	String UNCHECKED = "unchecked";
	
	<T,Z> T encode(Z propertyValue);
	<T,Z> T normalize(Z propertyValue);
	<T,Z> T decode(Z propertyValue);
}
