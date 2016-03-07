#!/usr/bin/env bash
export SPARK_HOME=/opt/spark-1.5.2-cdh5.5.0
export SPARK_YARN_QUEUE=spark
export HIVE_SERVER2_THRIFT_BIND_HOST=adm1ss.prod.mediav.com
cd /tmp
nohup $SPARK_HOME/sbin/start-thriftserver.sh --master yarn-client --conf spark.dynamicAllocation.enabled=false --conf spark.storage.memoryFraction=0.2 --conf spark.task.cpus=1 --conf spark.driver.extraJavaOptions="-Xms10G -Xmx10G -XX:+UseG1GC -XX:MaxMetaspaceSize=256m -XX:+PrintGC -XX:+PrintGCDetails" --conf spark.executor.extraJavaOptions="-XX:+UseG1GC" --conf spark.sql.shuffle.partitions=1000 --num-executors 500 --driver-memory 10G --executor-cores 1 --executor-memory 5G --hiveconf hive.server2.thrift.port=10002 > /data/spark/logs/spark-hadoop-thriftserver.log &
