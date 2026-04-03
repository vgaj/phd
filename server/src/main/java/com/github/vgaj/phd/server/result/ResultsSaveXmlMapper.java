/*
MIT License

Copyright (c) 2022-2025 Viru Gajanayake

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.github.vgaj.phd.server.result;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import tools.jackson.databind.MapperFeature;
import tools.jackson.dataformat.xml.XmlMapper;

public class ResultsSaveXmlMapper {
    public static XmlMapper getXmlMapper() {
        // Configure Jackson to include all properties by default,
        // otherwise private fields with no getter will not get included.
        // ALLOW_FINAL_FIELDS_AS_MUTATORS is needed to deserialise into final fields
        // (not enabled by default in Jackson 3, unlike Jackson 2).
        return XmlMapper.xmlBuilder()
                // Prevent Jackson from using a wrapper for empty lists
                .defaultUseWrapper(false)
                .changeDefaultVisibility(v -> v.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
                .build();
    }
}
