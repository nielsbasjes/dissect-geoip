/*
 * Apache HTTPD logparsing made easy
 * Copyright (C) 2011-2015 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.httpdlog.dissectors;

import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestGeoIPDissector {
    public static class MyRecord {

        final Map<String, String> results = new HashMap<>(32);

        @Field({
                "STRING:country.name",
                "STRING:country.iso",
                "STRING:subdivision.name",
                "STRING:subdivision.iso",
                "STRING:city.name",
                "STRING:postal.code",
                "STRING:location.latitude",
                "STRING:location.longitude"})
        public void setValue(final String name, final String value) {
            results.put(name, value);
        }

        public String getValue(final String name) {
            return results.get(name);
        }

        public void clear() {
            results.clear();
        }

    }

    static class TestGeoIPParser extends Parser<MyRecord>{
        public TestGeoIPParser() {
            super(MyRecord.class);
            GeoIPDissector dissector = new GeoIPDissector();
            dissector.initializeFromSettingsParameter("/usr/share/GeoIP/GeoIPCity.dat");
            addDissector(dissector);
            setRootType("IP");
        }
    }

    static class TestGeoIP2Parser extends Parser<MyRecord>{
        public TestGeoIP2Parser() {
            super(MyRecord.class);
            GeoIP2Dissector dissector = new GeoIP2Dissector();
            dissector.initializeFromSettingsParameter("/home/niels/tmp/GeoLite2-City.mmdb");
            addDissector(dissector);
            setRootType("IP");
        }
    }

    private static TestGeoIPParser parser1;
    private static TestGeoIP2Parser parser2;
    private static MyRecord record;


    @BeforeClass
    public static void setUp() throws ParseException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        parser1 = new TestGeoIPParser();
        parser2 = new TestGeoIP2Parser();
        record = new MyRecord();
    }

    @Test
    public void testGeoIP1() throws Exception {
        record.clear();
        parser1.parse(record, "80.100.47.45");

        System.out.print(record.results);
        assertEquals("STRING:country.name",        "Netherlands", record.getValue("STRING:country.name"));
        assertEquals("STRING:country.iso",         "NL", record.getValue("STRING:country.iso"));
        assertEquals("STRING:city.name",           null, record.getValue("STRING:city.name"));
        assertEquals("STRING:postal.code",         null, record.getValue("STRING:postal.code"));
        assertEquals("STRING:location.latitude",   "52.3667", record.getValue("STRING:location.latitude"));
        assertEquals("STRING:location.longitude",   "4.899994", record.getValue("STRING:location.longitude"));
    }

    @Test
    public void testGeoIP2() throws Exception {
        record.clear();
        parser2.parse(record, "80.100.47.45");
        assertEquals("STRING:country.name",        "Netherlands", record.getValue("STRING:country.name"));
        assertEquals("STRING:country.iso",         "NL", record.getValue("STRING:country.iso"));
        assertEquals("STRING:subdivision.name",    null, record.getValue("STRING:subdivision.name"));
        assertEquals("STRING:subdivision.iso",     null, record.getValue("STRING:subdivision.iso"));
        assertEquals("STRING:city.name",           null, record.getValue("STRING:city.name"));
        assertEquals("STRING:postal.code",         null, record.getValue("STRING:postal.code"));
        assertEquals("STRING:location.latitude",   "52.3667", record.getValue("STRING:location.latitude"));
        assertEquals("STRING:location.longitude",   "4.9", record.getValue("STRING:location.longitude"));
    }


}
