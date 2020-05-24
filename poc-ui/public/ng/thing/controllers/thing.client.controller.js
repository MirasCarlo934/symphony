/*
    $scope definition:
    {
        thing: thingObj
        charts: {
            [aid]: {
                labels:
                series:
                data:
            }
        }
    }
 */
angular.module("thing").controller("ThingController", ["$scope", "$http", "$location", "ngmqtt", "uuid", function($scope, $http, $location, ngmqtt, uuid) {
    // $scope = {
    //     thing: null,
    //     charts: {
    //         aid: {
    //             labels: null,
    //             series: null,
    //             data: null
    //         }
    //     }
    // }
    let uid = $location.path().split("/")[1];
    const mqttTopic = "things/" + uid;
    const attributeValueTopics = "things/" + uid + "/attributes/+/value";
    const bmTopic = "BM/" + uid;
    let options = {
        clientId: uuid.v4(),
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

    // get resources from server
    $http.get(appProperties.serverURL + ":" + appProperties.ports.core + "/things/" + uid).then( (response) => {
        $scope.thing = response.data;
    });

    // chart functions
    $scope.loadRecordsChart = function(aid) {
        $http.get(appProperties.serverURL + ":" + appProperties.ports.data +
            "/data/attributeValueRecords/search/findByThingAndAid?thing=" + $scope.thing.uid + "&aid=" + aid + "&size=50").then(
            (response) => {
                let $chart = $("#" + aid + "-chart-records");
                let records = response.data._embedded.attributeValueRecords;
                let labels = [];
                let data = [];
                records.forEach( (record) => {
                   labels.push(record.timestamp);
                   data.push(record.value);
                });
                labels.reverse();
                data.reverse();
                new Chart($chart, {
                    type: 'line',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: 'value',
                            data: data,
                            fill: false,
                            steppedLine: true,
                            borderColor: "red"
                        }]
                    },
                    options: {
                        scales: {
                            yAxes: [{
                                ticks: {
                                    beginAtZero: true
                                }
                            }]
                        }
                    }
                });
        });
    }

    // view functions
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

    // for angular-charts testing
    $scope.labels = ["January", "February", "March", "April", "May", "June", "July"];
    $scope.series = ['Series A', 'Series B'];
    $scope.data = [
        [65, 59, 80, 81, 56, 55, 40],
        [28, 48, 40, 19, 86, 27, 90]
    ];
    $scope.datasetOverride = [{ yAxisID: 'y-axis-1' }, { yAxisID: 'y-axis-2' }];
    $scope.options = {
        scales: {
            yAxes: [
                {
                    id: 'y-axis-1',
                    type: 'linear',
                    display: true,
                    position: 'left'
                },
                {
                    id: 'y-axis-2',
                    type: 'linear',
                    display: true,
                    position: 'right'
                }
            ]
        }
    };
}]);