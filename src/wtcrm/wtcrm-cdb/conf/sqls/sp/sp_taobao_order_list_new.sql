delete from tbl_cmd_map where c_cmd = 'taobao-order-list-new';
insert into tbl_cmd_map values('taobao-order-list-new', 'sp', 2, "sp_taobao_order_list_new(?, ?, '$toid', '$fpid', '$tuid', '$tp-name', '$tp-attr', $tp-price, $tp-count, '$buyer-name', '$buyer-tel', '$buyer-addr', '$buyer-zip')");

drop procedure if exists sp_taobao_order_list_new;
DELIMITER //
create procedure sp_taobao_order_list_new (
    out i_code          integer,
    out c_desc          varchar(32),
    in  in_c_toid       varchar(20),
    in  in_c_fpid       varchar(16),
    in  in_c_tuid       varchar(32),
    in  in_c_tp_name    varchar(64),
    in  in_c_tp_attr    varchar(64),
    in  in_i_tp_price   decimal(7, 2),
    in  in_i_tp_count   integer,
    in  in_c_buyer_name varchar(10),
    in  in_c_buyer_tel  varchar(20),
    in  in_c_buyer_addr varchar(100),
    in  in_c_buyer_zip  varchar(10)
)
comment '新的淘宝订单'
BEGIN
    declare i_temp integer;

    select count(1)
      into i_temp
      from tbl_order_taobao
     where c_toid = in_c_toid;
    
    if i_temp = 0 then
        insert into tbl_order_taobao values (
            now(),
            in_c_toid,
            in_c_fpid,
            in_c_tuid,
            in_c_tp_name,
            in_c_tp_attr,
            in_i_tp_price,
            in_i_tp_count,
            in_c_buyer_name,
            in_c_buyer_tel,
            in_c_buyer_addr,
            in_c_buyer_zip,
            0 -- not delivered
        );
    end if;
    
    set i_code = 0;
    set c_desc = null;
END;
//
DELIMITER ;