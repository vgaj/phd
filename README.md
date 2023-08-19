# Purpose
Monitors a NIC in promiscuous mode to identify if something is "Phoning Phone" to check for updates etc.

# Installation
This requires pcap to run so:
* On Ubuntu: apt-get install libpcap-dev
* On Centos: yum install libpcap-devel
* On Mac: brew install libpcap
* On Windows: choco install winpcap

# Building
mvn package

# Running
sudo java -jar target/phonehomedetector-0.0.1-SNAPSHOT.jar 
http://localhost:8080/