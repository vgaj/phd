#!/bin/bash
# MIT License
#
# Copyright (c) 2022-2025 Viru Gajanayake
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

if [ "$UID" -ne 0 ]; then
    echo "This needs to be run as root"
    exit 1
fi

script_dir="$(dirname "$(realpath "$0")")"
hotspotnic_file="$script_dir/hotspotnic"

echo "Phone Home Detector can run in one of two modes: WORKSTATION or HOTSPOT"
if [[ -f "$hotspotnic_file" ]]; then
    hotspotnic_content="$(cat "$hotspotnic_file")"
fi

if [[ -n "$hotspotnic_content" ]]; then
    echo "Currently it is running in HOTSPOT mode using NIC: $hotspotnic_content"
    new_mode="WORKSTATION"
else
    echo "Currently it is running in WORKSTATION mode"
    new_mode="HOTSPOT"
fi

echo -n "Do you want to change the mode to $new_mode (NB: any existing results will be lost)? (Y/N): "
read -r response
if [[ ! "$response" =~ ^[Yy]$ ]]; then
    echo "Exiting script."
    exit 1
fi

if [[ -z "$hotspotnic_content" ]]; then

    # Get a list of WIFI NICs
    mapfile -t wifi_devices < <(nmcli -t -f TYPE,DEVICE device | grep ^wifi | cut -d ':' -f 2)

    echo
    echo "Which WIFI NIC is being used for the HOTSPOT: "
    for i in "${!wifi_devices[@]}"; do
        echo "$i: ${wifi_devices[$i]}"
    done

    # Prompt the user to select an index
    echo -n "Select an index: "
    read -r selected_index

    # Store the selected device
    selected_device="${wifi_devices[$selected_index]}"
    if [[ -z "$selected_device" ]]; then
        echo "Invalid selection. Exiting."
        exit 1
    fi
    echo "You selected: $selected_device"
fi

systemctl stop phone-home-detector

if [[ -f "$hotspotnic_file" ]]; then
    rm -f "$hotspotnic_file"
fi

results_file="$script_dir/results.xml"
if [[ -f "$results_file" ]]; then
    rm "$results_file"
fi

if [[ -n "$selected_device" ]]; then
    echo "$selected_device" > "$hotspotnic_file"
fi

systemctl start phone-home-detector
