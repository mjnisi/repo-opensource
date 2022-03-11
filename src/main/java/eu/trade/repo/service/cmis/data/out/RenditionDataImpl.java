/**
 * 
 */
package eu.trade.repo.service.cmis.data.out;

import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.data.RenditionData;

import eu.trade.repo.model.Rendition;

/**
 * {@link RenditionData} implementation
 * 
 * @author porrjai
 */
public class RenditionDataImpl extends NonExtensionsObject implements RenditionData {

	private final Rendition rendition;


	/**
	 * New instance
	 * 
	 * @param rendition {@link Rendition} the rendition.
	 */
	public RenditionDataImpl(Rendition rendition) {
		this.rendition = rendition;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RenditionData#getStreamId()
	 */
	@Override
	public String getStreamId() {
		return rendition.getStreamId();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RenditionData#getMimeType()
	 */
	@Override
	public String getMimeType() {
		return rendition.getMimeType();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RenditionData#getBigLength()
	 */
	@Override
	public BigInteger getBigLength() {
		return BigInteger.valueOf(rendition.getLength());
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RenditionData#getKind()
	 */
	@Override
	public String getKind() {
		return rendition.getKind();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RenditionData#getTitle()
	 */
	@Override
	public String getTitle() {
		return rendition.getTitle();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RenditionData#getBigHeight()
	 */
	@Override
	public BigInteger getBigHeight() {
		return BigInteger.valueOf(rendition.getHeight());
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RenditionData#getBigWidth()
	 */
	@Override
	public BigInteger getBigWidth() {
		return BigInteger.valueOf(rendition.getWidth());
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RenditionData#getRenditionDocumentId()
	 */
	@Override
	public String getRenditionDocumentId() {
		return rendition.getRenditionDocumentId();
	}
}