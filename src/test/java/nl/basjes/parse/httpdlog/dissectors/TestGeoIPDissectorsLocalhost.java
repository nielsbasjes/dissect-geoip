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

import nl.basjes.parse.core.test.DissectorTester;
import org.junit.Test;

public class TestGeoIPDissectorsLocalhost {

    @Test
    public void testGeoIP_ASN() {
        GeoIPASNDissector dissector = new GeoIPASNDissector();
        dissector.initializeFromSettingsParameter("/var/lib/GeoIP/GeoLite2-ASN.mmdb");

        DissectorTester.create()
            .withInput("127.0.0.1")
            .withDissector(dissector)
            .expectAbsentString("ASN:asn.number")
            .expectAbsentLong("ASN:asn.number"           )
            .expectAbsentString("STRING:asn.organization"  )
            .checkExpectations();
    }

    @Test
    public void testGeoIP_Country() {
        GeoIPCountryDissector dissector = new GeoIPCountryDissector();
        dissector.initializeFromSettingsParameter("/var/lib/GeoIP/GeoLite2-Country.mmdb");

        DissectorTester.create()
            .withInput("127.0.0.1")
            .withDissector(dissector)
            .expectAbsentString("STRING:continent.name")
            .expectAbsentString("STRING:continent.code")
            .expectAbsentString("STRING:country.name")
            .expectAbsentString("STRING:country.iso")
            .checkExpectations();
    }

    @Test
    public void testGeoIP_City() {
        GeoIPCityDissector dissector = new GeoIPCityDissector();
        dissector.initializeFromSettingsParameter("/var/lib/GeoIP/GeoLite2-City.mmdb");

        DissectorTester.create()
            .withInput("127.0.0.1")
            .withDissector(dissector)
            .expectAbsentString("STRING:continent.name")
            .expectAbsentString("STRING:continent.code")
            .expectAbsentString("STRING:country.name")
            .expectAbsentString("STRING:country.iso")
            .expectAbsentString("STRING:city.name")
            .expectAbsentString("STRING:postal.code")
            .expectAbsentString("STRING:location.latitude")
            .expectAbsentString("STRING:location.latitude")
            .expectAbsentString("STRING:location.longitude")
            .expectAbsentString("STRING:location.longitude")
            .checkExpectations();
    }

}
