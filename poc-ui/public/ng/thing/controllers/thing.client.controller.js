angular.module("thing").controller("ThingController", ["$scope", "$http", "$location", "ngmqtt", function($scope, $http, $location, ngmqtt) {
    let uid = $location.path().split("/")[1];
    const mqttTopic = "things/" + uid;
    const bmTopic = "BM/" + uid;
    let options = {
        clientId: "poc-ui." + uid,
        protocolId: 'MQTT',
        protocolVersion: 4
    };

    ngmqtt.connect(appProperties.mqttURL + ":" + appProperties.ports.mqtt, options);
    ngmqtt.listenConnection("ThingController", () => {
        console.log("connected to MQTT");
        ngmqtt.subscribe(mqttTopic);
    });
    ngmqtt.listenMessage("ThingController", (topic, message) => {
        $scope.thing = JSON.parse(message.toString())
        $scope.$apply();
    });

    $http.get(appProperties.serverURL + ":" + appProperties.ports.core + "/things/" + uid).then(
        (response) => {
            $scope.thing = response.data;
        }
    )

    // functions
    $scope.updateValue = (aid) => {
        let $valueForm = $("#" + aid + "-valueForm");
        console.log($valueForm.find("input[name='value']").val());
        console.log("updating value: " + $("#" + aid + "-valueForm"));
        // $http.put(appProperties.serverURL + ":" + appProperties.ports.core + "/things/" + uid + "/attributes/" + aid + "/value",
        //     value).then((response) => {
        //         console.log(response.data);
        //     }
        // )
    }

    $scope.toggleValue = (aid) => {
        let val = $scope.thing.attributes.find((attr) => {return attr.aid == aid;}).value
        if (val == 0) val = 1;
        else val = 0;
        ngmqtt.publish(bmTopic + "/attributes/" + aid + "/value", val.toString());
    }
}]);