
for f in `find /Users/mozhenghua/j2ee_solution/project/plugins  -name '*.tpi' -print`
do
   echo " ln -s $f "
   ln -s $f /opt/data/tis/libs/plugins/${f##*/}
done ;