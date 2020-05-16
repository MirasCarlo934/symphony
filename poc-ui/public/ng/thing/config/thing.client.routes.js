angular.module("thing").config(["$routeProvider", function($routeProvider) {
    $routeProvider.when("/:uid", {
        templateUrl: "/ng/thing/views/thing.client.view.html"
    })
}]);