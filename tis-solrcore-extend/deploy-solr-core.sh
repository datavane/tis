  # 查看jar包已经有的文件
  #  jar -tvf ./solr-core-8.6.1.jar | grep CoreAdminOperation
  # 删除jar包中存在的class文件
  #  zip --delete ./solr-core-8.6.1.jar "org/apache/solr/handler/admin/CoreAdminOperation.class"
  #zip --delete ./solr-core-8.7.0.jar "org/apache/solr/cloud/RecoveryStrategy.class" RecoveryStrategy
#org.apache.maven.plugins:maven-deploy-plugin:2.8:deploy
mvn org.apache.maven.plugins:maven-deploy-plugin:2.8:deploy-file \
 -DgroupId=com.qlangtech.tis \
 -DartifactId=tis-solr-core \
 -Dversion=8.7.0-fix \
 -Dpackaging=jar \
 -DpomFile=/Users/mozhenghua/Desktop/j2ee_solution/mvn_repository/org/apache/solr/solr-core/fix/solr-core-8.7.0.pom \
 -Dsources=/Users/mozhenghua/Desktop/j2ee_solution/mvn_repository/org/apache/solr/solr-core/fix/solr-core-8.7.0-sources.jar \
 -Dfile=/Users/mozhenghua/Desktop/j2ee_solution/mvn_repository/org/apache/solr/solr-core/fix/solr-core-8.7.0.jar \
 -DrepositoryId=releases \
 -Durl=http://localhost:8080/release/