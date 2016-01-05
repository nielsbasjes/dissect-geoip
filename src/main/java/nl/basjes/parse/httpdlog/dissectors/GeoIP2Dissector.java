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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.maxmind.db.NodeCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;
import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GeoIP2Dissector extends Dissector {

    private static final int LRU_CACHE_SIZE = 8192;

    private static final String INPUT_TYPE = "IP";

    private String databaseFileName;

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
    boolean wantLocationTimezone = false;

    @Override
    public EnumSet<Casts> prepareForDissect(final String inputname, final String outputname) {
        String name = outputname;
        if (!inputname.isEmpty()) {
            name = outputname.substring(inputname.length() + 1);
        }

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
        if ("location.timezone".equals(name)) {
            wantLocationTimezone = true;
            return Casts.STRING_ONLY;
        }
        return null;
    }

    // --------------------------------------------

    @Override
    public void dissect(final Parsable<?> parsable, final String inputname) throws DissectionFailure {
        final ParsedField field = parsable.getParsableField(INPUT_TYPE, inputname);

        String fieldValue = field.getValue().getString();
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

        // City is the 'Country' + more details.
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
                parsable.addDissection(inputname, "STRING", "location.latitude", location.getLatitude());
            }
            if (wantLocationLongitude) {
                parsable.addDissection(inputname, "STRING", "location.longitude", location.getLongitude());
            }
            if (wantLocationTimezone) {
                parsable.addDissection(inputname, "STRING", "location.timezone", location.getTimeZone());
            }
        }
    }

    // --------------------------------------------

    private DatabaseReader reader;

    @Override
    public void prepareForRun() {
        // A filename pointing to your GeoIP2 or GeoLite2 database file
        Path databaseFilePath = new Path(databaseFileName);
        Configuration configuration = new Configuration();
        try {
            FSDataInputStream dataInputStream = databaseFilePath
                    .getFileSystem(configuration)
                    .open(databaseFilePath);

            reader = new DatabaseReader
                    .Builder(dataInputStream)
                    .fileMode(Reader.FileMode.MEMORY)
                    .withCache(new LRUCache(LRU_CACHE_SIZE))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------------------------------------------

    private class LRUCache implements NodeCache {

        private final LRUMap<Integer, JsonNode> cache;

        public LRUCache(int capacity) {
            this.cache = new LRUMap<>(capacity);
        }

        @Override
        public JsonNode get(int key, Loader loader) throws IOException {
            Integer k = key;
            JsonNode value = cache.get(k);
            if (value == null) {
                value = loader.load(key);
                cache.put(k, value);
            }
            if (value instanceof ContainerNode) {
                value = value.deepCopy();
            }
            return value;
        }
    }
}
