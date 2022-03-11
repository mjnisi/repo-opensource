package eu.trade.repo.query.ast;

import org.antlr.runtime.CommonToken;

public class Column extends CMISCommonTree {
	
	private final String name;
	private final String qualifier;
	private final String alias;
	private final String function;
	
	
	public Column(int t, String name, String qualifier, String alias, String function) {
		super(new CommonToken(t));
		
		this.name = name;
		this.qualifier = qualifier;
		this.alias = alias;
		this.function = function;
		
	}

	@Override
	public String getText() {
		return toString();
	}

	@Override
	public String toString() {
		return "[name=" + name + ", qualifier=" + qualifier + ", alias="
				+ alias + ", function=" + function + "]";
	}

    public String getName() {
        return name;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getAlias() {
        return alias;
    }

    public String getFunction() {
        return function;
    }
}
