package eu.trade.repo.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to denote a method as a CMIS secured action but based on custom logic.
 * <p>
 * {@link CustomAction#LOGIN} means no real action, only the need to be authenticated. See {@link eu.trade.repo.service.cmis.CmisRepositoryService}
 * 
 * @see ApplyTo
 * 
 * @author porrjai
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface CustomSecured {

	enum CustomAction {
		CREATE_DOCUMENT_FROM_SOURCE,
		LOGIN,
		ADMIN
    }

	CustomAction value();
}
