delete from tbl_instruction where i_inst = (conv(00002201, 16, 10) + 0);
insert into tbl_instruction values((conv(00002201, 16, 10) + 0), 'sp', 2, "sp_lock_account_return(?,?,$i_inst_id,'$c_caid')");

drop procedure if exists sp_lock_account_return;
DELIMITER //
create procedure sp_lock_account_return (
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_i_inst_id           integer,
    in    in_c_caid              varchar(64)
)
comment '新的游戏账户订单'
label_pro:BEGIN
    declare i_case_status   integer;
    declare i_state_after   integer DEFAULT 0;
    declare out_c_desc_temp varchar(32);
    declare i_gaid_temp     integer  DEFAULT 0;
    declare i_change_fsm    integer DEFAULT 0;
    /*使用事务，如果中间出错恢复所有操作*/
    START TRANSACTION; 
    /*初始化出参*/
    set out_c_desc = '';
    set out_i_code = 0;
    /*校验CAID和in_i_inst_id*/
    call sp_get_gaid_by_caid(out_i_code,out_c_desc,i_gaid_temp,in_c_caid);
    
    select in_i_inst_id,in_c_caid;
    if i_gaid_temp <> in_i_inst_id then
       select conv(F0000102, 16, 10) into out_i_code;
       set out_c_desc = "ILLEGAL_ARGUMENT.";   
       select out_c_desc;
       leave label_pro;
    end if;
    
    select i_rent 
      into i_case_status
      from tbl_game_account_rent 
     where i_gaid = in_i_inst_id;
      
    select i_case_status,i_gaid_temp;
    /*------------------------------------------------------------- 
    打印状态至前端，用于后续逻辑跟踪
    select concat(out_c_desc,"From i_rent ") into out_c_desc;
    select convert(i_case_status USING ascii) into out_c_desc_temp;
    select concat(out_c_desc,out_c_desc_temp) into out_c_desc;
    select out_c_desc;
    -------------------------------------------------------------*/
    
     case i_case_status  

        /*A已租B待租*/
        when 10 then 
            call sp_update_to_ANotRentBNotRent(out_i_code,out_c_desc,i_state_after,i_gaid_temp); 
            select out_c_desc;
            set i_change_fsm = 1;
            
        /*A已租B已租*/
        when 11 then   
            call sp_update_to_ANotRentBAlreadyRent(out_i_code,out_c_desc,i_state_after,i_gaid_temp); 
            select out_c_desc;
            set i_change_fsm = 1;
            
        /*异常待检查*/
        when 6 then   
            set out_i_code = 4026532098;/*ERROR_DB_OPERATE_FAILED = 0xF0000102; // 数据库账户异常状态*/
          
        /*异常账号*/
        when 7 then   
            set out_i_code = 4026532098;/*ERROR_DB_OPERATE_FAILED = 0xF0000102; // 数据库账户异常状态*/
           
        else   
            set out_i_code = 0;
          
    end case;  
    
    /*打印当前的out_c_desc至屏幕*/
    select out_c_desc;
    select i_change_fsm;
    select out_i_code;
  
    
    if out_i_code = 0 then
        if i_change_fsm = 1 then 
            call sp_update_tbl_journal_game_account(i_gaid_temp,in_c_caid,NOW(),i_case_status,i_state_after,1);
        end if;
        commit;
    else
        rollback;
    end if;

END;
//
DELIMITER ;