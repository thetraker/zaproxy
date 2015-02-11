/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.log4j.Logger;
import org.zaproxy.zap.Version;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * Base class that reads common {@code ZapAddOn} XML data.
 * <p>
 * Reads:
 * <ul>
 * <li>name;</li>
 * <li>version;</li>
 * <li>semver;</li>
 * <li>description;</li>
 * <li>author;</li>
 * <li>url;</li>
 * <li>changes;</li>
 * <li>not-before-version;</li>
 * <li>not-from-version;</li>
 * <li>dependencies:
 * <ul>
 * <li>javaversion;</li>
 * <li>addon:</li>
 * <ul>
 * <li>id;</li>
 * <li>not-before-version;</li>
 * <li>not-from-version;</li>
 * <li>semver.</li>
 * </ul>
 * </ul>
 * </li> </ul>
 * 
 * @since 2.4.0
 */
public abstract class BaseZapAddOnXmlData {

    private static final Logger LOGGER = Logger.getLogger(BaseZapAddOnXmlData.class);

    private static final String NAME_ELEMENT = "name";
    private static final String VERSION_ELEMENT = "version";
    private static final String SEM_VER_ELEMENT = "semver";
    private static final String DESCRIPTION_ELEMENT = "description";
    private static final String AUTHOR_ELEMENT = "author";
    private static final String URL_ELEMENT = "url";
    private static final String CHANGES_ELEMENT = "changes";
    private static final String NOT_BEFORE_VERSION_ELEMENT = "not-before-version";
    private static final String NOT_FROM_VERSION_ELEMENT = "not-from-version";

    private static final String DEPENDENCIES_ELEMENT = "dependencies";
    private static final String DEPENDENCIES_JAVA_VERSION_ELEMENT = "javaversion";
    private static final String DEPENDENCIES_ADDONS_ALL_ELEMENTS = "addons/addon";
    private static final String ZAPADDON_ID_ELEMENT = "id";
    private static final String ZAPADDON_NOT_BEFORE_VERSION_ELEMENT = "not-before-version";
    private static final String ZAPADDON_NOT_FROM_VERSION_ELEMENT = "not-from-version";
    private static final String ZAPADDON_SEMVER_ELEMENT = "semver";

    private String name;
    private int packageVersion;
    private Version version;
    private String description;
    private String author;
    private String url;
    private String changes;

    private Dependencies dependencies;

    private String notBeforeVersion;
    private String notFromVersion;

    /**
     * Constructs a {@code BaseZapAddOnXmlData} with the given {@code inputStream} as the source of the {@code ZapAddOn} XML
     * data.
     * 
     * @param inputStream the source of the {@code ZapAddOn} XML data.
     * @throws IOException if an error occurs while reading the data
     */
    public BaseZapAddOnXmlData(InputStream inputStream) throws IOException {
        ZapXmlConfiguration zapAddOnXml = new ZapXmlConfiguration();
        zapAddOnXml.setExpressionEngine(new XPathExpressionEngine());

        try {
            zapAddOnXml.load(inputStream);
        } catch (ConfigurationException e) {
            throw new IOException(e);
        }
        readDataImpl(zapAddOnXml);
    }

    /**
     * Constructs a {@code BaseZapAddOnXmlData} with the given {@code zapAddOnXml} {@code HierarchicalConfiguration} as the
     * source of the {@code ZapAddOn} XML data.
     * <p>
     * The given {@code HierarchicalConfiguration} must have a {@code XPathExpressionEngine} installed.
     * 
     * @param zapAddOnXml the source of the {@code ZapAddOn} XML data.
     * @see XPathExpressionEngine
     */
    public BaseZapAddOnXmlData(HierarchicalConfiguration zapAddOnXml) {
        readDataImpl(zapAddOnXml);
    }

    private void readDataImpl(HierarchicalConfiguration zapAddOnXml) {
        name = zapAddOnXml.getString(NAME_ELEMENT, "");
        packageVersion = zapAddOnXml.getInt(VERSION_ELEMENT, 0);
        version = createVersion(zapAddOnXml.getString(SEM_VER_ELEMENT, ""));
        description = zapAddOnXml.getString(DESCRIPTION_ELEMENT, "");
        author = zapAddOnXml.getString(AUTHOR_ELEMENT, "");
        url = zapAddOnXml.getString(URL_ELEMENT, "");
        changes = zapAddOnXml.getString(CHANGES_ELEMENT, "");

        dependencies = readDependencies(zapAddOnXml, "zapaddon");

        notBeforeVersion = zapAddOnXml.getString(NOT_BEFORE_VERSION_ELEMENT, "");
        notFromVersion = zapAddOnXml.getString(NOT_FROM_VERSION_ELEMENT, "");

        readAdditionalData(zapAddOnXml);
    }

    /**
     * Reads additional data from the {@code ZapAddOn} XML.
     * <p>
     * Called after reading the common data.
     *
     * @param zapAddOnData the source of the {@code ZapAddOn} XML data.
     */
    protected void readAdditionalData(HierarchicalConfiguration zapAddOnData) {
    }

    private static Version createVersion(String version) {
        if (!version.isEmpty()) {
            return new Version(version);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public int getPackageVersion() {
        return packageVersion;
    }

    public Version getVersion() {
        return version;
    }

    public String getChanges() {
        return changes;
    }

    public String getUrl() {
        return url;
    }

    public Dependencies getDependencies() {
        return dependencies;
    }

    public String getNotBeforeVersion() {
        return notBeforeVersion;
    }

    public String getNotFromVersion() {
        return notFromVersion;
    }

    protected List<String> getStrings(HierarchicalConfiguration zapAddOnXml, String element, String elementName) {
        String[] fields = zapAddOnXml.getStringArray(element);
        if (fields.length == 0) {
            return Collections.emptyList();
        }

        ArrayList<String> strings = new ArrayList<>(fields.length);
        for (String field : fields) {
            if (!field.isEmpty()) {
                strings.add(field);
            } else {
                LOGGER.warn("Ignoring empty \"" + elementName + "\" entry in add-on \"" + name + "\".");
            }
        }

        if (strings.isEmpty()) {
            return Collections.emptyList();
        }
        strings.trimToSize();

        return strings;
    }

    private Dependencies readDependencies(HierarchicalConfiguration currentNode, String element) {
        List<HierarchicalConfiguration> dependencies = currentNode.configurationsAt(DEPENDENCIES_ELEMENT);
        if (dependencies.isEmpty()) {
            return null;
        }

        if (dependencies.size() > 1) {
            malformedFile("expected at most one \"dependencies\" element for \"" + element + "\" element, found "
                    + dependencies.size() + ".");
        }

        HierarchicalConfiguration node = dependencies.get(0);
        String javaVersion = node.getString(DEPENDENCIES_JAVA_VERSION_ELEMENT, "");
        List<HierarchicalConfiguration> fields = node.configurationsAt(DEPENDENCIES_ADDONS_ALL_ELEMENTS);
        if (fields.isEmpty()) {
            return new Dependencies(javaVersion, Collections.<AddOnDep> emptyList());
        }

        List<AddOnDep> addOns = new ArrayList<>(fields.size());
        for (HierarchicalConfiguration sub : fields) {
            String id = sub.getString(ZAPADDON_ID_ELEMENT, "");
            if (id.isEmpty()) {
                malformedFile("an add-on dependency has empty \"" + ZAPADDON_ID_ELEMENT + "\".");
            }

            AddOnDep addOnDep = new AddOnDep(id, sub.getString(ZAPADDON_NOT_BEFORE_VERSION_ELEMENT, ""), sub.getString(
                    ZAPADDON_NOT_FROM_VERSION_ELEMENT,
                    ""), sub.getString(ZAPADDON_SEMVER_ELEMENT, ""));

            addOns.add(addOnDep);
        }

        return new Dependencies(javaVersion, addOns);
    }

    private void malformedFile(String reason) {
        throw new IllegalArgumentException("Add-on \"" + name + "\" contains malformed ZapAddOn.xml file, " + reason);
    }

    public static class Dependencies {

        private final String javaVersion;

        private final List<AddOnDep> addOnDependencies;

        public Dependencies(String javaVersion, List<AddOnDep> addOnDependencies) {
            this.javaVersion = javaVersion;
            this.addOnDependencies = addOnDependencies;
        }

        public String getJavaVersion() {
            return javaVersion;
        }

        public List<AddOnDep> getAddOns() {
            return addOnDependencies;
        }
    }

    public static class AddOnDep {

        private final String id;
        private final int notBeforeVersion;
        private final int notFromVersion;
        private final String semVer;

        public AddOnDep(String id, String notBeforeVersion, String notFromVersion, String semVer) {
            this.id = id;
            this.notBeforeVersion = convertToInt(notBeforeVersion, -1, "not-before-version");
            this.notFromVersion = convertToInt(notFromVersion, -1, "not-from-version");
            this.semVer = semVer;
        }

        private static int convertToInt(String value, int defaultValue, String element) {
            if (value == null || value.isEmpty()) {
                return defaultValue;
            }

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected integer for element \"" + element + "\" but was: " + value);
            }
        }

        public String getId() {
            return id;
        }

        public int getNotBeforeVersion() {
            return notBeforeVersion;
        }

        public int getNotFromVersion() {
            return notFromVersion;
        }

        public String getSemVer() {
            return semVer;
        }
    }
}
