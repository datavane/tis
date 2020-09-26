  # 查看jar包已经有的文件
  #  jar -tvf ./solr-core-8.6.1.jar | grep CoreAdminOperation
  # 删除jar包中存在的class文件
  #  zip --delete ./solr-core-8.6.1.jar "org/apache/solr/handler/admin/CoreAdminOperation.class"

mvn deploy:deploy-file \
 -DgroupId=com.qlangtech.tis \
 -DartifactId=tis-solr-core \
 -Dversion=8.6.1-fix \
 -Dpackaging=jar \
 -DpomFile=/Users/mozhenghua/Desktop/j2ee_solution/mvn_repository/org/apache/solr/solr-core/fix/solr-core-8.6.1.pom \
 -Dsources=/Users/mozhenghua/Desktop/j2ee_solution/mvn_repository/org/apache/solr/solr-core/fix/solr-core-8.6.1-sources.jar \
 -Dfile=/Users/mozhenghua/Desktop/j2ee_solution/mvn_repository/org/apache/solr/solr-core/fix/solr-core-8.6.1.jar \
 -DrepositoryId=releases \
 -Durl=http://localhost:8080/release/