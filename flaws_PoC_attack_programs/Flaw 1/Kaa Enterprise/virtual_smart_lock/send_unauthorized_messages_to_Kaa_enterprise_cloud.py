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


# ----------------------SendDateClient----------------------
class SendDateClient:

    def __init__(self, client):
        self.client = client
        # MQTT Topic
        self.data_send_topic = f'kp1/{APPLICATION_VERSION}/dcx/{ENDPOINT_TOKEN}/json'

    def connect_to_server(self):
        print(
            f'Connecting to Kaa server at {KPC_HOST}:{KPC_PORT} using application version {APPLICATION_VERSION} and '
            f'endpoint token {ENDPOINT_TOKEN}')
        self.client.connect(KPC_HOST, KPC_PORT, 60)
        print('Successfully connected')

    def disconnect_from_server(self):
        print(f'Disconnecting from Kaa server at {KPC_HOST}:{KPC_PORT}...')
        self.client.loop_stop()
        self.client.disconnect()
        print('Successfully disconnected')

    def compose_data_sample(self):
        return json.dumps({
            'lock_state': random.getrandbits(1),
        })


def on_message(client, userdata, message):
    print(f'<-- Received message on topic "{message.topic}":\n{str(message.payload.decode("utf-8"))}')


class SignalListener:
    keepRunning = True

    def __init__(self):
        signal.signal(signal.SIGINT, self.stop)
        signal.signal(signal.SIGTERM, self.stop)

    def stop(self):
        print('Shutting down...')
        self.keepRunning = False


def send_data():
    # Initiate server connection
    client = mqtt.Client(client_id=''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6)))
    send_data_client = SendDateClient(client)
    send_data_client.connect_to_server()
    client.on_message = on_message

    # Start the loop
    client.loop_start()

    # Send data samples in loop
    listener = SignalListener()
    while listener.keepRunning:

        payload = send_data_client.compose_data_sample()

        result = send_data_client.client.publish(topic=send_data_client.data_send_topic,
                                                       payload=payload)
        if result.rc != 0:
            print('Server connection lost, attempting to reconnect')
            send_data_client.connect_to_server()
        else:
            print(f'--> Sent message on topic "{send_data_client.data_send_topic}":\n{payload}')

        time.sleep(3)

    send_data_client.disconnect_from_server()


if __name__ == '__main__':
    send_data()
