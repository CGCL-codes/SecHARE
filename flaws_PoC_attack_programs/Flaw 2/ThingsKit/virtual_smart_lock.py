#!/usr/bin/python3
import random
import string
from multiprocessing import Process
import paho.mqtt.client as mqtt

HOST = "demo.thingskit.com"
PORT = 1883

# MQTT Username
username = "1U7VoSwVMrI537Xmw4Ry"

# Default MQTT Topic
telemetryTopic = "v1/devices/me/telemetry"
subscribeTopic = "#"


# main listening process
def main_loop(topic, user):
    client_id = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
    client = mqtt.Client(client_id)
    client.username_pw_set(user)
    client.on_connect = on_connect_main
    client.on_message = on_message
    client.connect(HOST, PORT, 60)
    client.subscribe(topic, qos=2)
    client.loop_forever()


def on_connect_main(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    if rc != 0:
        print('Server connection lost')
        return
    print("Connect Success!")
    
    # send data to the cloud
    payload = "{'lock_state':'close'}"
    client.publish(telemetryTopic, payload=payload, qos=0, retain=False)
    print(f'--> Sent message on topic "{telemetryTopic}":\n{payload}')


def on_message(client, userdata, msg):
    # receive data from the cloud
    print("topic" + ":" + msg.topic + "message:" + msg.payload.decode("utf-8") + "qoc" + ":" + str(
        msg.qos) + "connected!")


if __name__ == '__main__':
    listenP = Process(target=main_loop, args=(subscribeTopic, username))
    listenP.start()
