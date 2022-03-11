package eu.trade.repo.security.session;

import java.util.List;

public interface SessionInfo {

	List<UserKey> getCurrentSessions();
}
