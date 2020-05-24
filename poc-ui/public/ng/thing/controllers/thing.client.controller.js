angular.module("thing").controller("ThingController", ["$scope", "$http", "$location", "ngmqtt", "uuid", function($scope, $http, $location, ngmqtt, uuid) {
    $scope.charts = {};
    let uid = $location.path().split("/")[1];
    const thingTopic = "things/" + uid + "/#";
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
        ngmqtt.subscribe(thingTopic);
    });
    ngmqtt.listenMessage("ThingController", (topic, message) => {
        let topicLevels = topic.split("/");

        if (topicLevels.length === 3) { // thing field update
            let uid = topicLevels[1];
            let field = topicLevels[2];
            $scope.thing[field] = message;
        } else if (topicLevels.length === 5) { // attribute field update
            let aid = topicLevels[3];
            let field = topicLevels[4]
            for (const attr of $scope.thing.attributes) {
                if (attr.aid == aid) {
                    attr[field] = message;
                    if (field === "value") {
                        $scope.addLatestRecordToChart(aid, new Date().toISOString(), message);
                    }
                    break;
                }
            }
        }
        $scope.$apply();
    });

    // get resources from server
    $http.get(appProperties.serverURL + ":" + appProperties.ports.core + "/things/" + uid).then( (response) => {
        $scope.thing = response.data;
    });

    // chart functions
    $scope.addLatestRecordToChart = function(aid, timestamp, value) {
        let chart = $scope.charts[aid];
        console.log(timestamp);
        chart.data.datasets.forEach( (dataset) => {
            dataset.data.push({
                x: timestamp,
                y: value
            });
            dataset.data.shift();
        });
        chart.update();
    }
    $scope.loadRecordsChart = function(aid) {
        let dateNow = new Date();
        let yesterday = new Date();
        yesterday.setDate(dateNow.getDate()-1);
        $http.get(appProperties.serverURL + ":" + appProperties.ports.data +
            // "/data/attributeValueRecords/search/findByThingAndAid?thing=" + $scope.thing.uid + "&aid=" + aid).then(
            "/data/attributeValueRecords/search/findByThingAndAidFrom?thing=" + $scope.thing.uid + "&aid=" + aid +
            "&from=" + yesterday.toISOString()).then(
            (response) => {
                let $chart = $("#" + aid + "-chart-records");
                let records = response.data._embedded.attributeValueRecords;
                let data = [];
                records.forEach( (record) => {
                   data.push({
                       x: record.timestamp,
                       y: record.value
                   });
                });
                data.reverse();
                $scope.charts[aid] = new Chart($chart, {
                    type: 'line',
                    data: {
                        datasets: [{
                            label: 'value',
                            data: data,
                            fill: false,
                            steppedLine: true,
                            borderColor: "red"
                        }]
                    },
                    options: {
                        legend: {
                            display: false
                        },
                        scales: {
                            xAxes: [{
                                type: 'time'
                            }],
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