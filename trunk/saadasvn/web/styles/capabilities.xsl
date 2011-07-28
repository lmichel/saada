<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:vosi="http://www.ivoa.net/xml/VOSICapabilities/v1.0" xmlns:fn="http://www.w3.org/2005/xpath-functions/#">
	<xsl:output method="html" media-type="text/html" encoding="UTF-8" version="4.0" />
	
	<xsl:template match="/">
		<html>
			<head>
				<xsl:element name="link">
					<xsl:attribute name="rel">stylesheet</xsl:attribute>
					<xsl:attribute name="type">text/css</xsl:attribute>
					<xsl:attribute name="href">../styles/generic.css</xsl:attribute>
				</xsl:element>
				<xsl:element name="link">
					<xsl:attribute name="rel">stylesheet</xsl:attribute>
					<xsl:attribute name="type">text/css</xsl:attribute>
					<xsl:attribute name="href">../styles/tap.css</xsl:attribute>
				</xsl:element>
				<title>Saada - TAP Asynchronous Service</title>
			</head>
			<body style="margin:3px;">			
				<div id="header">
					<xsl:element name="img">
						<xsl:attribute name="alt">SAADA image</xsl:attribute>
						<xsl:attribute name="src">../images/saadatransp-text.gif</xsl:attribute>
					</xsl:element>
					<h1>Saada - TAP Asynchronous Service</h1>
				</div>
				<hr style="clear:both;margin-top:1em;" />
				<div id="tapPath"><a href="../tap">TAP service</a>-&gt;<b>Capabilities</b></div>
				
				<h1 id="pageTopic">TAP service capabilities</h1>
				<xsl:apply-templates select="vosi:capabilities | capabilities" />
				
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="vosi:capabilities | capabilities">
		<xsl:for-each select="vosi:capability | capability">
			<div class="section">
				<h1><xsl:value-of select="@standardID" /></h1> 
				<xsl:if test="vosi:validationLevel or validationLevel">
					<p><b>Validation levels: </b>
						<xsl:for-each select="vosi:validationLevel | validationLevel"><xsl:value-of select="." /></xsl:for-each>
					</p>
				</xsl:if>
				<xsl:for-each select="vosi:description | description">
					<p><b>Description: </b><xsl:value-of select="." /></p>
				</xsl:for-each>
				<xsl:for-each select="vosi:interface | interface">
					<p><b>Possible access with: </b>
						<ul>
							<xsl:for-each select="vosi:accessURL | accessURL">
								<li>
									<xsl:choose>
										<xsl:when test="@use != 'full'"><xsl:value-of select="." /> (use:<xsl:value-of select="@use" />)</xsl:when>
										<xsl:otherwise>
											<xsl:element name="a">
												<xsl:attribute name="class">tapResource</xsl:attribute>
												<xsl:attribute name="href"><xsl:value-of select="." /></xsl:attribute>
												<xsl:value-of select="." />
											</xsl:element>
										</xsl:otherwise>
									</xsl:choose>
								</li>
							</xsl:for-each>
						</ul>
					</p>
				</xsl:for-each>
			</div>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>