var mainApplicationModuleName = "poc-ui";

var mainApplicationModule = angular.module(mainApplicationModuleName, ["ngRoute", "ngmqtt", "thing", "angular-uuid"]);

mainApplicationModule.config(["$locationProvider",
    function ($locationProvider) {
        $locationProvider.hashPrefix("!");
    }]);

angular.element(document).ready(function() {
    angular.bootstrap(document, [mainApplicationModuleName]);
});