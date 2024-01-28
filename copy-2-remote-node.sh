rsync --exclude-from= .gitignore --exclude=".git" --exclude="*.jar" --exclude="*.tpi" --exclude="*.tar.gz" --exclude="*.class" --delete -vr ../tis-solr/* root@192.168.28.201:/opt/data/tiscode/tis/
