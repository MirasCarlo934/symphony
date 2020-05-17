var mainApplicationModuleName = "poc-ui";

var mainApplicationModule = angular.module(mainApplicationModuleName, ["ngRoute", "ngmqtt", "chart.js", "thing"]);

mainApplicationModule.config(["$locationProvider",
    function ($locationProvider) {
        $locationProvider.hashPrefix("!");
    }]);

angular.element(document).ready(function() {
    angular.bootstrap(document, [mainApplicationModuleName]);
});