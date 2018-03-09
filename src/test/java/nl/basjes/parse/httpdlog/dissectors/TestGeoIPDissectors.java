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

import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.test.DissectorTester;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestGeoIPDissectors {

    @Test
    public void testGeoIP_ASN() {
        GeoIPASNDissector dissector = new GeoIPASNDissector();
        dissector.initializeFromSettingsParameter("/var/lib/GeoIP/GeoLite2-ASN.mmdb");

        DissectorTester.create()
            .withInput("91.195.1.42")
            .withDissector(dissector)
            .expect("ASN:asn.number",               "43440")
            .expect("ASN:asn.number",               43440L)
            .expect("STRING:asn.organization",      "bol.com BV")
            .checkExpectations();
    }

    @Test
    public void testGeoIP_Country() {
        GeoIPCountryDissector dissector = new GeoIPCountryDissector();
        dissector.initializeFromSettingsParameter("/var/lib/GeoIP/GeoLite2-Country.mmdb");

        DissectorTester.create()
            .withInput("91.195.1.42")
            .withDissector(dissector)
            .expect("STRING:continent.name",        "Europe")
            .expect("STRING:continent.code",        "EU")
            .expect("STRING:country.name",          "Netherlands")
            .expect("STRING:country.iso",           "NL")
            .checkExpectations();
    }

    @Test
    public void testGeoIP_City() {
        GeoIPCityDissector dissector = new GeoIPCityDissector();
        dissector.initializeFromSettingsParameter("/var/lib/GeoIP/GeoLite2-City.mmdb");

        DissectorTester.create()
            .withInput("91.195.1.42")
            .withDissector(dissector)
            .expect("STRING:continent.name",        "Europe")
            .expect("STRING:continent.code",        "EU")
            .expect("STRING:country.name",          "Netherlands")
            .expect("STRING:country.iso",           "NL")
            .expect("STRING:city.name",             "Utrecht")
            .expect("STRING:postal.code",           "3528")
            .expect("STRING:location.latitude",     "52.0689")
            .expect("STRING:location.latitude",     52.0689)
            .expect("STRING:location.longitude",    "5.0806000000000004")
            .expect("STRING:location.longitude",    5.0806000000000004)
            .checkExpectations();
    }

}
