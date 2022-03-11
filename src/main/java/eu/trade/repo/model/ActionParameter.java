package eu.trade.repo.model;

/**
 * CMIS permission keys are defined as a prefix {@link Action} and a suffix that defines the action parameter:
 * Action.CAN_DELETE_OBJECT - Object
 * <p>
 * This enum defines the set of possibles action parameters.
 * 
 * @author porrjai
 */
public enum ActionParameter {

	DEFAULT,
	FOLDER,
	OBJECT,
	SOURCE,
	TARGET,
	DOCUMENT,
	POLICY,
	VERSION_SERIES
}
