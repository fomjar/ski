#!/bin/sh

name=mysql-ski-$(date "+%Y%m%d%H%M%S").dump
echo "dumping..."
mysqldump -uski -pski ski>$name 2>/dev/null
echo "compressing..."
tar -zcvf ${name}.tar.gz $name>/dev/null
rm $name
echo "done"
