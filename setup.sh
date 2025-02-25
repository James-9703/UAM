#!/bin/bash

# Function to check and install a package
install_if_missing() {
    PACKAGE=$1
    if ! command -v $PACKAGE &> /dev/null; then
        echo "$PACKAGE is not installed. Installing..."
        sudo apt-get update
        sudo apt-get install -y $PACKAGE
    else
        echo "$PACKAGE is already installed."
    fi
}

# Check and install wmctrl
install_if_missing wmctrl

# Check and install xprintidle
install_if_missing xprintidle





# File to check
SUDOERS_FILE="/etc/sudoers"

# Lines to ensure in the sudoers file
LINE1="Defaults logfile=/tmp/sudo"
LINE2="Defaults log_subcmds"

# Function to check and append a line if missing
append_if_missing() {
    local LINE="$1"
    if ! grep -Fxq "$LINE" "$SUDOERS_FILE"; then
        echo "$LINE" | sudo tee -a "$SUDOERS_FILE" > /dev/null
        echo "Appended: $LINE"
    else
        echo "Line already exists: $LINE"
    fi
}

# Check and append lines
append_if_missing "$LINE1"
append_if_missing "$LINE2"

chmod o+r /tmp/sudo

if  which java &> /dev/null; then
	echo "java installed"
  else
	echo "java not installed, recommend jdk21"
fi

