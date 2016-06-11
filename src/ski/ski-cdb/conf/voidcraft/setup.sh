#!/bin/sh

[ "" = "$1" ]   && sqls=$(find .  -type f -name '*.sql'|sort)
[ "sp" = "$1" ] && sqls=$(find sp -type f -name '*.sql'|sort)

for sql in $sqls; do
	echo "importing $sql"
    mysql -uski -pski -Dski<$sql
done

