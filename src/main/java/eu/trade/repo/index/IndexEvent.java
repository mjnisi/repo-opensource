package eu.trade.repo.index;

import org.springframework.context.ApplicationEvent;

public class IndexEvent extends ApplicationEvent{

	private static final long serialVersionUID = 1L;

	public IndexEvent(Object source) {
		super(source);
	}


}
