rm -f /opt/data/tis/libs/plugins/*.jpi
rm -f /opt/data/tis/libs/plugins/*.tpi

ln -s /Users/mozhenghua/j2ee_solution/project/plugins/tis-hdfs-plugin/target/tis-hdfs-plugin.tpi /opt/data/tis/libs/plugins/tis-hdfs-plugin.tpi
ln -s /Users/mozhenghua/j2ee_solution/project/plugins/tis-hive-flat-table-builder-plugin/target/tis-hive-flat-table-builder-plugin.tpi /opt/data/tis/libs/plugins/tis-hive-flat-table-builder-plugin.tpi
ln -s /Users/mozhenghua/j2ee_solution/project/plugins/tis-k8s-plugin/target/tis-k8s-plugin.tpi /opt/data/tis/libs/plugins/tis-k8s-plugin.tpi
ln -s /Users/mozhenghua/j2ee_solution/project/plugins/tis-asyncmsg-rocketmq-plugin/target/tis-asyncmsg-rocketmq-plugin.tpi  /opt/data/tis/libs/plugins/tis-asyncmsg-rocketmq-plugin.tpi
ln -s /Users/mozhenghua/j2ee_solution/project/plugins/tis-ds-mysql-plugin/target/tis-ds-mysql-plugin.tpi  /opt/data/tis/libs/plugins/tis-ds-mysql-plugin.tpi
ln -s /Users/mozhenghua/j2ee_solution/project/plugins/tis-ds-tidb-plugin/target/tis-ds-tidb-plugin.tpi  /opt/data/tis/libs/plugins/tis-ds-tidb-plugin.tpi
ln -s /Users/mozhenghua/j2ee_solution/project/plugins/tis-kafka-plugin/target/tis-kafka-plugin.tpi  /opt/data/tis/libs/plugins/tis-kafka-plugin.tpi

#for f in `find /Users/mozhenghua/j2ee_solution/project/plugins  -name '*.tpi' -print`
#do
#   echo "ln $f"
#   ln -s $f /opt/data/tis/libs/plugins/tis-asyncmsg-rocketmq-plugin.tpi
#done ;

#for tis-scala-compiler-dependencies
rm -f /opt/data/tis/libs/tis-scala-compiler-dependencies/*
cd /Users/mozhenghua/j2ee_solution/project/tis-saturn3/tis-scala-compiler-dependencies
mvn dependency:copy-dependencies
mkdir -p /opt/data/tis/libs/tis-scala-compiler-dependencies
ln -s /Users/mozhenghua/j2ee_solution/project/tis-saturn3/tis-scala-compiler-dependencies/target/dependency/* /opt/data/tis/libs/tis-scala-compiler-dependencies


#/Users/mozhenghua/Desktop/j2ee_solution/project/tis-ibatis/target/dependency
rm -f /opt/data/tis/libs/tis-ibatis/*
cd /Users/mozhenghua/Desktop/j2ee_solution/project/tis-ibatis
mvn clean package -Dmaven.test.skip=true
mvn dependency:copy-dependencies
mkdir -p /opt/data/tis/libs/tis-ibatis
ln -s /Users/mozhenghua/Desktop/j2ee_solution/project/tis-ibatis/target/dependency/* /opt/data/tis/libs/tis-ibatis
ln -s /Users/mozhenghua/Desktop/j2ee_solution/project/tis-ibatis/target/*.jar /opt/data/tis/libs/tis-ibatis

