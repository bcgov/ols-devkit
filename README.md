[![img](https://img.shields.io/badge/Lifecycle-Stable-97ca00)](https://github.com/bcgov/repomountie/blob/master/doc/lifecycle-badges.md)

## OLS Developer Toolkit

The [gh-pages branch](https://github.com/bcgov/ols-devkit/tree/gh-pages) of this repository is home to the OLS Developer Toolkit which includes web applications aimed primarily at developers so they can see how to integrate the [BC Address Geocoder](https://www2.gov.bc.ca/gov/content?id=118DD57CD9674D57BDBD511C2E78DC0D), [BC Route Planner](https://www2.gov.bc.ca/gov/content?id=9D99E684CCD042CD88FADC51E079B4B5) and the [Geomark Web Service](https://www2.gov.bc.ca/gov/content?id=F6BAF45131954020BCFD2EBCC456F084) into their own web apps. Applications can also be used by end users directly or in demonstrations to end users to explain what our services can do. The repository also contains Python scripts that are aimed solely at developers seeking to integrate our services into their automated workflows.

|Name|Type|Description|Audience
|----|:----:|----|----|
[Address Autocompletion Demo](https://bcgov.github.io/ols-devkit/examples/address_autocomplete.html)|Web app|Demonstrates how to integrate the geocoder's autocompletion capability into a web app; uses javascript and leaftlet|Web app developers
[Address List Editor](https://bcgov.github.io/ols-devkit/ale/)|Web app|Allows anyone to clean and geocode a list of up to 1,000 addresses; uses Javascript and Leaflet|End users
[Batch Address List Interval Splitter](https://github.com/bcgov/ols-devkit/tree/gh-pages/ali)|Python script|Python Script takes an output file from the Batch Geocoder and produces a series of CSV files based on score intervals of 10 | developers|
[Batch Address List Metrics](https://github.com/bcgov/ols-devkit/tree/gh-pages/alm)|Python script|Python Script provides summary stats based on an output file from the Batch Geocoder.| developers|
[Batch Address List Submitter](https://github.com/bcgov/ols-devkit/tree/gh-pages/als)|Python script|Python Script submits an address list to the Batch Geocoder plugin running in the Concurrent Processing Framework and retrieves the results| developers|
[Distance Between Pairs](https://github.com/bcgov/ols-devkit/tree/gh-pages/bps)|Python script and sample input files| Script computes distances by road between a list of source points and a list of destination points|developers|
[Geomark Web Service Requests](https://github.com/bcgov/ols-devkit/tree/gh-pages/geomark/scripts)|Python script| A collection of scripts to submit Geomark requests and to add Geomarks to a group.| developers|
[Location Services In Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html)|Web app|Demonstrates how to hook up the geocoder and route planner to a Javascript/Leaflet web app|Web app developers, Location Services engagement staff, end users|


Have a look at the [OLS Developer Toolkit Roadmap](https://github.com/bcgov/ols-devkit/blob/gh-pages/ols-devkit-roadmap.md).
Also check out [this demonstration of interactive directions](https://github.com/bcgov/ols-devkit/blob/gh-pages/interactive-directions.md)
