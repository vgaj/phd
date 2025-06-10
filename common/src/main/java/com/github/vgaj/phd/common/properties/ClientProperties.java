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

package com.github.vgaj.phd.common.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientProperties {
    private static Properties properties = null;

    private static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try (InputStream input = ClientProperties.class.getClassLoader().getResourceAsStream("common.properties")) {
                if (input == null) {
                    System.err.println("Failed to load common.properties");
                } else {
                    properties.load(input);
                }
            } catch (IOException ex) {
                System.err.println("Failed to load common.properties Error: " + ex);
            }
        }
        return properties;
    }

    public static String getVersion() {
        return getProperties().getProperty("phd.version");
    }

    public static String getHotspotNicPath() {
        return getProperties().getProperty("hotspotnic.file");
    }
}
