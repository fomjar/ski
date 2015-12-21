delete from tbl_cmd_map where c_cmd = 'taobao-order-proc-new';
insert into tbl_cmd_map values('taobao-order-proc-new', 'sp', 3, "sp_taobao_order_proc_new");

drop procedure if exists sp_taobao_order_proc_new;
DELIMITER //
create procedure sp_taobao_order_proc_new (
    out i_code  integer,
    out c_desc  varchar(32),
    out c_toid  varchar(20)  
)
comment '处理一条淘宝订单'
BEGIN
    declare i_temp integer;
    declare c_fpid varchar(16);
    
    set i_code = 0;
    set c_desc = null;
    set c_toid = null;

    select count(1)
      into i_temp
      from tbl_order_taobao
     where i_status = 0;
     
    if i_temp <> 0 then -- 存在状态为0的订单
        select c_fpid
          into c_fpid
          from tbl_order_taobao
      order by t_time
         limit 1;
        
        
    end if;
END;
//
DELIMITER ;