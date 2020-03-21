
(function() {

L.LayerOpenStreetCam = L.FeatureGroup.extend({

  oscApi: 'https://openstreetcam.org/1.0/list/nearby-photos/',

	cameraConeIcon: new L.Icon({
		iconUrl: 'img/camera_cone.png',
		iconSize: [32, 32],
		iconAnchor: [16, 8]
	}),

	cameraIcon: new L.Icon({
		iconUrl: 'img/camera.png',
		iconSize: [8, 8],
		iconAnchor: [4, 4]
	}),

	options: {
		minZoom: 18,				//min zoom for call data
		maxZoom: 100				//max zoom for call data
	},

	initialize: function(options) {
		L.FeatureGroup.prototype.initialize.call(this, []);
		L.Util.setOptions(this, options);
		this._curReq = null;
	},

	onAdd: function(map) {
		L.FeatureGroup.prototype.onAdd.call(this, map);		//set this._map
    map.on('moveend zoomend', this.refresh, this);

		this.refresh();
	},

	onRemove: function(map) {
		map.off('moveend zoomend', this.refresh, this);
		L.FeatureGroup.prototype.onRemove.call(this, map);

		// for (var i in this._layers) {
		// 	if (this._layers.hasOwnProperty(i)) {
		// 		L.FeatureGroup.prototype.removeLayer.call(this, this._layers[i]);
		// 	}
		// }
	},

	refresh: function() {
		this.clearLayers();
		if(this._map && this._map.getZoom() >= this.options.minZoom
			&& this._map.getZoom() <= this.options.maxZoom) {
			this.update();
		}
	},

	addMarker: function(data) {
    var markerOpts;
		if(data.heading) {
			markerOpts = {icon: this.cameraConeIcon, rotationAngle: data.heading, opacity: 0.5};
		} else {
			markerOpts = {icon: this.cameraIcon, opacity: 0.5};
		}
		marker = new L.Marker([data.lat, data.lng], markerOpts);
		marker.on('click', function(ev) {
			window.open('https://openstreetcam.org/' + data.name, 'openStreetCamWindow')
		});
		this.addLayer(marker);
	},

	update: function() {
  	var center = this._map.getCenter();

		if(this._curReq && this._curReq.abort)
			this._curReq.abort();		//prevent parallel requests

		var that = this;
		this._curReq = $.ajax({
			url: this.oscApi,
			method: 'POST',
			data: {
				lat: center.lat,
				lng: center.lng,
				radius: 200
			},
			success: function(data) {
				that._curReq = null;
				for (var k in data.currentPageItems) {
	        that.addMarker.call(that, data.currentPageItems[k]);
				}
			}
		});
	}

});

L.layerOpenStreetCam = function (options) {
    return new L.LayerOpenStreetCam(options);
};

}).call(this);
