<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xslthl="http://xslthl.sf.net"
    exclude-result-prefixes="xslthl"
    version="1.0">

  <xsl:import href="urn:docbkx:stylesheet"/>

<xsl:param name="section.autolabel">1</xsl:param>
<xsl:param name="body.start.indent">0pt</xsl:param>
<xsl:param name="alignment">left</xsl:param>

<xsl:attribute-set name="section.level1.properties">
<xsl:attribute name="break-before">page</xsl:attribute>
</xsl:attribute-set>
  
</xsl:stylesheet>