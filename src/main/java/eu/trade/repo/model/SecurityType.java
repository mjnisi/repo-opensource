package eu.trade.repo.model;

/**
 * Enum for repository types of security, currently: simple or multiple.
 * <p>
 * Simple means that only one authentication handler and only one authorization handler will be used, and also that the principal IDs will not be prefixed with any domain. <BR/>
 * Multiple means the more than one handler can be used and that the principal IDs will be prefixed with the related domain.
 * 
 * @author porrjai
 */
public enum SecurityType {
	SIMPLE,
	MULTIPLE
}
