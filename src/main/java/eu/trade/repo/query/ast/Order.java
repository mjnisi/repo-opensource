package eu.trade.repo.query.ast;

import org.antlr.runtime.CommonToken;

public class Order extends CMISCommonTree {
	
	private final String qualifier;
	private final String name;
	private final String order;
	
	
	public Order(int t, String qualifier, String name, String order) {
		super(new CommonToken(t));
		
		this.name = name;
		this.qualifier = qualifier;
		this.order = order;
		
	}
	
	public String getName() {
        return name;
    }
	
	public String getOrder() {
		return order != null?order.toLowerCase():null;
	}

	@Override
	public String getText() {
		return toString();
	}

	@Override
	public String toString() {
		return "[qualifier=" + qualifier + ", name=" + name + ", order="
				+ order + "]";
	}
	
}
