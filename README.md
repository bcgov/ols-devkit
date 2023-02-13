[![img](https://img.shields.io/badge/Lifecycle-Stable-97ca00)](https://github.com/bcgov/repomountie/blob/master/doc/lifecycle-badges.md)

## OLS Developer Toolkit

The [gh-pages branch](https://github.com/bcgov/ols-devkit/tree/gh-pages) of this repository is home to the OLS Developer Toolkit which includes web applications aimed primarily at developers so they can see how to integrate ols-geocoder and ols-router into their own web apps. Applications can also be used by end users directly or in demos to end users to explain what our services actually do. The repository also contains Python scripts that are aimed solely at developers seeking to integrate the ols-geocoder and ols-route-planner into their automated workflows.

|Name|Type|Description|Audience
|----|:----:|----|----|
[Location Services In Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html)|Web app|Demonstrates how to hook up the geocoder and route planner to a Javascript/Leaflet web app|Web app developers, Location Services engagement staff, end users|
[Address List Editor](https://bcgov.github.io/ols-devkit/ale/)|Web app|Allows anyone to clean and geocode a list of up to 1,000 addresses; uses Javascript and Leaflet|End users
[Batch Address List Metrics](https://github.com/bcgov/ols-devkit/tree/gh-pages/alm)|Web page containing Python script|Python Script provides summary stats based on an output file from the Batch Geocoder.| Workflow automation developers|
[Batch Address List Submitter](https://github.com/bcgov/ols-devkit/tree/gh-pages/als)|Web page containing Python script|Python Script submits an address list to the Batch Geocoder plugin running in the Concurrent Processing Framework and retrieves the results| Workflow automation developers|
[Between Pairs Script](https://github.com/bcgov/ols-devkit/tree/gh-pages/bps)|Web page containing Python script|Python Script submits a betweenPairs request to the BC Route Planner. Includes sample input and output files. | Workflow automation developers|
[Address Autocompletion Demo](https://bcgov.github.io/ols-devkit/examples/address_autocomplete.html)|Web app|Demonstrates how to integrate the geocoder's autocompletion capability into a web app; uses javascript and leaftlet|Web app developers
[Distance Between Pairs](https://github.com/bcgov/ols-devkit/tree/gh-pages/bps)|Directory containing Python script and sample input files| Script computes distances by road between a list of source points and a list of destination points|Workflow automation developers|

Have a look at the [OLS Developer Toolkit Roadmap](https://github.com/bcgov/ols-devkit/blob/gh-pages/ols-devkit-roadmap.md).
Also check out [this demonstration of interactive directions](https://github.com/bcgov/ols-devkit/blob/gh-pages/interactive-directions.md)
