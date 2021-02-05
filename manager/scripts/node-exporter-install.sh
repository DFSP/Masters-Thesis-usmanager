#!/bin/sh

set -e

command_exists() {
  command -v "$@" >/dev/null 2>&1
}

if ! command_exists node_exporter; then
  sudo useradd --no-create-home --shell /bin/false node_exporter
  cd ~
  curl -LO https://github.com/prometheus/node_exporter/releases/download/v0.16.0/node_exporter-0.16.0.linux-amd64.tar.gz
  sha256sum node_exporter-0.16.0.linux-amd64.tar.gz
  tar xvf node_exporter-0.16.0.linux-amd64.tar.gz
  sudo cp node_exporter-0.16.0.linux-amd64/node_exporter /usr/local/bin
  sudo chown node_exporter:node_exporter /usr/local/bin/node_exporter
  rm -rf node_exporter-0.16.0.linux-amd64.tar.gz node_exporter-0.16.0.linux-amd64
  echo "" >/etc/systemd/system/node_exporter.service
  printf "[Unit]\nDescription=Node Exporter\nWants=network-online.target\nAfter=network-online.target\n\n[Service]\nUser=node_exporter\nGroup=node_exporter\nType=simple\nExecStart=/usr/local/bin/node_exporter\n\n[Install]\nWantedBy=multi-user.target" >/etc/systemd/system/node_exporter.service
  sudo systemctl daemon-reload
  sudo systemctl start node_exporter

  echo "Node_exporter installed."
else
  echo "Node_exporter is already installed."
fi
