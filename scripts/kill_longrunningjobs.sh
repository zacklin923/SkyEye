#! /usr/bin/env bash

curl 'http://rm1ss.prod.mediav.com:8088/ws/v1/cluster/apps?state=RUNNING' |jsawk 'return this.apps.app'|jsawk 'if (this.queue = "root.camus") return null'|jsawk 'if (this.elapsedTime <= 3600000) return null'|jsawk  'return this.id'|jsawk -a 'return this.join("\n")' > longrunningjobs

for i in `cat longrunningjobs`
  do
    echo "killing app "$i
    yarn application -kill $i
  done

