appProperties = {
    serverURL: "http://192.168.0.125",
    // serverURL: "http://localhost",
    mqttURL: "ws://192.168.0.125",
    // mqttURL: "ws://localhost",
    ports: {
        core: "8080",
        data: "8083",
        mqtt: "9001"
    }
}

// for express integration
module.exports = appProperties;