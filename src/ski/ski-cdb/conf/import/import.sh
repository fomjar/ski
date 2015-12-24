#!/bin/sh

function check() {
    action=$1
    if [ "0" = "$?" ]; then
        echo "[success] $action"
    else
        echo "[fail   ] $action"
    fi
}

function readValidLines() {
    dat=$1
    lines=$(cat $dat|grep -v '^#'|grep -v '^\s*$')
    echo "$lines"
}

function wordat() {
    index=$1
    index_len=${#index}
    line=$@
    line=${line:$((++index_len))}
    echo "$line"|awk -F '|' '{print $'$index'}'|sed 's/^\s*//g'|sed 's/\s*$//g'
}

function executeSql() {
    sql=$1
    mysql -uroot -pski123 -D ski<<end
    $sql
end
}

cd dat

lines=$(readValidLines account.dat)
echo "$lines"|while read line; do
    executeSql "insert into tbl_account_basic  values($(wordat 1 $line), \"$(wordat 2 $line)\", \"$(wordat 3 $line)\", null)" 1>/dev/null 2>&1
    check "import account basic $(wordat 2 $line)"
    executeSql "insert into tbl_account_detail values($(wordat 1 $line), now(), \"$(wordat 4 $line)\")" 1>/dev/null 2>&1
    check "import account detail $(wordat 2 $line)"
done

lines=$(readValidLines game.dat)
echo "$lines"|while read line; do
    executeSql "insert into tbl_game values($(wordat 1 $line), \"$(wordat 2 $line)\", \"$(wordat 3 $line)\", \"$(wordat 4 $line)\", \"$(wordat 5 $line)\", null, null, \"$(wordat 6 $line)\", \"$(wordat 7 $line)\", \"$(wordat 8 $line)\", \"$(wordat 9 $line)\", \"$(wordat 10 $line)\")" 1>/dev/null 2>&1
    check "import game $(wordat 7 $line)"
done

lines=$(readValidLines product.dat)
echo "$lines"|while read line; do
    executeSql "insert into tbl_product values(\"$(wordat 1 $line)\", $(wordat 2 $line), $(wordat 3 $line))" 1>/dev/null 2>&1
    check "import product $(wordat 1 $line)"
done

lines=$(readValidLines account_game.dat)
echo "$lines"|while read line; do
    executeSql "insert into tbl_account_game values($(wordat 1 $line), $(wordat 2 $line))" 1>/dev/null 2>&1
    check "import map of account and game $(wordat 1 $line) : $(wordat 2 $line)"
done
