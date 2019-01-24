
cd /usr/share
echo "localpath:'`pwd`'"

DATA_DIR=tis
#TAR_FILE=tis.tar.gz

#function untar() {
   
#    if [ ! -d $DATA_DIR  ]; then
#       mkdir -p $DATA_DIR
#       tar xvf $TAR_FILE -C $DATA_DIR
#    fi
    
#    if [ ! -d $DATA_DIR  ]; then
#       echo "Fialed to untar '$TAR_FILE'"
#       exit 1
#    fi
#}

#untar

cd /usr/share/tis-console/$DATA_DIR

java -jar tis.jar