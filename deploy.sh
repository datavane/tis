# maven build default command
mvn clean deploy -Dmaven.test.skip=true -Dautoconfig.skip -pl tis-plugin,maven-tpi-plugin,tis-sql-parser,tis-web-start,tis-logback-flume-parent -am  -Ptis-repo


