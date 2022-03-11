package eu.trade.repo.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

/**
 * Project constants.
 */
public final class Constants {

	private Constants() {}

	public static final String CMIS_READ = "cmis:read";
	public static final String CMIS_WRITE = "cmis:write";
	public static final String CMIS_ALL = "cmis:all";

	public static final String PRINCIPAL_ID_ANYONE = "cmis:anyone";
	public static final String PRINCIPAL_ID_ANONYMOUS = "cmis:anonymous";
	public static final String PRINCIPAL_ID_USER = "cmis:user";

	public static final int MODE_CHECKIN = 1;
	public static final int MODE_CHECKOUT = 2;
	public static final int MODE_CHECKOUT_NEW = 3;

	public static final Set<String> CMIS_BASIC_PERMISSIONS = basicPermissions();

	public static final String TYPE_CMIS_FOLDER = BaseTypeId.CMIS_FOLDER.value();
	public static final String TYPE_CMIS_POLICY = BaseTypeId.CMIS_POLICY.value();
	public static final String TYPE_CMIS_DOCUMENT = BaseTypeId.CMIS_DOCUMENT.value();
	public static final String TYPE_CMIS_RELATIONSHIP = BaseTypeId.CMIS_RELATIONSHIP.value();

	public static final String DEFAULT_CREATE_MINOR_VERSIONLABEL ="0.1";
	public static final String DEFAULT_CREATE_MAJOR_VERSIONLABEL ="1.0";
	public static final String DEFAULT_CREATE_NONE_VERSIONLABEL ="n/a";
	public static final String DEFAULT_CREATE_COUT_VERSIONLABEL ="PWC";
	public static final Map<VersioningState, String> DEFAULT_VERSION_LABEL = defaultLables();

	private static Map<VersioningState, String> defaultLables() {
		Map<VersioningState, String> defaultLables = new HashMap<>();
		defaultLables.put(VersioningState.MINOR, DEFAULT_CREATE_MINOR_VERSIONLABEL);
		defaultLables.put(VersioningState.MAJOR, DEFAULT_CREATE_MAJOR_VERSIONLABEL);
		defaultLables.put(VersioningState.NONE, DEFAULT_CREATE_NONE_VERSIONLABEL);
		defaultLables.put(VersioningState.CHECKEDOUT, DEFAULT_CREATE_COUT_VERSIONLABEL);
		return defaultLables;
	}

	public static final String CMIS_QUERY_SCORE = "score";
	public static final String CMIS_SCORE_PROP = "SEARCH_SCORE";

	public static final int CMIS_FETCH_ALL = -1;
	public static final String CMIS_PATH_SEP = "/";
	public static final String CMIS_VERSION = "1.1";
	public static final String CMIS_MULTIVALUE_SEP = ";";

	public static final List<String> CMIS_QUERY_DATETIME_FORMATS = Collections.unmodifiableList(Arrays.asList("yyyy-MM-dd'T'HH:mm:ss.SSS",
		"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
		"yyyy-MM-dd'T'HH:mm:ss.SSS+hh:mm",
		"yyyy-MM-dd'T'HH:mm:ss.SSS-hh:mm"));
	
	
	public static final String NS = "http://ec.europa.eu/trade/repo";

	/* Configuration */
	public static final String PROPNAME_INDEX_DOCUMENT_WORD_LIMIT = "index.documentWordLimit";
	public static final String PROPNAME_INDEX_PAGE_SIZES_TOKEN_SEGMENT = "index.pageSizes.tokenSegment";
	public static final String PROPNAME_INDEX_PAGE_SIZES_WORD_PAGE = "index.pageSizes.wordPage";
	public static final String PROPNAME_INDEX_PAGE_SIZES_WORD_POSITION_PAGE = "index.pageSizes.wordPositionPage";
	public static final String PROPNAME_INDEX_PAGE_SIZES_DELETE_PAGE = "index.pageSizes.deletePage";
	public static final String PROPNAME_INDEX_MAX_ATTEMPTS = "index.background.retryInErrorIndexes.maxAttempts";
	public static final String PROPNAME_INDEX_QUEUE_REMAINING_THRESHOLD = "index.background.retryInErrorIndexes.remainingQueueCapacityThreshold";
	public static final String PROPNAME_INDEX_BACKGROUND_ENABLED = "index.background.enabled";
	public static final String PROPNAME_INDEX_ATOMIC_ENABLED = "index.atomic.enabled";
	public static final String PROPNAME_INDEX_POOL_SMALL_TASKS_QUEUE_CAPACITY = "index.pool.smallTasks.queueCapacity";
	public static final String PROPNAME_INDEX_POOL_LARGE_TASKS_QUEUE_CAPACITY = "index.pool.largeTasks.queueCapacity";
	public static final String PROPNAME_INDEX_POOL_METADATA_TASKS_QUEUE_CAPACITY = "index.pool.metadataTasks.queueCapacity";
	public static final String PROPNAME_INDEX_POOL_SELECTION_LIMIT_SIZE = "index.pool.selection.limitSize";


	public static final String PROPNAME_PRODUCT_MAX_CONTENT_SIZE = "product.maxContentSize";

	public static final int DEFAULT_DOCUMENT_WORD_LIMIT = 10000;
	public static final int DEFAULT_TOKEN_SEGMENT_SIZE = 1000;
	public static final int DEFAULT_WORD_PAGE_SIZE = 1000;
	public static final int DEFAULT_WORD_POSITION_PAGE_SIZE = 10000;
	public static final int DEFAULT_DELETE_PAGE_SIZE = 1000;
	public static final int DEFAULT_MAX_ATTEMPTS = 3;
	public static final double DEFAULT_QUEUE_REMAINING_THRESHOLD = 0.20;
	public static final boolean DEFAULT_BACKGROUND_ENABLED = false;
	public static final boolean DEFAULT_ATOMIC_ENABLED = false;
	//100 KB
	public static final int DEFAULT_POOL_SELECTION_LIMIT_SIZE = 102400;

	public static final int FIVE_MINUTES_IN_MILLIS = 300000;
	public static final int TEN_MINUTES_IN_MILLIS = 600000;
	public static final int FIFTEEN_MINUTES_IN_MILLIS = 900000;

	public static final String BUILTIN = "builtin";

	public enum ORDER_DIR {
		ASC("asc"), DESC("desc");

		private final String order;

		ORDER_DIR(String order) {
			this.order = order;
		}

		public String getOrder() {
			return this.order;
		}

		@Override
		public String toString() {
			return this.order;
		}
	}

	// Operations base security.

	public static final String REPO_ROL_PREFIX = "REPO.";
	public static final String CREATE_REPO = REPO_ROL_PREFIX + "CreateRepo";
	public static final String DELETE_REPO = REPO_ROL_PREFIX + "DeleteRepo";
	public static final String CHANGE_REPO_CAPABILITIES = REPO_ROL_PREFIX + "ChangeRepoCapabilities";
	public static final String CHANGE_REPO_SECURITY = REPO_ROL_PREFIX + "ChangeRepoSecurity";
	public static final String VIEW_REPO_SESSIONS = REPO_ROL_PREFIX + "ViewRepoSessions";
	public static final String VIEW_REPO_SUMMARY = REPO_ROL_PREFIX + "ViewRepoSummary";
	public static final String CHANGE_REPO_MAPPINGS = REPO_ROL_PREFIX + "ChangeRepoMappings";
	public static final String CHANGE_REPO_PERMISSIONS = REPO_ROL_PREFIX + "ChangeRepoPermissions";
	public static final String[] ADMIN_OPS = {CREATE_REPO, DELETE_REPO, CHANGE_REPO_CAPABILITIES, CHANGE_REPO_SECURITY, VIEW_REPO_SESSIONS, VIEW_REPO_SUMMARY};
	public static final String CAPABILITY_NOT_SUPPORTED_BY_THE_REPOSITORY = "Capability not supported by the repository";

	private static Set<String> basicPermissions() {
		Set<String> basicPermissions = new HashSet<>();
		basicPermissions.add(CMIS_READ);
		basicPermissions.add(CMIS_WRITE);
		basicPermissions.add(CMIS_ALL);
		return basicPermissions;
	}

	public static final List<BaseTypeId> BASE_TYPE_CMIS_11 = Collections.unmodifiableList(Arrays.asList(BaseTypeId.CMIS_DOCUMENT,
			BaseTypeId.CMIS_FOLDER,
			BaseTypeId.CMIS_RELATIONSHIP,
			BaseTypeId.CMIS_POLICY,
			BaseTypeId.CMIS_ITEM,
			BaseTypeId.CMIS_SECONDARY));
	public static final List<BaseTypeId> BASE_TYPE_CMIS_10 = Collections.unmodifiableList(Arrays.asList(BaseTypeId.CMIS_DOCUMENT,
			BaseTypeId.CMIS_FOLDER,
			BaseTypeId.CMIS_RELATIONSHIP,
			BaseTypeId.CMIS_POLICY));

	// Exceptions response codes
	public static final BigInteger SC_UNAUTHORIZED = BigInteger.valueOf(HttpServletResponse.SC_UNAUTHORIZED);
	
	
	public static final String QUERY_CASE_SENSITIVE = "queryCaseSensitive";
	public static final String PERCENT_WILDCARD = "%";
}
