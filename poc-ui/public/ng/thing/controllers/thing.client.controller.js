thingModule.controller("ThingController", ["$log", "$scope", "$http", "$location", "$interval", "ngmqtt", "uuid", "moment", "data", function($log, $scope, $http, $location, $interval, ngmqtt, uuid, moment, dataService) {
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
        $log.info("connected to MQTT");
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

    // get thing resource from server
    $http.get(appProperties.serverURL + ":" + appProperties.ports.core + "/things/" + uid).then( (response) => {
        $scope.thing = response.data;
        $scope.thing.attributes.forEach( (attr) => {
            $scope.charts[attr.aid] = {};
        })
    });

    // chart functions
    $scope.addLatestRecordToChart = function(aid, timestamp, value) {
        let chart = $scope.charts[aid].records;
        chart.data.datasets.forEach( (dataset) => {
            dataset.data.push({
                x: timestamp,
                y: value
            });
            // dataset.data.shift();
        });
        chart.update();
    }
    $scope.loadRecordsChart = function(aid) {
        let attr = $scope.getAttribute(aid);
        let yesterday = new Date();
        yesterday.setDate(yesterday.getDate()-1);
        dataService.getAttributeValueRecords($scope.thing.uid, aid, yesterday).then(
            (response) => {
                let $chart = $("#" + aid + "-chart-records");
                let records = response.data._embedded.attributeValueRecords;
                let data = [];
                records.forEach( (record) => {
                    data.unshift({
                        x: record.timestamp,
                        y: record.value
                    });
                });
                $scope.charts[aid].records = new Chart($chart, {
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
                        responsive: true,
                        maintainAspectRatio: false,
                        title: {
                            display: true,
                            text: "Value over Time"
                        },
                        legend: {
                            display: false
                        },
                        tooltips: {
                            callbacks: {
                                title: function(tooltipItem, data) {
                                    let datasetIndex = tooltipItem[0].datasetIndex;
                                    let index = tooltipItem[0].index;
                                    let title = data.datasets[datasetIndex].data[index].x;
                                    return moment(title).calendar();
                                }
                            }
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
    $scope.loadTimeSpentAtChart = function(aid) {
        const colors = ["red", "blue", "yellow", "green", "purple", "orange", "cyan", "magenta"]
        let yesterday = new Date();
        yesterday.setDate(yesterday.getDate()-1);
        dataService.getAttributeValueStats($scope.thing.uid, aid, yesterday).then( (response) => {
            let $chart = $("#" + aid + "-chart-timeSpentAt");
            let timeSpentAt = response.data.timeSpentAt;
            let labels = []
            let data = [];
            for (let value in timeSpentAt) {
                if (!timeSpentAt.hasOwnProperty(value)) continue;
                labels.unshift(value);
                data.unshift(timeSpentAt[value]);
            }
            $scope.charts[aid].timeSpentAt = new Chart($chart, {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: data,
                        backgroundColor: colors
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    title: {
                        display: true,
                        text: "Time Spent At"
                    },
                    legend: {
                        display: true
                    },
                    tooltips: {
                        callbacks: {
                            label: function(tooltipItem, data) {
                                let label = data.labels[tooltipItem.index] || '';
                                let time = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
                                let timeStr = "";
                                const timeDivisions = [{
                                    millis: 24*60*60*1000, unit: "d"
                                }, {
                                    millis: 60*60*1000, unit: "h"
                                }, {
                                    millis: 60*1000, unit: "m"
                                }, {
                                    millis: 1000, unit: "s"
                                }]

                                if (label) {
                                    label += ': ';
                                }
                                for (let i = 0; i < timeDivisions.length; i++) {
                                    let timeDiv = timeDivisions[i];
                                    if (time / timeDiv.millis > 1) {
                                        timeStr += Math.floor(time/timeDiv.millis) + timeDiv.unit + " ";
                                        time = time % timeDiv.millis;
                                    }
                                }
                                label += timeStr;
                                return label;
                            }
                        }
                    }
                }
            });

            // reload data every minute
            $interval(() => {
                let yesterday = new Date();
                yesterday.setDate(yesterday.getDate()-1);
                dataService.getAttributeValueStats($scope.thing.uid, aid, yesterday).then( (response) => {
                    let timeSpentAt = response.data.timeSpentAt;
                    let data = [];
                    for (let value in timeSpentAt) {
                        if (!timeSpentAt.hasOwnProperty(value)) continue;
                        labels.unshift(value);
                        data.unshift(timeSpentAt[value]);
                    }
                    $scope.charts[aid].timeSpentAt.data.datasets[0].data = data;
                    $scope.charts[aid].timeSpentAt.update;
                });
            }, 60*1000);
        });
    }

    // view functions
    $scope.updateValue = (aid, inputType) => {
        let $valueForm = $("#" + aid + "-valueForm");
        let val;
        if (inputType === "radio") {
            val = $valueForm.find("input[name='value']:checked").val();
        } else if (inputType === "textarea"){
            val = $valueForm.find("textarea[name='value']").val();
        } else {
            val = $valueForm.find("input[name='value']").val();
        }
        ngmqtt.publish(bmTopic + "/attributes/" + aid + "/value", val.toString());
    }
    // for binary data types only
    $scope.toggleValue = (aid) => {
        let val = $scope.thing.attributes.find((attr) => {return attr.aid == aid;}).value
        if (val == 0) val = 1;
        else val = 0;
        ngmqtt.publish(bmTopic + "/attributes/" + aid + "/value", val.toString());
    }

    // utility functions
    $scope.getAttribute = (aid) => {
        for (const attr of $scope.thing.attributes) {
            if (attr.aid == aid) {
                return attr;
            }
        }
    }
}]);