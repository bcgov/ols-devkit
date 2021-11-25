Description
-----------
Project:           geomark
Title:             Geomark Web Service
Version:           6.0.0

Software/Hardware Requirements
------------------------------
Oracle:                       Oracle 10+
Java:                         11+
Maven:                        3.6.1+
App Server:                   Tomcat 9+
App Server Additional Memory: 50MB

1. Configuration Files
----------------------

CPF requires a configuration file on each server.

Property                             Description
-------------------------------      ------------------------------------------
geomarkDataSourceUrl                 The JDBC URL to the geomark database
geomarkDataSourcePassword            The password for the PROXY_GEOMARK_WEB user account
geomarkService.maxGeomarkAgeDays     The maximum number of days before a geomark expires
geomarkService.maxVertices           The maximum number of vertices in a geomark's geometry

Create the directory and configuration file.

NOTE: Configuration for delivery, test and production can be managed in
subversion https://apps.bcgov/svn/geomark/config/ and checked out to this directory.

mkdir -p /apps/config/geomark
cd /apps/config/geomark
vi geomark.properties

Sample Values
-------------
The latest sample config file can be obtained from:

https://apps.bcgov/svn/geomark/config/delivery/trunk/geomark.properties

It contains the values for the delivery environment. Verify that the test and production versions
are similar.

2. Ministry Continuous Integration System
-----------------------------------------

If it does not already exist Construct a new new deploy job using the Ministry
Continuous Integration System.

http://apps.gov.bc.ca/gov/standards/index.php/Migration_Task_with_CIS

Job name:                       revolsys-geomark-deploy
Subversion:                     http://apps.bcgov/svn/geomark/source/trunk
MVN Goals and options:          clean install
Ministry Artifacts Repository:  Yes
Deploy to Tomcat:               Yes

3. Compilation & Deployment
---------------------------

Build the revolys-geomark-deploy job using the Ministry Continuous Integration
System.

4. Notification
---------------

Notify all developers and contributors listed in the pom.xml that the deployment
is complete.

5. Perform Release
------------------
 
This step is performed during migration of an application to the production
environment. The migration occurs after the developer has tested the application
in the delivery environment and the business area have completed the UAT in the test environment.
 
Use the Ministry Continuous Integration System to tag the version in Subversion,
build the release version and deploy it to the Ministry Artifacts Repository.
