package eu.trade.repo.index.triggers.handlers;
public class ApplicationContextProvider implements ApplicationContextAware{

		return ApplicationContextWrapper.getCtx();
	}
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		ApplicationContextWrapper.setCtx(ctx);
	}
}