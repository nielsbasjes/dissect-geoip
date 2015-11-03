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

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GeoIP2Dissector extends AbstractGeoIPDissector {

    // --------------------------------------------

    private DatabaseReader reader;

    @Override
    public void prepareForRun() {
        // A File object pointing to your GeoIP2 or GeoLite2 database
        File database = new File(databaseFileName);

        // This creates the DatabaseReader object, which should be reused across lookups.
        try {
            reader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------------------------------------------

    public void dissect(final Parsable<?> parsable, final String inputname, final InetAddress ipAddress) throws DissectionFailure {
        // Replace "city" with the appropriate method for your database, e.g. "country".
        CityResponse response = null;
        try {
            response = reader.city(ipAddress);
        } catch (IOException | GeoIp2Exception e) {
            return;
        }

        if (response == null) {
            return;
        }
            Country country = response.getCountry();
            if (country != null) {
                if (wantCountryName) {
                    parsable.addDissection(inputname, "STRING", "country.name", country.getName());
                }
                if (wantCountryIso) {
                    parsable.addDissection(inputname, "STRING", "country.iso", country.getIsoCode());
                }
            }

            Subdivision subdivision = response.getMostSpecificSubdivision();
            if (subdivision != null) {
                if (wantSubdivisionName) {
                    parsable.addDissection(inputname, "STRING", "subdivision.name", subdivision.getName());
                }
                if (wantSubdivisionIso) {
                    parsable.addDissection(inputname, "STRING", "subdivision.iso", subdivision.getIsoCode());
                }
            }

            if (wantCityName) {
                City city = response.getCity();
                if (city != null) {
                    parsable.addDissection(inputname, "STRING", "city.name", city.getName());
                }
            }

            if (wantPostalCode) {
                Postal postal = response.getPostal();
                if (postal != null) {
                    parsable.addDissection(inputname, "STRING", "postal.code", postal.getCode());
                }
            }

            Location location = response.getLocation();
            if (location != null) {
                if (wantLocationLatitude) {
                    parsable.addDissection(inputname, "STRING", "location.latitude", Double.toString(location.getLatitude()));
                }
                if (wantLocationLongitude) {
                    parsable.addDissection(inputname, "STRING", "location.longitude", Double.toString(location.getLongitude()));
                }
                if (wantLocationTimezone) {
                    parsable.addDissection(inputname, "STRING", "location.timezone", location.getTimeZone());
                }
            }
    }
    // --------------------------------------------

}
