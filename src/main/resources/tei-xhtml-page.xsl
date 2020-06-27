<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<xsl:stylesheet
    version="3.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
    <xsl:output method="xml" version="1.1" encoding="UTF-8"/>
    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="element()[@class='tei-TEI']">
        <xsl:element name="html" namespace="http://www.w3.org/1999/xhtml">
            <xsl:attribute name="class">
                <xsl:value-of select="'fontFeatures unicodeWebFonts solarizedLight'"/>
            </xsl:attribute>
            <xsl:element name="head" namespace="http://www.w3.org/1999/xhtml">
                <xsl:element name="meta" namespace="http://www.w3.org/1999/xhtml">
                    <xsl:attribute name="charset">
                        <xsl:value-of select="'utf-8'"/>
                    </xsl:attribute>
                </xsl:element>
                <xsl:element name="link" namespace="http://www.w3.org/1999/xhtml">
                    <xsl:attribute name="rel">
                        <xsl:value-of select="'stylesheet'"/>
                    </xsl:attribute>
                    <xsl:attribute name="type">
                        <xsl:value-of select="'text/css'"/>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="'style.css'"/>
                    </xsl:attribute>
                </xsl:element>
                <xsl:element name="title" namespace="http://www.w3.org/1999/xhtml">
                    <xsl:value-of select="//element()[@class='tei-title']/text()"/>
                </xsl:element>
            </xsl:element>
            <xsl:element name="body" namespace="http://www.w3.org/1999/xhtml">
                <xsl:element name="nav" namespace="http://www.w3.org/1999/xhtml">
                    (
                    <xsl:element name="a" namespace="http://www.w3.org/1999/xhtml">
                        <xsl:attribute name="href">
                            <xsl:value-of select="'?tei=TRUE'"/>
                        </xsl:attribute>
                        view source
                    </xsl:element>
                    )
                </xsl:element>
                <xsl:element name="div" namespace="http://www.w3.org/1999/xhtml">
                    <xsl:apply-templates select="@* | node()"/>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
