# Phone Home Detector
This application monitors network traffic to identify programs that are "Phoning Home" to check for updates etc.

To install on Ubuntu 24.04 use:
```
add-apt-repository ppa:viru7/phd
apt update
apt install phone-home-detector
```

## Method
The application works by looking at the data sent to IP addresses in one minute blocks.
The source data can come from one of two source: libpcap or a BPF program.
The source is selected by configuration.

## Functionality
This application has the following broad functionality:
* A service that collects data and analyses it as stated above.
* When running with BPF it also tries to identify the program that is making the connection.
* The logic to identify patterns of network traffic is separate from that which identifies the associated program.
This allows it to run with either the BPF collection mechanism or the more portable libpcap.
* There is command line program that allows a user to query the results.
The interface between the CLI application and the service is via a Unix Domain Socket.
* There is also a web interface which is not mature.
It provides another user interface to the same information and uses the same mechanism to interface with the service.
* When the service stops it saves the results in XML format which are reloaded when it starts.

## Packing and Deployment
This maven project builds fat jars.
A systemd service and scripts to start the service have been created.
These are packaged in a deb.  
 
## Support
The following distributions and kernel versions have been tested:
* Ubuntu 24.04

## Known Issues
* No IPv6 support