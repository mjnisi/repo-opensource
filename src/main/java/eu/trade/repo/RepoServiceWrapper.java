package eu.trade.repo;

import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.CmisServiceWrapperManager;
import org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper;
//import org.apache.chemistry.opencmis.server.support.wrapper.CmisServiceWrapperManager;
//import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;

public class RepoServiceWrapper extends ConformanceCmisServiceWrapper {

	private static final int MAX_ID_LENGTH = 100;

	public RepoServiceWrapper(CmisService service,
			BigInteger defaultTypesMaxItems, BigInteger defaultTypesDepth,
			BigInteger defaultMaxItems, BigInteger defaultDepth) {
		super(service, defaultTypesMaxItems, defaultTypesDepth, defaultMaxItems,
				defaultDepth);
	}

	/* (non-Javadoc)
	 * @see org.apache.chemistry.opencmis.server.support.CmisServiceWrapper#checkId(java.lang.String, java.lang.String)
	 */
	@Override
	protected void checkId(String name, String id) {
        if (id == null) {
            throw new CmisInvalidArgumentException(name + " must be set!");
        }

        if (id.length() == 0) {
            throw new CmisInvalidArgumentException(name + " must not be empty!");
        }
        
        if (id.length() > MAX_ID_LENGTH) {
            throw new CmisInvalidArgumentException(name + " is invalid!");
        }
    }

	/* (non-Javadoc)
	 * @see org.apache.chemistry.opencmis.server.support.CmisServiceWrapper#checkIds(java.lang.String, java.lang.String[])
	 */
	@Override
	protected void checkIds(String name, String... ids) {
        for (String id : ids) {
            if (id != null && id.length() > 0 && id.length() < MAX_ID_LENGTH) {
                return;
            }
        }

        throw new CmisInvalidArgumentException(name + " must be set!");
	}

}
