#!/bin/sh

[ "" = "$1" ]   && sqls=$(find .  -type f -name '*.sql'|sort)
[ "sp" = "$1" ] && sqls=$(find sp -type f -name '*.sql'|sort)

for sql in $sqls; do
    mysql -uroot -ppanopasswd@520 -D wtcrm<$sql 1>/dev/null 2>&1
    if [ "0" = "$?" ]; then
        echo "[success] import $sql"
    else
        echo "[fail   ] import $sql"
    fi
done
