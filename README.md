Dissect IP using GeoIP2 information
===
This project is a custom dissector for [LogParser](https://github.com/nielsbasjes/logparser) that uses
the [MaxMind](http://www.maxmind.com) GeoIP2 data to dissect IP addresses into things like Country, City, ASN, etc.

This dissector has not been made part of the main library because of the licenses of the Maxmind Java API libraries.
I want to be able to use the logparser with tools like Pig and Drill and as such bundeling a GPL library is a no go.

Why only GeoIP2?
--
MaxMind has two APIs active with two different fileformats.
Because the legacy GeoIP Java API can only read the database file using a java.io.File class it was too hard
to build a class that can load these databases when running on a distributed system (like Hadoop/Pig/Flink).
Since that is also the legacy format I simply decided to skip it.

Where are the datafiles?
---
Simple: I didn't include them.

The data is owned by MaxMind and in order to use it you must either purchase a license for 'accurate' GeoIP2
data or download a 'slightly less accurate' free GeoLite2 version.

See http://dev.maxmind.com/ for the both the paid GeoIP2 and the free GeoLite2 downloadable databases.

I personally install and run the geoipupdate tool.

http://dev.maxmind.com/geoip/geoipupdate/

For the unit tests it is assumed that the following files exist: 

    /var/lib/GeoIP/GeoLite2-City.mmdb
    /var/lib/GeoIP/GeoLite2-Country.mmdb
    /var/lib/GeoIP/GeoLite2-ASN.mmdb

You can achieve this by installing geoipupdate tool with the config file /etc/GeoIP.conf
 
    # The following UserId and LicenseKey are required placeholders:
    UserId 999999
    LicenseKey 000000000000
    ProductIds GeoLite2-City GeoLite2-Country GeoLite2-ASN 

Building it
---

Effectively you install Apache Maven and the jdk and run  

    mvn clean package -DskipTests=true
    
should build the software. 

This commands builds without the tests because I expect that some unit tests will fail because the data will change over time.
Since I didn't include the datafiles this is a bit of a problem.

How do I use it?
---

In Apache Pig you can do something like this now:

    Clicks = 
           LOAD 'ip.log' 
           USING nl.basjes.pig.input.apachehttpdlog.Loader(
                   '"%h"',
                   'IP:connection.client.host',
                   
           '-load:nl.basjes.parse.httpdlog.dissectors.GeoIPCityDissector:/var/lib/GeoIP/GeoLite2-City.mmdb',
                   'STRING:connection.client.host.country.name',
                   'STRING:connection.client.host.country.iso',
                   'STRING:connection.client.host.subdivision.name',
                   'STRING:connection.client.host.subdivision.iso',
                   'STRING:connection.client.host.city.name',
                   'STRING:connection.client.host.postal.code',
                   'STRING:connection.client.host.location.latitude',
                   'STRING:connection.client.host.location.longitude',
    
           '-load:nl.basjes.parse.httpdlog.dissectors.GeoIPASNDissector:/var/lib/GeoIP/GeoLite2-ASN.mmdb',
                   'ASN:connection.client.host.asn.number',
                   'STRING:connection.client.host.asn.organization'
              )
           AS (
                   connection_client_host:chararray,
    
                   connection_client_host_country_name:chararray,
                   connection_client_host_country_iso:chararray,
                   connection_client_host_subdivision_name:chararray,
                   connection_client_host_subdivision_iso:chararray,
                   connection_client_host_city_name:chararray,
                   connection_client_host_postal_code:chararray,
                   connection_client_host_location_latitude:double,
                   connection_client_host_location_longitude:double,
    
                   connection_client_host_asn_number:long,
                   connection_client_host_asn_organization:chararray
              )


