<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:vosi="http://www.ivoa.net/xml/VOSITables/v1.0">
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
				<div id="tapPath"><a href="../tap">TAP service</a>-&gt;<b>Tables</b></div>
				
				<h1 id="pageTopic">Available tables</h1>
				<xsl:apply-templates select="vosi:tableset | tableset" />
				
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="vosi:tableset | tableset">
		<div class="section">
			<h1>Tables</h1>
			<ul>
				<xsl:for-each select="schema">
					<li>
						<b><xsl:value-of select="name" />: </b>
						<xsl:for-each select="table">
							<xsl:if test="position() > 1">, </xsl:if>
							<xsl:element name="a">
								<xsl:attribute name="class">tapResource</xsl:attribute>
								<xsl:attribute name="href">#<xsl:value-of select="name" /></xsl:attribute>
								<xsl:value-of select="name" />
							</xsl:element>
						</xsl:for-each>
					</li>
				</xsl:for-each>
			</ul>
		</div>
		<xsl:apply-templates select="schema" />
	</xsl:template>
	
	<xsl:template match="schema">
		<div class="section">
			<h1>Schema: <xsl:value-of select="name" /></h1>
			<xsl:apply-templates select="table" />
		</div>
	</xsl:template>
	
	<xsl:template match="table">
		<table>
			<tr>
				<xsl:element name="th">
					<xsl:attribute name="style">font-variant: small-caps;</xsl:attribute>
					<xsl:attribute name="colspan"><xsl:value-of select="count(column[1]/child::*)" /></xsl:attribute>
					<xsl:attribute name="id"><xsl:value-of select="name" /></xsl:attribute>
					<xsl:value-of select="name" />
				</xsl:element>
			</tr>
			<tr style="text-transform: capitalize;">
				<xsl:for-each select="column[1]/child::*">
					<th><xsl:value-of select="name()" /></th>
				</xsl:for-each>
			</tr>
			<xsl:for-each select="column">
				<xsl:choose>
					<xsl:when test="position() mod 2 = 0">
						<tr class="pair">
							<xsl:for-each select="child::*">
								<td><xsl:value-of select="." /></td>
							</xsl:for-each>
						</tr>
					</xsl:when>
					<xsl:otherwise>
						<tr>
							<xsl:for-each select="child::*">
								<td><xsl:value-of select="." /></td>
							</xsl:for-each>
						</tr>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</table>
	</xsl:template>
</xsl:stylesheet>