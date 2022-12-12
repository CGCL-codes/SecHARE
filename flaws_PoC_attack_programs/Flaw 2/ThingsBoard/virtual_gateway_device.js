const mqtt = require('mqtt');

const thingsboardHost = "demo.thingsboard.io";
const accessToken = "JCA35IyPWD4V9ZZpjlHh";

// Default topics.
const connectTopic = 'v1/gateway/connect';
const disconnectTopic = 'v1/gateway/disconnect';
const attributesTopic = 'v1/gateway/attributes';
const rpcTopic = 'v1/gateway/rpc';


// MQTT Username
console.log('Connecting to: %s using MQTT username: %s', thingsboardHost, accessToken);
let client = mqtt.connect('mqtt://' + thingsboardHost, {username: accessToken});

client.on('connect', function () {
    console.log('Client connected!');
	
	// Inform cloud that sub-devices are connected to the Gateway
	client.publish(connectTopic, JSON.stringify({
        'device': 'Device A',
    }));
	client.publish(connectTopic, JSON.stringify({
        'device': 'Device B',
    }));
	
    // Upload fake sub-devices data
    client.publish(attributesTopic, JSON.stringify({
        'Device A': {'attribute1': 'value1', 'attribute1':'42'},
        'Device B': {'attribute1':'value1', 'attribute1':'42'}
    }));
	
    // Subscribe to RPC commands from the cloud
    client.subscribe(rpcTopic);

    // DoS, inform cloud that device is disconnected from the Gateway
    client.publish(disconnectTopic, JSON.stringify({
        'device': 'Device B',
    }));
});

client.on('message', function (topic, message) {
	
	// receive information from the cloud
    console.log('request.topic: ' + topic);
    console.log('request.body: ' + message.toString());
})


// Catches ctrl+c event
process.on('SIGINT', function () {
    console.log();
    console.log('Disconnecting...');
    client.end();
    console.log('Exited!');
    process.exit(2);
});

// Catches uncaught exceptions
process.on('uncaughtException', function (e) {
    console.log('Uncaught Exception...');
    console.log(e.stack);
    process.exit(99);
});
