
# -*- coding: utf-8 -*-
# Author: Elano
import socket, requests

sock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)

IP = "127.0.0.1"
PORT = 8053
# MESSAGE = "\x46" * 3000
# Ao inves do ping, usar um texto qualquer para simular a entrega de pacotes (pedaços do texto)
MESSAGE = requests.get('https://baconipsum.com/api/?type=meat-and-filler&paras=100&format=text').text
MAX_LENGTH = 1024

def send():
    ACK_COUNT = 1
    # quebra o texto em chunks de MAX_LENGTH
    if len(MESSAGE) > MAX_LENGTH:
        buff = [ MESSAGE[i:i + MAX_LENGTH]  for i in range(0,len(MESSAGE),MAX_LENGTH)]
    else:
        buff = [MESSAGE]

    for m in buff:
        data = _send(m)
        print(data + " " + str(ACK_COUNT))
        ACK_COUNT += 1
    data = _send("BYE")
    print(data)
    return

def _send(_m):
    sock.settimeout(5)
    sock.sendto(bytes(_m, "utf-8"), (IP, PORT))
    data, addr = sock.recvfrom(MAX_LENGTH)
    return data.decode('utf-8')

def establish_connection():
    # sock.sendto(bytes("HELLO","utf-8"),(IP,PORT))
    # a,b = sock.recvfrom(MAX_LENGTH)
    # a = a.decode('latin-1')
    response = _send("HELLO")
    print(response)
    if response == "HELLO ACK":
        return True
    else:
        return False


try:
    if establish_connection():
        send()
        sock.close()
except socket.timeout:
    print("Ocorreu um erro ao enviar o pacote.")
except Exception as e:
    print(str(e))
