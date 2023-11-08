package com.github.vgaj.phd.server.analysis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class ResultsSaveXmlMapper
{
    public static XmlMapper getXmlMapper()
    {
        JacksonXmlModule xmlModule = new JacksonXmlModule();

        // Prevent Jackson from using a wrapper for empty lists
        xmlModule.setDefaultUseWrapper(false);

        XmlMapper xmlMapper = new XmlMapper(xmlModule);

        // Configure Jackson to only include all properties by default,
        // otherwise private fields with no getter will not get included.
        xmlMapper.setVisibility(xmlMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));

        return xmlMapper;
    }
}
