<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:variable name="packageNamePrefix" select="'org.apache.roller.pojos.'"/>
    <xsl:template match="/">
        <entity-mappings xmlns="http://java.sun.com/xml/ns/persistence/orm" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://java.sun.com/xml/ns/persistence/orm_1_0.xsd orm_1_0.xsd" version="1.0">
            <description>Persistence Metadata for Roller</description>
            <persistence-unit-metadata>
                <!--xml-mapping-metadata-complete/-->
                <persistence-unit-defaults>
                    <access>PROPERTY</access>
                    <!--cascade-persist/-->
                </persistence-unit-defaults>
            </persistence-unit-metadata>
            <package>org.apache.roller.pojos</package>			
            <xsl:apply-templates select="hibernate-mapping/class"/>
        </entity-mappings>
    </xsl:template>
    <xsl:template match="hibernate-mapping/class">
        <entity  metadata-complete="true">
            <xsl:attribute name="name">
                <xsl:value-of select="substring-after(@name,$packageNamePrefix)"/>
            </xsl:attribute>
            <xsl:attribute name="class">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <table>
                <xsl:attribute name="name">
                    <xsl:value-of select="@table"/>
                </xsl:attribute>
            </table>
            <attributes>
                <xsl:apply-templates select="id"/>
                <xsl:for-each select="property">
                    <xsl:call-template name="process-property"/>
                </xsl:for-each>
                <xsl:for-each select="many-to-one">
                    <xsl:call-template name="process-many-to-one"/>    
                </xsl:for-each>
            </attributes>
        </entity>
    </xsl:template>       
    
    <xsl:template name="process-property">
        <basic>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <column>
		<xsl:attribute name="name">
		    <xsl:value-of select="@column"/>
		</xsl:attribute>
                <xsl:attribute name="insertable">
                    <xsl:value-of select="@insert"/>
                </xsl:attribute>
                <xsl:attribute name="updatable">
                    <xsl:value-of select="@update"/>
                </xsl:attribute>
                <xsl:attribute name="unique">
                    <xsl:value-of select="@unique"/>
                </xsl:attribute>
            </column>
        </basic>
    </xsl:template>
    <xsl:template name="process-many-to-one">
        <many-to-one>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="target-entity">
                <xsl:value-of select="@class"/>
            </xsl:attribute>
            <join-column>
                <xsl:attribute name="name">
                    <xsl:value-of select="@column"/>
                </xsl:attribute>
                <xsl:attribute name="insertable">
                    <xsl:value-of select="@insert"/>
                </xsl:attribute>
                <xsl:attribute name="updatable">
                    <xsl:value-of select="@update"/>
                </xsl:attribute>
                <xsl:if  test= "boolean(@not-null)">
                    <xsl:attribute name="nullable">
                        <xsl:choose>
                            <xsl:when test="@not-null = 'true' ">
                                <xsl:text>false</xsl:text>
                            </xsl:when>
                            <xsl:when test="@not-null = 'false' ">
                                <xsl:text>true</xsl:text>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:attribute>
                </xsl:if>
            </join-column>
            <xsl:if test="@cascade = 'all'">
                <cascade>
                    <cascade-all/>
                </cascade>
            </xsl:if>
        </many-to-one>
    </xsl:template>
    <xsl:template match="id">
        <id>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <column>
            <xsl:attribute name="name">
                <xsl:value-of select="@column"/>
            </xsl:attribute>
            </column>
        </id>
    </xsl:template>
    
</xsl:stylesheet>

