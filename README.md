# ReliableUdp


Executar python ReliableUdpSender.py

O cliente utiliza o protocolo inventado "ReliableUdp", inicia uma conexão com a flag HELLO.
O servidor retorna com um HELLO ACK. Depois disso está apto a enviar os pacotes recebendo
uma mensagem de confirmação (ACK). O cliente sinaliza que não há mais pacotes a enviar com um BYE
e o servidor finaliza com um FIN.
