Dissect IP using GeoIP2 information
===
This project is a custom dissector for [LogParser](https://github.com/nielsbasjes/logparser) that uses
the [MaxMind](http://www.maxmind.com) GeoIP2 data to dissect IP addresses into things like Country, City, etc.

This dissector has not been made part of the main library because of the licenses of the Maxmind Java API libraries.
I want to be able to use the logparser with tools like Pig and Drill and as such bundeling a GPL library is a no go.

Why only GeoIP2?
--
MaxMind has two APIs active with two different fileformats.
Because the legacy GeoIP Java API can only read the database file using a java.io.File class it was too hard
to build a class that can load these databases when running on a distributed system (like Hadoop/Spark/Flink).
Since that is also the legacy format I simply decided to skip it.

Where are the datafiles?
---
Simple: I didn't include them.

The data is owned by MaxMind and in order to use it you must either purchase a license for 'accurate' GeoIP2
data or download a 'slightly less accurate' free GeoLite2 version.

See http://dev.maxmind.com/ for the both the paid GeoIP2 and the free GeoLite2 downloadable databases.

I personally install and run the geoipupdate tool.

http://dev.maxmind.com/geoip/geoipupdate/

How do I use it?
---
TODO
For now: See the unit tests in the code.

