import json
import random
import string
import time
import signal

import paho.mqtt.client as mqtt

KPC_HOST = "mqtt.cloud.kaaiot.com"  # Kaa Cloud plain MQTT host
KPC_PORT = 1883  # Kaa Cloud plain MQTT port

APPLICATION_VERSION = "c8k92ktah5mk2gf05gag-version1"  # Paste your application version
ENDPOINT_TOKEN = "123456"  # Paste your endpoint token


# ----------------------CommandReceiveClient----------------------
class CommandReceiveClient:

    def __init__(self, client):
        self.client = client
        # MQTT Topic
        self.data_collection_topic = f'kp1/{APPLICATION_VERSION}/dcx/{ENDPOINT_TOKEN}/json/32'

        command_reboot_topic = f'kp1/{APPLICATION_VERSION}/cex/{ENDPOINT_TOKEN}/command/reboot/status'
        self.client.message_callback_add(command_reboot_topic, self.handle_reboot_command)
        self.command_reboot_result_topic = f'kp1/{APPLICATION_VERSION}/cex/{ENDPOINT_TOKEN}/result/reboot'

        command_zero_topic = f'kp1/{APPLICATION_VERSION}/cex/{ENDPOINT_TOKEN}/command/zero/status'
        self.client.message_callback_add(command_zero_topic, self.handle_zero_command)
        self.command_zero_result_topic = f'kp1/{APPLICATION_VERSION}/cex/{ENDPOINT_TOKEN}/result/zero'

    def connect_to_server(self):
        print(f'Connecting to Kaa server at {KPC_HOST}:{KPC_PORT} using application version {APPLICATION_VERSION} and '
              f'endpoint token {ENDPOINT_TOKEN}')
        self.client.connect(KPC_HOST, KPC_PORT, 60)
        print('Successfully connected')

    def disconnect_from_server(self):
        print(f'Disconnecting from Kaa server at {KPC_HOST}:{KPC_PORT}...')
        self.client.loop_stop()
        self.client.disconnect()
        print('Successfully disconnected')

    def handle_reboot_command(self, client, userdata, message):
        print(f'<--- Received "reboot" command on topic {message.topic} \nRebooting...')
        command_result = self.compose_command_result_payload(message)
        print(f'command result {command_result}')
        client.publish(topic=self.command_reboot_result_topic, payload=command_result)
        # With below approach we don't receive the command confirmation on the server side.
        # self.client.disconnect()
        # time.sleep(5)  # Simulate the reboot
        # self.connect_to_server()

    def handle_zero_command(self, client, userdata, message):
        print(f'<--- Received "zero" command on topic {message.topic} \nSending zero values...')
        command_result = self.compose_command_result_payload(message)
        client.publish(topic=self.data_collection_topic, payload=self.compose_data_sample(0))
        client.publish(topic=self.command_zero_result_topic, payload=command_result)

    def compose_command_result_payload(self, message):
        command_payload = json.loads(str(message.payload.decode("utf-8")))
        print(f'command payload: {command_payload}')
        command_result_list = []
        for command in command_payload:
            command_result = {"id": command['id'], "statusCode": 200, "reasonPhrase": "OK", "payload": "Success"}
            command_result_list.append(command_result)
        return json.dumps(
            command_result_list
        )

    def compose_data_sample(self, fuel_level, min_temp, max_temp):
        return json.dumps({
            'timestamp': int(round(time.time() * 1000)),
            'fuelLevel': fuel_level,
            'temperature': random.randint(min_temp, max_temp),
        })


def on_message(client, userdata, message):
    print(f'Message received: topic {message.topic}\nbody {str(message.payload.decode("utf-8"))}')


class SignalListener:
    keepRunning = True

    def __init__(self):
        signal.signal(signal.SIGINT, self.stop)
        signal.signal(signal.SIGTERM, self.stop)

    def stop(self):
        print('Shutting down...')
        self.keepRunning = False


def receive_command():
    # Initiate server connection
    client = mqtt.Client(client_id=''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6)))

    command_receive_client = CommandReceiveClient(client)
    command_receive_client.connect_to_server()

    client.on_message = on_message

    # Start the loop
    client.loop_start()

    fuel_level, min_temp, max_temp = 100, 95, 100

    # Send data samples in loop
    listener = SignalListener()
    while listener.keepRunning:

        payload = command_receive_client.compose_data_sample(fuel_level, min_temp, max_temp)

        result = command_receive_client.client.publish(topic=command_receive_client.data_collection_topic, payload=payload)
        if result.rc != 0:
            print('Server connection lost, attempting to reconnect')
            command_receive_client.connect_to_server()
        else:
            print(f'--> Sent message on topic "{command_receive_client.data_collection_topic}":\n{payload}')

        time.sleep(3)

        fuel_level = fuel_level - 0.3
        if fuel_level < 1:
            fuel_level = 100

    command_receive_client.disconnect_from_server()


if __name__ == '__main__':
    receive_command()
