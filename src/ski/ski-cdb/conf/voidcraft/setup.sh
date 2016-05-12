#!/bin/sh

function check() {
    action=$1
    if [ "0" = "$?" ]; then
        echo "[success] $action"
    else
        echo "[fail   ] $action"
    fi
}
mysql=../../../../../service/mysql/bin/mysql

[ "" = "$1" ]   && sqls=$(find .  -type f -name '*.sql'|sort)
[ "sp" = "$1" ] && sqls=$(find sp -type f -name '*.sql'|sort)

for sql in $sqls; do
    $mysql -uski -pski -Dski<$sql
    check "setup $sql"
done

