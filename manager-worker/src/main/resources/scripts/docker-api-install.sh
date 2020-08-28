#!/bin/sh
set -e

setup_docker_api() {
  DIRECTORY_DOCKER_SERVICE_API=/etc/systemd/system/docker.service.d
  FILE_DOCKER_SERVICE_API=/etc/systemd/system/docker.service.d/startup_options.conf
  if [ ! -d "$DIRECTORY_DOCKER_SERVICE_API" ]; then
    mkdir $DIRECTORY_DOCKER_SERVICE_API
  fi
  echo "" >"$FILE_DOCKER_SERVICE_API"
  sudo chmod 777 "$FILE_DOCKER_SERVICE_API"
  printf "# /etc/systemd/system/docker.service.d/override.conf\n[Service]\nExecStart=\nExecStart=/usr/bin/dockerd -H fd:// -H tcp://0.0.0.0:2376" >"$FILE_DOCKER_SERVICE_API"
  sudo systemctl daemon-reload
  sudo systemctl restart docker.service
  echo "Docker API configured"
}

setup_docker_api