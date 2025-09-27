# FAQ
#### How do I clear all data?
Stop the Phone Home Detector, clear the stored results and then restart:
```
sudo systemctl stop phone-home-detector
sudo rm /usr/share/phone-home-detector/results.xml
sudo systemctl start phone-home-detector
```
#### Are there plans to package for other distributions?
Yes, currently working out priorities.

####  How does the performance of a hotspot compare with a real router?
The Phone Home Detector shouldn't have a noticeable impact on the performance of the hotspot, but I would be interested in hearing if it does.
The hotspot itself will undoubtedly not be as performant as a purpose built router.
However, I have tested the setup running on a Thinkpad T490 with a client connected to the hotspot streaming Youtube at 1440p HD with no issues.

#### Does the application allow the data to be inspected?
No. Use Wireshark after you know the IP addresses of interest.

