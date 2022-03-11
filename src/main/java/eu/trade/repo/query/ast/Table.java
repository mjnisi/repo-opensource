package eu.trade.repo.query.ast;

import org.antlr.runtime.CommonToken;

public class Table extends CMISCommonTree {
	
	private final String name;
	private final String alias;
	
	
	public Table(int t, String name, String alias) {
		super(new CommonToken(t));
		this.name = name;
		this.alias = alias;
		
	}

	@Override
	public String getText() {
		return toString();
	}

	@Override
	public String toString() {
		return "[name=" + name + ", alias=" + alias + "]";
	}

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }
}
