This is intended to become a dissector for logparser that allows using the Maxmind GeoIP.

Current status: Doesn't work yet.
Main problem that needs to tackled is how to read and use the database file on a distributed system (which IS the main usecase).

This has not been made part of the main library because of the licenses of the Maxmind libraries.
I want to be able to use the logparser with tools like Pig and Drill and as such bundeling a GPL library is a no go.