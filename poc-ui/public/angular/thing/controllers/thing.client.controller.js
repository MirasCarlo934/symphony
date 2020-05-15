angular.module("thing").controller("ThingController", ["$scope", "$http", "$location", function($scope, $http, $location) {
    let uid = $location.path().split("/")[1];
    $http.get(appProperties.serverURL + ":" + appProperties.ports.core + "/things/" + uid).then(
        function(response) {
            console.log(data);
        }
    )
}]);