<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<xsl:stylesheet
    version="3.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
>
    <xsl:output method="xml" version="1.1" encoding="UTF-8"/>
    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="text()">
        <xsl:choose>
            <xsl:when test="fn:matches(.,'^\s*$')">
                <xsl:value-of select="fn:string('&#x0020;')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="fn:matches(.,'^\s')">
                    <xsl:value-of select="fn:string('&#x0020;')"/>
                </xsl:if>
                <xsl:value-of select="fn:normalize-unicode(fn:normalize-space())"/>
                <xsl:if test="fn:matches(.,'\s$')">
                    <xsl:value-of select="fn:string('&#x0020;')"/>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
