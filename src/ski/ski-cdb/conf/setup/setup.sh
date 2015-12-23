#!/bin/sh

function check() {
    action=$1
    if [ "0" = "$?" ]; then
        echo "[success] $action"
    else
        echo "[fail   ] $action"
    fi
}

[ "" = "$1" ]   && sqls=$(find .  -type f -name '*.sql'|sort)
[ "sp" = "$1" ] && sqls=$(find sp -type f -name '*.sql'|sort)

for sql in $sqls; do
    mysql -uroot -ppanopasswd@520 -D wtcrm<$sql 1>/dev/null 2>&1
    check "setup $sql"
done
