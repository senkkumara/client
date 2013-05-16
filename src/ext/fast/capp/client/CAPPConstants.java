/**
 * CAPP exchange xml file key definition
 */
package ext.fast.capp.client;

/**
 * Created on 2005-7-20
 * 
 * @author liuld
 */
public interface CAPPConstants {
	// Top level elements
	public static final String CAPPDATA = "CAPPDATA";

	// 2nd level elements
	public static final String CAPP = "CAPP";
	public static final String PART = "PART";
	public static final String BOMITEM = "BOMITEM";
	public static final String DOCUMENT = "DOCUMENT";
	public static final String MARKUP = "MARKUP";
	public static final String SIGNATURE = "SIGNATURE";
	public static final String WORKITEM = "WORKITEM";
	public static final String DIR_NODE = "DIR_NODE";
	public static final String RESULT = "RESULT";
	public static final String MESSAGE = "MESSAGE";
	public static final String EXCEPTION = "EXCEPTION";

	// 3rd level elements
	public static final String METADATA = "METADATA";

	// attribute names
	public static final String TYPE = "TYPE"; // CAPP
	public static final String NAME = "NAME"; // METADATA
	public static final String VALUE = "VALUE"; // METADATA

	// attribute value - constants
	public static final String SERVER_URL = "SERVER_URL";
	public static final String WINDOW_CAPTION = "WINDOW_CAPTION";
	public static final String VIEW = "VIEW";
	public static final String IBA_LIST = "IBA_LIST"; // 请求IBA属性名列表
	public static final String MULTIPLE_SELECTABLE = "MULTIPLE_SELECTABLE";
	public static final String GET_BOM_LIST = "GET_BOM_LIST"; // 是否获取BOM清单
	public static final String PRODUCT_NAME = "PRODUCT_NAME";
	public static final String PRODUCT_NUMBER = "PRODUCT_NUMBER";
	public static final String DOWNLOAD = "DOWNLOAD";
	public static final String PART_NAME = "PART_NAME";
	public static final String PART_NUMBER = "PART_NUMBER";
	public static final String PART_VERSION = "PART_VERSION";
	public static final String DOC_NAME = "DOC_NAME";
	public static final String DOC_NUMBER = "DOC_NUMBER";
	public static final String DOC_VERSION = "DOC_VERSION";
	public static final String DOC_TYPE = "DOC_TYPE";
	public static final String PRIMARY_FILE = "PRIMARY_FILE";
	public static final String SECONDARY_FILE = "SECONDARY_FILE";
	public static final String RELATED_PART_NAME = "RELATED_PART_NAME";
	public static final String RELATED_PART_NUMBER = "RELATED_PART_NUMBER";
	public static final String RELATED_PART_VIEW = "RELATED_PART_VIEW";
	public static final String RELATED_PART_VERSION = "RELATED_PART_VERSION";
	public static final String RELATED_DOC_NUMBER = "RELATED_DOC_NUMBER";
	public static final String RELATED_DOC_VERSION = "RELATED_DOC_VERSION";
	public static final String IBA = "IBA"; // 检入时要设定的IBA属性ArrayList
	public static final String CHECKIN_COMMENTS = "CHECKIN_COMMENTS";
	public static final String OBJECT_URL = "OBJECT_URL";
	public static final String GET_URL_ONLY = "GET_URL_ONLY"; // 不要打开浏览器
	public static final String LATEST_VERSION_ONLY = "LATEST_VERSION_ONLY";

	public static final String NUMBER = "NUMBER";
	public static final String VERSION = "VERSION";
	public static final String PARENT_NUMBER = "PARENT_NUMBER";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String MODIFYSTAMP = "MODIFYSTAMP";
	public static final String MARKUP_NAME = "MARKUP_NAME";
	public static final String ROLE = "ROLE";
	public static final String ACTIVITY = "ACTIVITY";
	public static final String SIGNER = "SIGNER";
	public static final String COMMENTS = "COMMENTS";
	public static final String DATE = "DATE";
	public static final String PROMPT = "PROMPT";

	public static final String CAPP_TASK_NUMBER = "CAPP_TASK_NUMBER";
	public static final String BASELINE_NUMBER = "BASELINE_NUMBER";

	public static final String TRUE = "TRUE";
	public static final String FALSE = "FALSE";
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";

	// capp call types
	public static final String SEARCH_PART = "SEARCH_PART";
	public static final String SEARCH_DOCUMENT = "SEARCH_DOCUMENT";
	public static final String CHECKOUT_DOCUMENT = "CHECKOUT_DOCUMENT";
	public static final String DELETE_DOCUMENT = "DELETE_DOCUMENT";
	public static final String REVISE_DOCUMENT = "REVISE_DOCUMENT";
	public static final String QUICK_GET_PART = "QUICK_GET_PART";
	public static final String QUICK_GET_DOCUMENT = "QUICK_GET_DOCUMENT";
	public static final String QUICK_CHECKOUT_DOCUMENT = "QUICK_CHECKOUT_DOCUMENT";
	public static final String QUICK_REVISE_DOCUMENT = "QUICK_REVISE_DOCUMENT";
	public static final String QUICK_DELETE_DOCUMENT = "QUICK_DELETE_DOCUMENT";
	public static final String CHECKIN_DOCUMENT = "CHECKIN_DOCUMENT";
	public static final String LOAD_DOCUMENT = "LOAD_DOCUMENT";
	public static final String SAVE_MARKUP = "SAVE_MARKUP";
	public static final String LIST_MARKUP = "LIST_MARKUP";
	public static final String GET_MARKUP = "GET_MARKUP";
	public static final String LIST_DIR = "LIST_DIR";
	public static final String LIST_WORKITEMS = "LIST_WORKITEMS";
	public static final String OPEN_WORKITEM_PAGE = "OPEN_WORKITEM_PAGE";
	public static final String OPEN_PART_PAGE = "OPEN_PART_PAGE";
	public static final String OPEN_DOCUMENT_PAGE = "OPEN_DOCUMENT_PAGE";
	public static final String GET_DOCUMENT_SIGNATURES = "GET_DOCUMENT_SIGNATURES";
	public static final String VERIFY_AUTHORIZATION = "VERIFY_AUTHORIZATION";
	public static final String GET_TOOLING_NUMBER = "GET_TOOLING_NUMBER";
	public static final String LIST_PART_BASELINES = "LIST_PART_BASELINES";
	public static final String LIST_PART_DOCUMENTS = "LIST_PART_DOCUMENTS";
	public static final String SET_DOCUMENT_IN_CAPP_TASK = "SET_DOCUMENT_IN_CAPP_TASK";

	// internal call types
	public static final String SEARCH_PRODUCT = "SEARCH_PRODUCT";
	public static final String DOWNLOAD_DOCUMENT = "DOWNLOAD_DOCUMENT";
	public static final String GET_TYPE_INFO = "GET_TYPE_INFO";
	// 被检出文档的原始信息
	public static final String PREPARE_CHECKIN_DOCUMENT = "PREPARE_CHECKIN_DOCUMENT";

	// internal attribute name/values
	public static final String DOC_NEW = "DOC_NEW"; // 检入是否新建文档
	public static final String PRODUCT = "PRODUCT";
	public static final String ITERATION = "ITERATION";
	public static final String CHECKOUT_STATUS = "CHECKOUT_STATUS";
	public static final String LIFECYCLE_STATUS = "LIFECYCLE_STATUS";
	public static final String STATE = "STATE";
	public static final String LAST_MODIFIED = "LAST_MODIFIED";
	public static final String OID = "OID";
	public static final String PARENT_OID = "PARENT_OID";
	public static final String LOCATION = "LOCATION";
	public static final String DESCRIBES = "DESCRIBES";
	public static final String IBA_DEFINITIONS = "IBA_DEFINITIONS";
	public static final String CREATOR = "CREATOR";

	// other constants
	public static final String PARAMS = "PARAMS";
	public static final String FILES = "FILES";
	public static final String TEST = "TEST";
	public static final String USER = "USER";
	public static final String PASS = "PASS";
	public static final String USER_AUTHORIZATION_FAIL = "用户登录失败!";

	// work item types
	public static final String WORKITEM_CAPP_TASK = "WORKITEM_CAPP_TASK";
	public static final String WORKITEM_CAPP_DOCUMENT = "WORKITEM_CAPP_DOCUMENT";
	public static final String WORKITEM_UNKNOWN = "WORKITEM_UNKNOWN";
	public static final String PBO_NAME = "PBO_NAME";
	public static final String PBO_NUMBER = "PBO_NUMBER";
	public static final String PBO_VERSION = "PBO_VERSION";
	public static final String PBO_ENDITEM_CODE = "PBO_ENDITEM_CODE";
	public static final String PBO_DESCRIPTION = "PBO_DESCRIPTION";

	// virtual dir node
	public static final String NODE_CLASS = "NODE_CLASS";
	public static final String NODE_TYPE = "NODE_TYPE";
	public static final String NODE_NAME = "NODE_NAME";
	public static final String NODE_NUMBER = "NODE_NUMBER";
	public static final String NODE_VERSION = "NODE_VERSION";

	// bom item
	public static final String LEVEL = "LEVEL";
	public static final String QUANTITY = "QUANTITY";
	public static final String CAPP_DOCUMENTS = "CAPP_DOCUMENTS";
	public static final String IBAS = "IBAS";

	// 工装申请卡取号
	public static final String CLASS_NUMBER = "CLASS_NUMBER";
	public static final String TOOLING = "TOOLING";

	// virtual dir node class
	public static final String ROOT = "ROOT";
	public static final String FOLDER = "FOLDER";
	// public static final String PRODUCT = "PRODUCT";
	// public static final String PART = "PART";
	// public static final String DOCUMENT = "DOCUMENT";
	public static final String BASELINE = "BASELINE";

	// 工艺统计
	public static final String REPORT = "REPORT";
	public static final String DATE_FROM = "DATE_FROM";
	public static final String DATE_TO = "DATE_TO";

	// IBA属性定义名键值, SoftType类型名称
	public static final String TYPE_NAME_LIST = "TYPE_NAME_LIST";
	public static final String TYPE_NAME_INFO_MAP = "TYPE_NAME_INFO_MAP";
	public static final String TYPE_NAME = "TYPE_NAME";
	public static final String TYPE_ID = "TYPE_ID";
	public static final String TYPE_BRANCH_ID = "TYPE_BRANCH_ID";
	public static final String TYPE_IBA = "TYPE_IBA"; // 文档小类属性名称或值
	public static final String TYPE_IBA_LIST = "TYPE_IBA_LIST";
	public static final String TYPE_IBA_OPTIONS = "TYPE_IBA_OPTIONS";//软性离散值
	public static final String IBA_NAME = "IBA_NAME";
	public static final String IBA_DISPLAY = "IBA_DISPLAY";
	public static final String IBA_NAME_DISP_MAP = "IBA_NAME_DISP_MAP";
	public static final String AOFO_EFFECTIVITY_IBA = "AOFO_EFFECTIVITY_IBA";
	public static final String FO_UPDATE_PART_IBA_LIST = "FO_UPDATE_PART_IBA_LIST";
	public static final String NONSIGNING_ACTIVITY_KEYWORDS = "NONSIGNING_ACTIVITY_KEYWORDS";
}