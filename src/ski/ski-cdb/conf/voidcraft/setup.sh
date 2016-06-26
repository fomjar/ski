#!/bin/sh

[ "table" = "$1" ] && sqls='create_table.sql'
[ "" = "$1" ] && sqls=$(find sp -type f -name '*.sql'|sort)

for sql in $sqls; do
	echo "importing $sql"
    mysql -uski -pski -Dski<$sql
done

