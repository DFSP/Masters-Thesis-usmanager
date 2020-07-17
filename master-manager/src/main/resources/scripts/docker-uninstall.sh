#!/bin/sh

command_exists() {
	command -v "$@" > /dev/null 2>&1
}

uninstall_docker() {
	sudo apt-get purge -y docker-engine docker docker.io docker-ce docker-ce-cli
	sudo apt-get autoremove -y --purge docker-engine docker docker.io docker-ce  
	sudo rm -rf /var/lib/docker /etc/docker
	sudo rm /etc/apparmor.d/docker
	sudo groupdel docker
	sudo rm -rf /var/run/docker.sock	
}

uninstall_docker

if ! command_exists docker; then
	echo "Docker uninstalled"	
else
	echo "Failed to uninstall Docker"
fi
