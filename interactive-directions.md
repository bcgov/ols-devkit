This tutorial demonstrates the interactive directions capability of the Location Services in Action application.

To understand a route, its often necessary to look at it from different zoom levels. The Location Services in Action app supports this by allowing you to control the map view from the Directions panel. Here's a demonstration.

1. Start up [Location Services in Action](https://bcgov.github.io/ols-devkit/ols-demo/index.html)
2. Select the Route tab
3. Add two random addresses within the current map view by selecting the Jump at Random icon (\*) next to each waypoint

![image](https://user-images.githubusercontent.com/11318574/134563611-80f8b2ea-75e1-4946-b201-d18931619f56.png)

4. Click on the first line of Directions. The line will be highlighted and the map will zoom to the starting intersection of the first route leg. The intersection will also be highlighted with a blue ring. 

![image](https://user-images.githubusercontent.com/11318574/134563854-65cca3cc-c5bc-47dc-aaf6-c2f3014abdce.png)

5. Click again on the first line of Directions. The highlight will disappear and the map will zoom back to the full extent of the route.

![image](https://user-images.githubusercontent.com/11318574/134564158-a09cf761-b61a-4951-a7d6-9cab8d3fa041.png)

6. Every line in Directions behaves the same way. Here's the second line:

![image](https://user-images.githubusercontent.com/11318574/134565453-19887966-305e-4cac-8e58-d432311de90d.png)

To make interactive directions work, the demo app uses the point coordinates embedded in each line of Directions.

Here is the route planner request that generated this route:

https://router.api.gov.bc.ca/directions.json?apikey=someAPIkey&points=-123.2770486%2C48.4631703%2C-123.3831595%2C48.4858357&criteria=fastest&enable=gdf%2Cldf%2Ctr%2Cxc%2Ctc%2C&departure=2021-09-23T11%3A19%3A00-07%3A00&correctSide=false&roundTrip=false


and here is the response showing the point coordinates in each line entry in the Directions list:

```
{
 "enable": "gdf,ldf,tc,tr,xc",
 "executionTime": 8,
 "timeText": "14 minutes 40 seconds",
 "srsCode": 4326,
 "directions": [
  {
   "distance": 0.368,
   "type": "START",
   "time": 35,
   "text": "Head northwest on Lockehaven Dr for 350 m (35 seconds)",
   "name": "Lockehaven Dr",
   "heading": "NORTHWEST",
   "point": [
    -123.27708,48.46315]
  },{
   "distance": 0.063,
   "type": "TURN_RIGHT",
   "time": 17,
   "text": "Turn right onto Queenswood Dr for 65 m (17 seconds)",
   "name": "Queenswood Dr",
   "point": [
    -123.28139,48.46211]
  },{
   "distance": 0.569,
   "type": "TURN_SHARP_LEFT",
   "time": 63,
   "text": "Turn sharp left onto Telegraph Bay Rd for 550 m (1 minute 3 seconds)",
   "name": "Telegraph Bay Rd",
   "point": [
    -123.28187,48.46258]
  },{
   "distance": 0.833,
   "type": "CONTINUE",
   "time": 81,
   "text": "Continue onto Cadboro Bay Rd for 850 m (1 minute 21 seconds)",
   "name": "Cadboro Bay Rd",
   "point": [
    -123.28783,48.45953]
  },{
   "distance": 0.986,
   "type": "TURN_RIGHT",
   "time": 72,
   "text": "Turn right onto Sinclair Rd for 1,000 m (1 minute 12 seconds)",
   "name": "Sinclair Rd",
   "point": [
    -123.2976,48.461]
  },{
   "distance": 0.05,
   "type": "TURN_RIGHT",
   "time": 15,
   "text": "Turn right onto Finnerty Rd for 50 m (15 seconds)",
   "name": "Finnerty Rd",
   "point": [
    -123.30754,48.46687]
  },{
   "distance": 4.461,
   "type": "TURN_SLIGHT_RIGHT",
   "time": 364,
   "text": "Turn slight right onto McKenzie Ave for 4.5 km (6 minutes 4 seconds)",
   "name": "McKenzie Ave",
   "point": [
    -123.30772,48.46716]
  },{
   "distance": 2.007,
   "type": "TURN_SLIGHT_RIGHT",
   "time": 187,
   "text": "Turn slight right onto Quadra St for 2 km (3 minutes 7 seconds)",
   "name": "Quadra St",
   "point": [
    -123.36459,48.47003]
  },{
   "distance": 0.281,
   "type": "TURN_LEFT",
   "time": 29,
   "text": "Turn left onto Caen Rd for 300 m (29 seconds)",
   "name": "Caen Rd",
   "point": [
    -123.38048,48.48433]
  },{
   "distance": 0.158,
   "type": "TURN_RIGHT",
   "time": 16,
   "text": "Turn right onto Dieppe Rd for 150 m (16 seconds)",
   "name": "Dieppe Rd",
   "point": [
    -123.384,48.48457]
  },{
   "type": "FINISH",
   "text": "Finish!",
   "point": [
    -123.38312,48.48582]
  }],
 "disclaimer": "https://www2.gov.bc.ca/gov/content?id=79F93E018712422FBC8E674A67A70535",
 "criteria": "fastest",
 "copyrightNotice": "Copyright 2020 Province of British Columbia - Open Government License",
 "points": [
  [
   -123.27705,48.46317],[
   -123.38316,48.48584]],
 "copyrightLicense": "https://www2.gov.bc.ca/gov/content?id=A519A56BC2BF44E4A008B33FCF527F61",
 "distance": 9.777,
 "routeFound": true,
 "version": "2.1.3",
 "routeDescription": null,
 "searchTimestamp": "2021-09-24 0:31:29",
 "distanceUnit": "km",
 "time": 880.431093361275,
 "notifications": [],
 "privacyStatement": "https://www2.gov.bc.ca/gov/content?id=9E890E16955E4FF4BF3B0E07B4722932",
 "route": [
  [
   -123.27708,48.46315],[
   -123.27735,48.46331],[
   -123.27765,48.46339],[
   -123.27797,48.4634],[
   -123.27833,48.46335],[
   -123.27882,48.46322],[
   -123.27965,48.46292],[
   -123.28139,48.46211],[
   -123.28178,48.46245],[
   -123.28187,48.46258],[
   -123.28205,48.46239],[
   -123.28259,48.46207],[
   -123.2833,48.46171],[
   -123.28494,48.46067],[
   -123.28543,48.46036],[
   -123.28608,48.45996],[
   -123.28624,48.45988],[
   -123.28636,48.45983],[
   -123.28672,48.45966],[
   -123.28752,48.45954],[
   -123.28783,48.45953],[
   -123.28807,48.45958],[
   -123.28901,48.45993],[
   -123.29017,48.46033],[
   -123.29062,48.46045],[
   -123.29094,48.46056],[
   -123.29139,48.4608],[
   -123.29303,48.46166],[
   -123.29357,48.46193],[
   -123.29371,48.46197],[
   -123.29476,48.46193],[
   -123.29546,48.46183],[
   -123.29664,48.46151],[
   -123.29712,48.46133],[
   -123.2976,48.461],[
   -123.29907,48.46182],[
   -123.29931,48.46195],[
   -123.30021,48.46245],[
   -123.30067,48.46272],[
   -123.301,48.46291],[
   -123.30269,48.46386],[
   -123.30342,48.46426],[
   -123.30528,48.46531],[
   -123.30667,48.4661],[
   -123.30675,48.46616],[
   -123.30754,48.46687],[
   -123.30747,48.46691],[
   -123.30745,48.46697],[
   -123.30743,48.46701],[
   -123.30746,48.46709],[
   -123.30754,48.46713],[
   -123.30762,48.46716],[
   -123.30772,48.46716],[
   -123.30803,48.46738],[
   -123.30868,48.46779],[
   -123.30904,48.46799],[
   -123.3096,48.46829],[
   -123.31032,48.4687],[
   -123.31059,48.46881],[
   -123.31094,48.46893],[
   -123.3114,48.469],[
   -123.31191,48.469],[
   -123.31337,48.46886],[
   -123.31497,48.46871],[
   -123.31587,48.46865],[
   -123.3176,48.46864],[
   -123.31946,48.46862],[
   -123.32042,48.46862],[
   -123.32107,48.46863],[
   -123.32119,48.46859],[
   -123.32301,48.4686],[
   -123.32377,48.4686],[
   -123.32441,48.46859],[
   -123.32459,48.46859],[
   -123.3287,48.46862],[
   -123.33074,48.46863],[
   -123.33083,48.46866],[
   -123.33273,48.46867],[
   -123.33532,48.46869],[
   -123.33966,48.46869],[
   -123.34043,48.46873],[
   -123.34079,48.46882],[
   -123.34112,48.46898],[
   -123.34398,48.47115],[
   -123.34451,48.47144],[
   -123.34495,48.47161],[
   -123.34527,48.4717],[
   -123.34685,48.47197],[
   -123.34806,48.47219],[
   -123.34869,48.47221],[
   -123.35118,48.47198],[
   -123.35248,48.4718],[
   -123.35262,48.47174],[
   -123.35292,48.4717],[
   -123.35509,48.4714],[
   -123.35667,48.47103],[
   -123.35736,48.47094],[
   -123.36121,48.47043],[
   -123.36361,48.47015],[
   -123.36459,48.47003],[
   -123.36493,48.47027],[
   -123.36837,48.47226],[
   -123.36954,48.47295],[
   -123.37018,48.47327],[
   -123.37073,48.47358],[
   -123.37148,48.47425],[
   -123.37218,48.47502],[
   -123.37246,48.47534],[
   -123.37289,48.47584],[
   -123.37339,48.47622],[
   -123.37433,48.47699],[
   -123.37538,48.47784],[
   -123.37593,48.47828],[
   -123.37626,48.47854],[
   -123.37671,48.47897],[
   -123.37692,48.47929],[
   -123.37723,48.47975],[
   -123.37791,48.48081],[
   -123.37805,48.48103],[
   -123.37839,48.48164],[
   -123.37873,48.48224],[
   -123.37889,48.48246],[
   -123.37973,48.48362],[
   -123.38001,48.48393],[
   -123.38048,48.48433],[
   -123.38067,48.48418],[
   -123.38083,48.48413],[
   -123.38137,48.48406],[
   -123.3818,48.48409],[
   -123.38282,48.4844],[
   -123.38334,48.48453],[
   -123.384,48.48457],[
   -123.38399,48.48471],[
   -123.38399,48.4849],[
   -123.38396,48.48498],[
   -123.38393,48.48506],[
   -123.38368,48.48533],[
   -123.38312,48.48582]]
}
```
