<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<xsl:stylesheet
    version="3.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:exsl="http://exslt.org/common"
>
    <xsl:output method="xml" version="1.1" encoding="UTF-8"/>

    <xsl:template match="element()[@facs[starts-with(.,'#')]]" mode="#all">
        <xsl:copy>
            <xsl:apply-templates select="@* except @facs"/>
            <xsl:apply-templates select="exsl:node-set(fn:element-with-id(fn:substring(@facs,2)))" mode="noID"/>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@xml:id" mode="noID"/>

    <xsl:template match="@*|node()" mode="#all">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
