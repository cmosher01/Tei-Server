<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<xsl:stylesheet
    version="3.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
>
    <xsl:output method="xml" version="1.1" encoding="UTF-8"/>
    <xsl:mode on-no-match="shallow-copy"/>

    <!-- DEFAULT (block) TEI elem ==> HTML div class=tei-elem -->
    <!-- TODO: add more block elements -->
    <xsl:template match="tei:teiHeader|tei:fileDesc|tei:titleStmt|tei:publicationStmt|tei:sourceDesc|tei:profileDesc|tei:particDesc|tei:person|tei:text|tei:body|tei:p|tei:facsimile">
        <xsl:element name="div" namespace="http://www.w3.org/1999/xhtml">
            <xsl:attribute name="class">
                <xsl:value-of select="fn:concat('tei-', fn:local-name())"/>
            </xsl:attribute>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:element>
    </xsl:template>

    <!-- DEFAULT (inline) TEI elem ==> HTML span class="tei-elem" -->
    <xsl:template match="tei:*">
        <xsl:element name="span" namespace="http://www.w3.org/1999/xhtml">
            <xsl:attribute name="class">
                <xsl:value-of select="fn:concat('tei-', fn:local-name())"/>
            </xsl:attribute>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
