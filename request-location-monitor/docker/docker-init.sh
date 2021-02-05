#!/bin/sh
#Script to launch processes

exec ./request-location-monitor -port $1 -interval $2
