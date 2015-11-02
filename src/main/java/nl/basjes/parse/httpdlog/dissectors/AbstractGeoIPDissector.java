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

import com.maxmind.geoip.Country;
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
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

public abstract class AbstractGeoIPDissector extends Dissector {
    // --------------------------------------------

    static final String INPUT_TYPE = "IP";

    String databaseFileName;

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    // --------------------------------------------

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();

        result.add("STRING:country.name");
        result.add("STRING:country.iso");
        result.add("STRING:subdivision.name");
        result.add("STRING:subdivision.iso");
        result.add("STRING:city.name");
        result.add("STRING:postal.code");
        result.add("STRING:location.latitude");
        result.add("STRING:location.longitude");

        return result;
    }

    // --------------------------------------------

    @Override
    public boolean initializeFromSettingsParameter(String settings) {
        databaseFileName = settings;
        return true; // Everything went right.
    }

    // --------------------------------------------

    @Override
    protected void initializeNewInstance(Dissector newInstance) {
        newInstance.initializeFromSettingsParameter(databaseFileName);
    }

    boolean wantCountryName = false;
    boolean wantCountryIso = false;
    boolean wantSubdivisionName = false;
    boolean wantSubdivisionIso = false;
    boolean wantCityName = false;
    boolean wantPostalCode = false;
    boolean wantLocationLatitude = false;
    boolean wantLocationLongitude = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = outputname.substring(inputname.length() + 1);
        if ("country.name".equals(name)) {
            wantCountryName = true;
            return Casts.STRING_ONLY;
        }
        if ("country.iso".equals(name)) {
            wantCountryIso = true;
            return Casts.STRING_ONLY;
        }
        if ("subdivision.name".equals(name)) {
            wantSubdivisionName = true;
            return Casts.STRING_ONLY;
        }
        if ("subdivision.iso".equals(name)) {
            wantSubdivisionIso = true;
            return Casts.STRING_ONLY;
        }
        if ("city.name".equals(name)) {
            wantCityName = true;
            return Casts.STRING_ONLY;
        }
        if ("postal.code".equals(name)) {
            wantPostalCode = true;
            return Casts.STRING_ONLY;
        }
        if ("location.latitude".equals(name)) {
            wantLocationLatitude = true;
            return Casts.STRING_OR_DOUBLE;
        }
        if ("location.longitude".equals(name)) {
            wantLocationLongitude = true;
            return Casts.STRING_OR_DOUBLE;
        }
        return null;
    }

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

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        String fieldValue = field.getValue();
        if (fieldValue == null || fieldValue.isEmpty()) {
            return; // Nothing to do here
        }

        InetAddress ipAddress = null;
        try {
            ipAddress = InetAddress.getByName(fieldValue);
            if (ipAddress == null) {
                return;
            }
        } catch (UnknownHostException e) {
            return;
        }

        dissect(parsable, inputname, ipAddress);
    }

    // --------------------------------------------

    abstract void dissect(final Parsable<?> parsable, final String inputname, final InetAddress ipAddress) throws DissectionFailure;
}