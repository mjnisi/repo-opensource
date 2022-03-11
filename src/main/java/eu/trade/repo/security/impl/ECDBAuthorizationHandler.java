package eu.trade.repo.security.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.stringtemplate.v4.compiler.CodeGenerator.conditional_return;

public class ECDBAuthorizationHandler extends RestAuthorizationHandler{
	protected Map<String, String> countryISOMap = new HashMap<String , String>();

	/**
	 * @see eu.trade.repo.security.Configurable#init(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void init(Configuration configuration) {
		try {
			List<String> ss = Files.readAllLines(new File(configuration.getString("roleToPrincipalMap")).toPath(), Charset.defaultCharset());
			for (String row : ss) {
				String[] split = row.split(":");
				countryISOMap.put(split[0], split[1]);
			}
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		super.init(configuration);
	}
	
	/**
	 * @see eu.trade.repo.security.AuthorizationHandler#getPrincipalIds(java.lang.String)
	 */
	@Override
	public Set<String> getPrincipalIds(String username) {
    	
		Map<String, Object> variables = new HashMap<String, Object>(1);
        variables.put("username", username);

        try {
        	String[] response = restTemplate.getForObject(endpoint, String[].class, variables);
        	Set<String> countries = new HashSet<String>();
        	
        	for (String country : response) {
        		String countrymap = countryISOMap.get(country);
        		
        		if (countrymap == null) {
    				LOG.error(String.format("Cannot find country map for country %s and user %s", country, username));
    				countries.add("UNKNOWN_"+country);
        			continue;
        		}
				countries.add(countrymap);
			}
            return countries;
        } catch (Exception e) {
        	LOG.error(ERROR + e.getLocalizedMessage(), e);
        	if(emptySetOnError) {
        		return Collections.emptySet();
        	} else {
        		throw new IllegalStateException(ERROR, e);
        	}
        }
	}
}