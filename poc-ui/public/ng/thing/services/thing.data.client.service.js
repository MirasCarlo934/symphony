thingModule.service("data", ["$http", function($http) {

    this.getAttributeValueRecords = function(thingUID, aid, from, to) {
        if (to != null) {
            return $http.get(appProperties.serverURL + ":" + appProperties.ports.data +
                "/data/attributeValueRecords/search/findByThingAndAidBetween?thing=" + thingUID + "&aid=" + aid +
                "&from=" + from.toISOString() + "&to=" + to.toISOString());
        } else {
            return $http.get(appProperties.serverURL + ":" + appProperties.ports.data +
                "/data/attributeValueRecords/search/findByThingAndAidFrom?thing=" + thingUID + "&aid=" + aid +
                "&from=" + from.toISOString());
        }
    }

    this.getAttributeValueStats = function(thingUID, aid, from, to) {
        let url = appProperties.serverURL + ":" + appProperties.ports.data +
            "/data/attributeValueRecords/stats/byDate?thing=" + thingUID + "&aid=" + aid +
            "&from=" + from.toISOString();
        if (to != null) {
            url += "&to=" + to.toISOString();
        }
        return $http.get(url);
    }
}]);