package eu.trade.repo.index.triggers.handlers;import org.springframework.beans.BeansException;import org.springframework.context.ApplicationContext;import org.springframework.context.ApplicationContextAware;
public class ApplicationContextProvider implements ApplicationContextAware{
	public static ApplicationContext getApplicationContext() {
		return ApplicationContextWrapper.getCtx();
	}
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		ApplicationContextWrapper.setCtx(ctx);
	}	private static final class ApplicationContextWrapper {		private static ApplicationContext ctx = null;		public static ApplicationContext getCtx() {			return ctx;		}		public static void setCtx(ApplicationContext ctx) {			ApplicationContextWrapper.ctx = ctx;		}	}
}
