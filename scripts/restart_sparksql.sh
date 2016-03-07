#!/usr/bin/env bash

USER=`id -un`

# check running user
if [ "$USER" != "root" ]; then
    echo "must run as root !"
    exit 1
fi

# kill old sparksql server
SPARKSQLPID=`sudo -u hadoop /opt/jdk8/bin/jps |grep SparkSubmit|awk '{print $1}'`
if [ "$SPARKSQLPID" != "" ]; then
    echo "SparkSQL is already running @$SPARKSQLPID , kill it first !"
    kill -9 $SPARKSQLPID
    sleep 3
fi

# start sparksql server
cd /tmp
sudo -u hadoop sh /opt/spark/start-thriftserver.sh
sleep 10
TRACKINGURL=`grep tracking /tmp/spark-hadoop.log|tail -n1|awk '{print $3}'`
echo "SparkSQL is running. tracking url : "$TRACKINGURL

# kill old skyeye server
SKYEYEPID=`jps |grep NettyServer|awk '{print $1}'`
if [ "$SKYEYEPID" != "" ]; then
    echo "Skyeye is already running @$SKYEYEPID , kill it first !"
    kill -9 $SKYEYEPID
    sleep 3
fi

rm -rf /opt/skyeye-2.0/RUNNING_PID
export JAVA_HOME=/opt/jdk8/
nohup /opt/skyeye-2.0/bin/skyeye -Dhttp.port=80 -Dsparksql.webui.url=$TRACKINGURL &
echo "DONE"


