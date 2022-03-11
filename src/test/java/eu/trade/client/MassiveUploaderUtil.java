package eu.trade.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.util.FileUtils;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;

public class MassiveUploaderUtil extends AbstractSessionTest {

	public static final String PARAMETER_TARGET_FOLDER = "massive.uploader.target.folder";
	public static final String DEFAULT_TARGET_FOLDER = "/";
	public static final String PARAMETER_LOCAL_FOLDERS = "massive.uploader.local.folders";
	public static final String PARAMETER_LOG_FILE = "massive.uploader.log.file";
	public static final String DEFAULT_LOG_FILE = "uploader.log";
	public static final String PARAMETER_SAFE_UPLOAD = "massive.uploader.safe.upload";
	public static final String DEFAULT_SAFE_UPLOAD = "false";

	private String targetFolderPath;
	private String[] localFolderPaths;
	private String folderType;
	private String documentType;
	private String logFile;
	private boolean safeUpload;

	@Override
	public void init(Map<String, String> parameters) {
		super.init(parameters);
		setName("Upload massively a folder");
		setDescription("Uploads a local folder completely to the repository folder specified in the parameters file.");
		localFolderPaths = parameters.get(PARAMETER_LOCAL_FOLDERS).split(",");
		targetFolderPath = get(parameters, PARAMETER_TARGET_FOLDER, DEFAULT_TARGET_FOLDER);
		folderType = get(parameters, TestParameters.DEFAULT_FOLDER_TYPE, TestParameters.DEFAULT_FOLDER_TYPE_VALUE);
		documentType = get(parameters, TestParameters.DEFAULT_DOCUMENT_TYPE, TestParameters.DEFAULT_DOCUMENT_TYPE_VALUE);
		logFile = get(parameters, PARAMETER_LOG_FILE, DEFAULT_LOG_FILE);
		String safeUploadParam = get(parameters, PARAMETER_SAFE_UPLOAD, DEFAULT_SAFE_UPLOAD);
		safeUpload = Boolean.parseBoolean(safeUploadParam);
	}

	private String get(Map<String, String> parameters, String key, String defaultValue) {
		String value = parameters.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	@Override
	public void run(Session session) {
		CmisObject cmisObject = session.getObjectByPath(targetFolderPath);
		if (!(cmisObject instanceof Folder)) {
			throw new IllegalArgumentException("Target folder not found: " + targetFolderPath);
		}
		Folder parent = (Folder) cmisObject;
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(logFile))) {
			if (localFolderPaths.length == 1) {
				uploadSingle(writer, session, parent, localFolderPaths[0]);
			}
			else {
				uploadMultiple(writer, session, parent);
			}
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Log file cannot be oppened.", e);
		} catch (IOException e) {
			throw new IllegalStateException("Error writing log file.", e);
		}
	}

	private void uploadMultiple(Writer writer, Session session, Folder parent) throws IOException {
		for (String localFolderPath : localFolderPaths) {
			File folder = new File(localFolderPath);
			if (folder.isDirectory()) {
				Folder newFolder = createFolder(session, parent, folder);
				upload(writer, session, newFolder, folder);
			} else {
				writeLn(writer, "Local folder is not a directory: " + localFolderPaths);
			}
		}
	}

	private void uploadSingle(Writer writer, Session session, Folder parent, String localFolderPath) throws IOException {
		File folder = new File(localFolderPath);
		if (folder.isDirectory()) {
			upload(writer, session, parent, folder);
		} else {
			writeLn(writer, "Local folder is not a directory: " + localFolderPaths);
		}
	}

	private void upload(Writer writer, Session session, Folder parent, File folder) throws IOException {
		for (File child : folder.listFiles()) {
			writeLn(writer, "Start to upload: " + child.getAbsolutePath() + " at: " + new Timestamp(System.currentTimeMillis()));

			try {
				if(child.isFile()) {
					long length = child.length();
					if (length <= 0 || length >= 1073741824) {
						writeLn(writer, "Skipping file. Size: " + length);
					}
					else {
						createDocument(session, parent, child);
					}
				}
				else if(child.isDirectory()) {
					Folder newFolder = createFolder(session, parent, child);
					upload(writer, session, newFolder, child);
				}
				writeLn(writer, child.getAbsolutePath() + " completely uploaded at: " + new Timestamp(System.currentTimeMillis()));
			} catch (Exception e) {
				writeLn(writer, "File to upload file: " + child.getAbsolutePath() + " at: " + new Timestamp(System.currentTimeMillis()) + ". Reason: " + e.getLocalizedMessage()) ;
			}
			writer.flush();
		}
	}

	private void createDocument(Session session, Folder parent, File child) throws FileNotFoundException {
		if (safeUpload) {
			CmisObject cmisObject = null;
			try {
				cmisObject = session.getObjectByPath(parent.getPath() + "/" + child.getName());
			} catch (CmisObjectNotFoundException e) {
			}
			if (cmisObject == null) {
				FileUtils.createDocumentFromFile(parent.getId(), child, documentType, VersioningState.MAJOR, session);
			}
		}
		else {
			FileUtils.createDocumentFromFile(parent.getId(), child, documentType, VersioningState.MAJOR, session);
		}
	}

	private Folder createFolder(Session session, Folder parent, File folder) {
		Folder newFolder = null;
		if (safeUpload) {
			CmisObject cmisObject = null;
			try {
				cmisObject = session.getObjectByPath(parent.getPath() + "/" + folder.getName());
			} catch (CmisObjectNotFoundException e) {
			}

			if (cmisObject instanceof Folder) {
				newFolder = (Folder) cmisObject;
			}
			else {
				newFolder = FileUtils.createFolder(parent, folder.getName(), folderType);
			}
		}
		else {
			newFolder = FileUtils.createFolder(parent, folder.getName(), folderType);
		}
		return newFolder;
	}

	private void writeLn(Writer writer, String line) throws IOException {
		writer.write(line);
		writer.write("\n");
	}
}
