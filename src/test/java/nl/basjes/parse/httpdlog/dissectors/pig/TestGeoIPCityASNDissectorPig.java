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

package nl.basjes.parse.httpdlog.dissectors.pig;

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.builtin.mock.Storage;
import org.apache.pig.data.Tuple;
import org.junit.Test;

import java.util.List;

import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.junit.Assert.assertEquals;

public class TestGeoIPCityASNDissectorPig {

    private static final String logformat = "%h";
    private final String logfile = getClass().getResource("/ip.log").toString();

    @Test
    public void testGeoIPCityASNDissectorPig() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Clicks = " +
            "       LOAD '" + logfile + "' " +
            "       USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
            "               '" + logformat + "'," +
            "               'IP:connection.client.host'," +
            "       '-load:nl.basjes.parse.httpdlog.dissectors.GeoIPCityDissector:/var/lib/GeoIP/GeoLite2-City.mmdb'," +
            "               'STRING:connection.client.host.continent.name'," +
            "               'STRING:connection.client.host.continent.code'," +
            "               'STRING:connection.client.host.country.name'," +
            "               'STRING:connection.client.host.country.iso'," +
            "               'STRING:connection.client.host.subdivision.name'," +
            "               'STRING:connection.client.host.subdivision.iso'," +
            "               'STRING:connection.client.host.city.name'," +
            "               'STRING:connection.client.host.postal.code'," +
            "               'STRING:connection.client.host.location.latitude'," +
            "               'STRING:connection.client.host.location.longitude'," +

            "       '-load:nl.basjes.parse.httpdlog.dissectors.GeoIPASNDissector:/var/lib/GeoIP/GeoLite2-ASN.mmdb'," +
            "               'ASN:connection.client.host.asn.number'," +
            "               'STRING:connection.client.host.asn.organization'" +
            "          )" +
            "       AS (" +
            "               connection_client_host:chararray," +

            "               connection_client_host_continent_name:chararray," +
            "               connection_client_host_continent_code:chararray," +
            "               connection_client_host_country_name:chararray," +
            "               connection_client_host_country_iso:chararray," +
            "               connection_client_host_subdivision_name:chararray," +
            "               connection_client_host_subdivision_iso:chararray," +
            "               connection_client_host_city_name:chararray," +
            "               connection_client_host_postal_code:chararray," +
            "               connection_client_host_location_latitude:double," +
            "               connection_client_host_location_longitude:double," +

            "               connection_client_host_asn_number:long," +
            "               connection_client_host_asn_organization:chararray" +
            "          );"
        );
        pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks");

        assertEquals(3, out.size());

        Tuple result;

        result = out.get(0); // Google
        assertEquals("8.8.8.8",             result.get(0).toString());
        assertEquals("North America",       result.get(1).toString());
        assertEquals("NA",                  result.get(2).toString());
        assertEquals("United States",       result.get(3).toString());
        assertEquals("US",                  result.get(4).toString());
        assertEquals(null,                  result.get(5));
        assertEquals(null,                  result.get(6));
        assertEquals(null,                  result.get(7));
        assertEquals(null,                  result.get(8));
        assertEquals("37",                  result.get(9).toString().substring(0, 2));
        assertEquals("-97",                 result.get(10).toString().substring(0, 3));
        assertEquals("15169",               result.get(11).toString());
        assertEquals("Google LLC",          result.get(12).toString());

        result = out.get(1); // Basjes.nl
        assertEquals("80.100.47.45",        result.get(0).toString());
        assertEquals("Europe",              result.get(1).toString());
        assertEquals("EU",                  result.get(2).toString());
        assertEquals("Netherlands",         result.get(3).toString());
        assertEquals("NL",                  result.get(4).toString());
        assertEquals(null,                  result.get(5));
        assertEquals(null,                  result.get(6));
        assertEquals(null,                  result.get(7));
        assertEquals(null,                  result.get(8));
        assertEquals("52",                  result.get(9).toString().substring(0, 2));
        assertEquals("4.",                  result.get(10).toString().substring(0, 2));
        assertEquals("3265",                result.get(11).toString());
        assertEquals("Xs4all Internet BV",  result.get(12).toString());

        result = out.get(2); // Bol.com office
        assertEquals("91.195.1.42",         result.get(0).toString());
        assertEquals("Europe",              result.get(1).toString());
        assertEquals("EU",                  result.get(2).toString());
        assertEquals("Netherlands",         result.get(3).toString());
        assertEquals("NL",                  result.get(4).toString());
        assertEquals("Provincie Utrecht",   result.get(5));
        assertEquals("UT",                  result.get(6));
        assertEquals("Utrecht",             result.get(7));
        assertEquals("3528",                result.get(8));
        assertEquals("52",                  result.get(9).toString().substring(0, 2));
        assertEquals("5.",                  result.get(10).toString().substring(0, 2));
        assertEquals("43440",               result.get(11).toString());
        assertEquals("bol.com BV",          result.get(12).toString());
    }


}
