mvn deploy:deploy-file \
 -DgroupId=com.qlangtech.tis.incr \
 -DartifactId=tis-incr-totalpay \
 -Dversion=2.0.0-SNAPSHOT \
 -Dpackaging=jar \
 -Dfile=/opt/data/streamscript/search4totalpay/20190820171040/search4totalpay-incr.jar \
 -Dsources=/opt/data/streamscript/search4totalpay/20190820171040/search4totalpay-incr-sources.jar \
 -DrepositoryId=snapshots \
 -Durl=http://nexus.2dfire-dev.com/repository/snapshots/