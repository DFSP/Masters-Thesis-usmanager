#!/bin/sh

set -e

uninstall_docker_command() {
	sudo apt-get purge -y docker-engine docker docker.io docker-ce  
	sudo apt-get autoremove -y --purge docker-engine docker docker.io docker-ce  
}

delete_docker_group() {
	if grep -q docker /etc/group
	then
		sudo groupdel docker
		echo 'Deleted docker group'
	else
		echo 'Docker group not found'
	fi
}

remove_docker_files() {
	echo "Deleting docker files..."
	if [ -r "/var/lib/docker" ]; 
	then
		sudo rm -rf /var/lib/docker
  	fi
  	if [ -r "/etc/docker" ]; 
	then
		sudo rm -rf /etc/docker
  	fi
  	if [ -f "/etc/apparmor.d/docker" ]; 
	then
		sudo rm /etc/apparmor.d/docker
  	fi
  	if [ -f "/var/run/docker.sock" ]; 
	then
		sudo rm -rf /var/run/docker.sock
  	fi
}

uninstall_docker_command

delete_docker_group

remove_docker_files