#!/bin/bash

set -e

if [ -z "$@" ]; then
  exec /usr/bin/supervisord -c /etc/supervisord.conf --nodaemon
else
  exec PATH=/usr/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin $@
fi