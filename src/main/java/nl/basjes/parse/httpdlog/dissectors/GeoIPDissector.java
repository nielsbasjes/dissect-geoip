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

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.exceptions.DissectionFailure;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class GeoIPDissector extends AbstractGeoIPDissector {
    // --------------------------------------------

    private LookupService lookupService;

    @Override
    public void prepareForRun() {
        // A File object pointing to your GeoIP2 or GeoLite2 database
        File database = new File(databaseFileName);

        // This creates the DatabaseReader object, which should be reused across lookups.
        try {
            lookupService = new LookupService(databaseFileName,
                    LookupService.GEOIP_MEMORY_CACHE | LookupService.GEOIP_CHECK_CACHE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------------------------------------------

    public void dissect(final Parsable<?> parsable, final String inputname, final InetAddress ipAddress) throws DissectionFailure {
        Location location = lookupService.getLocation(ipAddress);
        if (location == null) {
            return;
        }
        if (wantCountryName) {
            parsable.addDissection(inputname, "STRING", "country.name", location.countryName);
        }
        if (wantCountryIso) {
            parsable.addDissection(inputname, "STRING", "country.iso", location.countryCode);
        }
        if (wantCityName) {
            parsable.addDissection(inputname, "STRING", "city.name", location.city);
        }
        if (wantPostalCode) {
            parsable.addDissection(inputname, "STRING", "postal.code", location.postalCode);
        }
        if (wantLocationLatitude) {
            parsable.addDissection(inputname, "STRING", "location.latitude", Float.toString(location.latitude));
        }  // 44.9733
        if (wantLocationLongitude) {
            parsable.addDissection(inputname, "STRING", "location.longitude", Float.toString(location.longitude));
        } // -93.2323
    }
}
// --------------------------------------------



