#!/usr/bin/python3
import random
import string
from multiprocessing import Process
import paho.mqtt.client as mqtt

# Default MQTT connection Info.
HOST = "dev.thingspanel.cn"
PORT = 10000
TOPIC = "thingspanel.telemetry"
USERNAME = "guest"
PASSWORD = "guest"

# paste token from cloud‘s client push logs.
TOKEN = "the_temperature"


# main listening process
def main_loop():
    client_id = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
    client = mqtt.Client(client_id)
    client.username_pw_set(USERNAME, PASSWORD)
    client.on_connect = on_connect_main
    client.on_message = on_message
    client.connect(HOST, PORT, 60)
    client.subscribe("#", qos=2)
    client.loop_forever()


# main process
def on_connect_main(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    if rc != 0:
        print('Server connection lost')
        return
    print("Connect Success!")

    # send data to the cloud
    payload = "{\"token\":\"" + TOKEN + "\",\"values\":{\"lock_state\":\"close\"}}"
    client.publish(TOPIC, payload=payload, qos=0, retain=False)


def on_message(client, userdata, msg):
    # receive data from the cloud
    print("topic" + ":" + msg.topic + "message:" + msg.payload.decode("utf-8") + "qoc" + ":" + str(
        msg.qos) + "connected!")


if __name__ == '__main__':
    listenP = Process(target=main_loop)
    listenP.start()

