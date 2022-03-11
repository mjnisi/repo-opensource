package eu.europa.ec.digit.cmisrepo.test.util;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

public class RepositoryUtil {
	
	public static void main(String[] args) throws Exception {
		String cRepoId = "repo_" + System.currentTimeMillis();
		String cRepoName = "repoName_" + System.currentTimeMillis();
		String cRepoDescription = "repoDesc_" + System.currentTimeMillis();
		createRepositoryLocalhost("localhost", "cmisrepo", "username", "******", cRepoId, cRepoName, cRepoDescription);

		deleteRepositoryLocalhost("localhost", "cmisrepo", "username", "******", cRepoId);
	}
	
	private static HttpClientContext getContext(String hostname, String username, String password) {
		HttpHost targetHost = new HttpHost(hostname, 8080, "http");
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

		AuthCache authCache = new BasicAuthCache();
		authCache.put(targetHost, new BasicScheme());
		HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);
		return context;
	}

	
	public static void createRepositoryLocalhost(String hostname, String repoName, String username, String password, String idRepo, String nameRepo,
			String descriptionRepo) throws Exception {
		HttpClientContext cContext = getContext(hostname, username, password);
		HttpClient client = HttpClientBuilder.create().build();
		String cURI = "http://" + hostname + ":8080/" + repoName + "/admin/createRepository?repositoryId=" + idRepo
                + "&repositoryName=" + nameRepo + "&repositoryDesc=" + descriptionRepo;
		System.out.println(cURI);
		HttpResponse response = client.execute(new HttpGet(cURI), cContext);

		if(response.getStatusLine().getStatusCode() != 200)
			throw new RuntimeException("Was not able to create repository: " + idRepo + ", reason: " + response.getStatusLine().getReasonPhrase());
	}

	public static void deleteRepositoryLocalhost(String hostname, String repoName, String username, String password, String idRepo) throws Exception {
		HttpClientContext cContext = getContext(hostname, username, password);
		HttpClient client = HttpClientBuilder.create().build();
		HttpResponse response = client
				.execute(new HttpGet("http://" + hostname + ":8080/" + repoName + "/admin/deleteConfirmed/" + idRepo), cContext);
		
		if(response.getStatusLine().getStatusCode() != 200)
			throw new RuntimeException("Was not able to delete repository: " + idRepo + ", reason: " + response.getStatusLine().getReasonPhrase());
	}
}
