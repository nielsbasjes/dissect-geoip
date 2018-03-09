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

public class TestGeoIPCountryDissectorPig {

    private static final String logformat = "%h";
    private final String logfile = getClass().getResource("/ip.log").toString();

    @Test
    public void testGeoIPCountryDissectorPig() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Clicks = " +
            "       LOAD '" + logfile + "' " +
            "       USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
            "               '" + logformat + "'," +
            "               'IP:connection.client.host'," +
            "       '-load:nl.basjes.parse.httpdlog.dissectors.GeoIPCountryDissector:/var/lib/GeoIP/GeoLite2-Country.mmdb'," +
            "               'STRING:connection.client.host.continent.name'," +
            "               'STRING:connection.client.host.continent.code'," +
            "               'STRING:connection.client.host.country.name'," +
            "               'STRING:connection.client.host.country.iso'" +
            "          )" +
            "       AS (" +
            "               connection_client_host:chararray," +
            "               connection_client_host_continent_name:chararray," +
            "               connection_client_host_continent_code:chararray," +
            "               connection_client_host_country_name:chararray," +
            "               connection_client_host_country_iso:chararray" +
            "          );"
        );
        pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks");

        assertEquals(3, out.size());

        Tuple result;

        result = out.get(0); // Google
        assertEquals("8.8.8.8", result.get(0).toString());
        assertEquals("North America", result.get(1).toString());
        assertEquals("NA", result.get(2).toString());
        assertEquals("United States", result.get(3).toString());
        assertEquals("US", result.get(4).toString());

        result = out.get(1); // Basjes.nl
        assertEquals("80.100.47.45", result.get(0).toString());
        assertEquals("Europe", result.get(1).toString());
        assertEquals("EU", result.get(2).toString());
        assertEquals("Netherlands", result.get(3).toString());
        assertEquals("NL", result.get(4).toString());

        result = out.get(2); // Bol.com
        assertEquals("91.195.1.42", result.get(0).toString());
        assertEquals("Europe", result.get(1).toString());
        assertEquals("EU", result.get(2).toString());
        assertEquals("Netherlands", result.get(3).toString());
        assertEquals("NL", result.get(4).toString());
    }


}
