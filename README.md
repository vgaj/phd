# Phone Home Detector
## What is it?
This application monitors network traffic to identify programs that are "phoning home" to check for updates etc.
It aims to be very lightweight by using a BPF program in the kernel to track the network traffic, and then analysing it in a Java userspace program.

## Installation and usage
Currently, the Phone Home Detector is only packaged for Ubuntu 24.04.
To install first add the PPA and ensure it is updated:
```
sudo add-apt-repository ppa:viru7/phd
sudo apt update && sudo apt upgrade && sudo reboot
```
Then install: 
```
sudo apt install phone-home-detector
```
The Phone Home Detector will start running in the background as a systemd service.
To query the results simply run:
```
phone-home-detector
```
There is also a simple web interface available at http://localhost:9080/

## Hotspot mode
The previous (base) installation looks for traffic originating from the local machine.
It is also possible to use the Phone Home Detector in hotspot mode where it will identify traffic originating from other machines and devices in your home.
For this you need to run on a machine with Wi-Fi and a separate internet connection (probably wired ethernet).

The idea is to create a Wi-Fi hotspot sharing the internet connection from the other NIC.
Then connect any machines or devices that you want to monitor to the hotspot.
Phone Home Detector running in hotspot mode will monitor the traffic to identify patterns.

## Hotspot mode setup
Note that this requires **Ubuntu Workstation 24.04**

Standard OS mechanisms are used to set up a hotspot:
* Settings -> WiFi -> Turn On Wi-Fi Hotspot...
* Enter Network Name and Password
* Click Turn On

Then any machines or devices to be monitored need to be connected the Hotspot network.
One method that can be used is to turn off the existing Wi-Fi network and set up the Hotspot with the SSID and password of the existing network allowing devices to connect to it without re-configuration.

At this point Phone Home Detector needs to be reconfigured run in hotspot mode.
This is done by running the following command and following the instructions.
Note that you will need to know the name of the Wi-Fi device that is being used for the Hotspot if there is more than one.
```
sudo phone-home-detector -s
```
The web interface is useful for filtering results.

## Method
The application works by looking at the data sent to each IP address in one minute blocks.
It then looks for patterns in the interval or size of data sent.

The data is captured by a BPF program in the kernel and analysed by a userspace Java program.
In workstation mode it captures outgoing traffic and in hotspot mode it captures incoming traffic on the specified NIC.
In workstation mode it runs an additional BPF program to attempt to identify the process responsible. 


## Other Functionality
* The main Phone Home Detector application runs as a systemd service.
* There is a command line application and a simple web application (also a systemd service) available to query the results.
* The interface between these and the service is via a Unix Domain Socket.
* When the service stops it saves the results in XML format which are reloaded when it starts.

## Packing and Deployment
* This maven project builds fat jars.
* A systemd service and scripts to start the service have been created.
* These are packaged in a deb.
 
## Support
The following distributions and kernel versions have been tested:
* Ubuntu 24.04

## Known Issues
* No IPv6 support