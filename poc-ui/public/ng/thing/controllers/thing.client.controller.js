angular.module("thing").controller("ThingController", ["$scope", "$http", "$location", "ngmqtt", function($scope, $http, $location, ngmqtt) {
    let uid = $location.path().split("/")[1];
    const mqttTopic = "things/" + uid;
    const attributeValueTopics = "things/" + uid + "/attributes/+/value";
    const bmTopic = "BM/" + uid;
    let options = {
        clientId: "poc-ui." + uid,
        protocolId: 'MQTT',
        protocolVersion: 4
    };

    ngmqtt.connect(appProperties.mqttURL + ":" + appProperties.ports.mqtt, options);
    ngmqtt.listenConnection("ThingController", () => {
        console.log("connected to MQTT");
        // ngmqtt.subscribe(mqttTopic);
        ngmqtt.subscribe(attributeValueTopics);
    });
    ngmqtt.listenMessage("ThingController", (topic, message) => {
        let topicAid = topic.split("/")[3];
        for (const attr of $scope.thing.attributes) {
            if (attr.aid == topicAid) {
                attr.value = message;
                break;
            }
        }
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
        let val = $valueForm.find("input[name='value']").val();
        ngmqtt.publish(bmTopic + "/attributes/" + aid + "/value", val.toString());
    }

    $scope.enterKeyPressUpdateValue = (aid, $event) => {
        if ($event.which === 13) {
            alert("Enter pressed");
        }
    }

    // for binary data types only
    $scope.toggleValue = (aid) => {
        let val = $scope.thing.attributes.find((attr) => {return attr.aid == aid;}).value
        if (val == 0) val = 1;
        else val = 0;
        ngmqtt.publish(bmTopic + "/attributes/" + aid + "/value", val.toString());
    }
}]);