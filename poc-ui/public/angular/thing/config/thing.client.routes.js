angular.module("thing").config(["$routeProvider", function($routeProvider) {
    $routeProvider.when("/:uid", {
        templateUrl: "/angular/thing/views/thing.client.view.html"
    })
}]);