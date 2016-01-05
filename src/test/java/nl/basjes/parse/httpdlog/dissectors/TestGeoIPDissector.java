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
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestGeoIPDissector {
    public static class MyRecord {

        final Map<String, String> sResults = new HashMap<>(32);
        final Map<String, Double> dResults = new HashMap<>(32);

        @Field({"STRING:country.name",
                "STRING:country.iso",
                "STRING:subdivision.name",
                "STRING:subdivision.iso",
                "STRING:city.name",
                "STRING:postal.code",
                "STRING:location.latitude",
                "STRING:location.longitude"})
        public void setValue(final String name, final String value) {
            sResults.put(name, value);
        }

        @Field({"STRING:location.latitude",
                "STRING:location.longitude"})
        public void setValue(final String name, final Double value) {
            dResults.put(name, value);
        }

        public String getString(final String name) {
            return sResults.get(name);
        }
        public Double getDouble(final String name) {
            return dResults.get(name);
        }

        public void clear() {
            sResults.clear();
            dResults.clear();
        }

    }

    static class TestGeoIP2Parser extends Parser<MyRecord>{
        public TestGeoIP2Parser() {
            super(MyRecord.class);
            GeoIP2Dissector dissector = new GeoIP2Dissector();
            dissector.initializeFromSettingsParameter("/var/lib/GeoIP/GeoLite2-City.mmdb");
            addDissector(dissector);
            setRootType("IP");
        }
    }

    private static TestGeoIP2Parser parser2;
    private static MyRecord record;


    @BeforeClass
    public static void setUp() throws ParseException, InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        parser2 = new TestGeoIP2Parser();
        record = new MyRecord();
    }

    @Test
    public void testGeoIP2() throws Exception {
        record.clear();
        parser2.parse(record, "80.100.47.45");
        assertEquals("STRING:country.name",        "Netherlands", record.getString("STRING:country.name"));
        assertEquals("STRING:country.iso",         "NL", record.getString("STRING:country.iso"));
        assertEquals("STRING:subdivision.name",    null, record.getString("STRING:subdivision.name"));
        assertEquals("STRING:subdivision.iso",     null, record.getString("STRING:subdivision.iso"));
        assertEquals("STRING:city.name",           null, record.getString("STRING:city.name"));
        assertEquals("STRING:postal.code",         null, record.getString("STRING:postal.code"));
        assertEquals("STRING:location.latitude",   "52.3667", record.getString("STRING:location.latitude"));
        assertEquals("STRING:location.latitude",   52.3667, record.getDouble("STRING:location.latitude"), 0.0001);
        assertEquals("STRING:location.longitude",   "4.9", record.getString("STRING:location.longitude"));
        assertEquals("STRING:location.longitude",   4.899994, record.getDouble("STRING:location.longitude"), 0.0001);
    }

}
