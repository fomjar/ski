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
    lines=$(cat $dat|grep -v '^#'|grep -v '^$'|sed 's/#\+/#/g')
    echo "$lines"
}

function wordat() {
    line=$1
    index=$2
    echo $line|awk -F# '{print $'$index'}'
}

function executeSql() {
    sql=$1
    mysql -uroot -ppanopasswd@520 -D wtcrm<<end
    $sql
end
}

lines=$(readValidLines account.dat)
echo "$lines"|while read line; do
    executeSql "insert into tbl_account_basic  values($(echo $line|awk -F# '{print $1}'), '$(echo $line|awk -F# '{print $2}')', '$(echo $line|awk -F# '{print $3}')', null)" 1>/dev/null 2>&1
    check "import account basic $(echo $line|awk -F# '{print $2}')"
    executeSql "insert into tbl_account_detail values($(echo $line|awk -F# '{print $1}'), now(), '$(echo $line|awk -F# '{print $4}')')" 1>/dev/null 2>&1
    check "import account detail $(echo $line|awk -F# '{print $2}')"
done

lines=$(readValidLines game.dat)
echo "$lines"|while read line; do
    executeSql "insert into tbl_game values($(echo $line|awk -F# '{print $1}'), '$(echo $line|awk -F# '{print $2}')', '$(echo $line|awk -F# '{print $3}')', '$(echo $line|awk -F# '{print $4}')', '$(echo $line|awk -F# '{print $5}')', null, null, '$(echo $line|awk -F# '{print $6}')', '$(echo $line|awk -F# '{print $7}')', '$(echo $line|awk -F# '{print $8}')', '$(echo $line|awk -F# '{print $9}')', '$(echo $line|awk -F# '{print $10}')')" 1>/dev/null 2>&1
    check "import game $(echo $line|awk -F# '{print $7}')"
done

lines=$(readValidLines product.dat)
echo "$lines"|while read line; do
    executeSql "insert into tbl_product values('$(echo $line|awk -F# '{print $1}')', $(echo $line|awk -F# '{print $2}'), $(echo $line|awk -F# '{print $3}'))" 1>/dev/null 2>&1
    check "import product $(echo $line|awk -F# '{print $1}')"
done

lines=$(readValidLines account_game.dat)
echo "$lines"|while read line; do
    executeSql "insert into tbl_account_game values($(echo $line|awk -F# '{print $1}'), $(echo $line|awk -F# '{print $2}'))" 1>/dev/null 2>&1
    check "import map of account and game $(echo $line|awk -F# '{print $1}') : $(echo $line|awk -F# '{print $2}')"
done
