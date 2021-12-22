source_path="/opt/data/tis/cfg_repo"
rsync --exclude ".git"  -vr $source_path root@192.168.28.200:/opt/data/tis
