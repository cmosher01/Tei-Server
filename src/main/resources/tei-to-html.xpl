<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step
    version="1.0"
    xmlns:p="http://www.w3.org/ns/xproc"
>
    <p:input port="parameters" kind="parameter"/>
    <p:input port="source" sequence="true"/>
    <p:output port="result"/>

    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="tei-copyOf.xsl"/>
        </p:input>
    </p:xslt>

    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="tei-facs.xsl"/>
        </p:input>
    </p:xslt>

    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="tei-norm-text.xsl"/>
        </p:input>
    </p:xslt>

<!--    <p:xslt>-->
<!--        <p:input port="stylesheet">-->
<!--            <p:document href="tei-xml-attr.xsl"/>-->
<!--        </p:input>-->
<!--    </p:xslt>-->

    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="tei-xhtml-specific.xsl"/>
        </p:input>
    </p:xslt>

    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="tei-xhtml-general.xsl"/>
        </p:input>
    </p:xslt>

    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="tei-xhtml-page.xsl"/>
        </p:input>
    </p:xslt>
</p:declare-step>
