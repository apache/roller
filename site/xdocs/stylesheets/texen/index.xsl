<?xml version='1.0'?>
<!--
Copyright 2006 Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<xsl:stylesheet 
			xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
			version='1.0'>
	<xsl:output 
			method='xml' 
			indent='yes'
			encoding='UTF-8'/>
			
	<xsl:template match='document'>
		<li>
		<xsl:apply-templates select='properties'/>
		<xsl:apply-templates select='body'/>
		</li>
	</xsl:template>
	<xsl:template match='properties'>
					<xsl:element name='a'>
						<!-- @@@document-name@@@ is a magic variable substituted by the Texen control template -->
						<xsl:attribute name='href'>@@@document-name@@@</xsl:attribute>
						<xsl:value-of select='title'/>
					</xsl:element>
	</xsl:template>
	<xsl:template match='body'>
			<ul>
		    <xsl:apply-templates select='section'/>
			</ul>
	</xsl:template>
	<xsl:template match='section'>
		<li>
		<xsl:choose>
			<xsl:when test='@id'>
				<xsl:element name='a'>
					<!-- @@@document-name@@@ is a magic variable substituted by the Texen control template -->
					<xsl:attribute name='href'>@@@document-name@@@#<xsl:value-of select='@id'/></xsl:attribute>
					<xsl:value-of select='title'/>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select='title'/>
			</xsl:otherwise>
	    </xsl:choose>
		<xsl:if test='section'>
			<ul><xsl:apply-templates select='section'/></ul>
		</xsl:if>
		</li>
	</xsl:template>
</xsl:stylesheet>