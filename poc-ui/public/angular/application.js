var mainApplicationModuleName = "poc-ui";

var mainApplicationModule = angular.module(mainApplicationModuleName, ["ngRoute", "thing"]);

mainApplicationModule.config(["$locationProvider",
    function ($locationProvider) {
        $locationProvider.hashPrefix("!");
    }]);

angular.element(document).ready(function() {
    angular.bootstrap(document, [mainApplicationModuleName]);
});