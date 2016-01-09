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

import org.apache.pig.ExecType;
import org.apache.pig.PigServer;
import org.apache.pig.builtin.mock.Storage;
import org.apache.pig.data.Tuple;
import org.junit.Test;

import java.util.List;

import static org.apache.pig.builtin.mock.Storage.resetData;
import static org.apache.pig.builtin.mock.Storage.tuple;
import static org.junit.Assert.assertEquals;

public class TestGeoIP2DissectorPig {

    private static final String logformat = "%h";
    private final String logfile = getClass().getResource("/ip.log").toString();

    @Test
    public void remappedLoaderTest() throws Exception {
        PigServer pigServer = new PigServer(ExecType.LOCAL);
        Storage.Data data = resetData(pigServer);

        pigServer.registerQuery(
            "Clicks = " +
            "       LOAD '" + logfile + "' " +
            "       USING nl.basjes.pig.input.apachehttpdlog.Loader(" +
            "               '" + logformat + "'," +
            "               'IP:connection.client.host'," +
            "       '-load:nl.basjes.parse.httpdlog.dissectors.GeoIP2Dissector:/var/lib/GeoIP/GeoLite2-City.mmdb'," +
            "               'STRING:connection.client.host.country.name'," +
            "               'STRING:connection.client.host.country.iso'," +
            "               'STRING:connection.client.host.location.latitude'," +
            "               'STRING:connection.client.host.location.longitude'" +
            "          )" +
            "       AS (" +
            "               connection_client_host:chararray," +
            "               connection_client_host_country_name:chararray," +
            "               connection_client_host_country_iso:chararray," +
            "               connection_client_host_location_latitude:double," +
            "               connection_client_host_location_longitude:double" +
            "          );"
        );
        pigServer.registerQuery("STORE Clicks INTO 'Clicks' USING mock.Storage();");

        List<Tuple> out = data.get("Clicks");

        assertEquals(2, out.size());
        assertEquals(tuple(
                "178.208.38.98",
                "Netherlands",
                "NL",
                "52.3667",
                "4.9"
                ).toDelimitedString("><#><"),
                out.get(0).toDelimitedString("><#><"));
        assertEquals(tuple(
                "80.100.47.45",
                "Netherlands",
                "NL",
                "52.3667",
                "4.9"
                ).toDelimitedString("><#><"),
                out.get(1).toDelimitedString("><#><"));

  }


}
