delete from tbl_cmd_map where i_cmd = (conv(00000601, 16, 10) + 0);
insert into tbl_cmd_map values((conv(00000601, 16, 10) + 0), 'sp', 2, "sp_update_sale(?, ?, $'c_user'");
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_sale` //
CREATE PROCEDURE `ski`.`sp_update_sale`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_c_user              varchar(32)
)
label_pro:BEGIN
     
    declare c_take_info varchar(64);
    declare i_gaid_tmp integer;
    
    set out_i_code = 0;
    set out_c_desc = '';
   /*
    if isnull(in_c_user) then
       set out_i_code = 4026532099;
    end if;
    
    select i_gaid into  i_gaid_tmp from tbl_game_account where c_user = in_c_user;
    
    if isnull(i_gaid_tmp)
       select conv(F0000102, 16, 10) into out_i_code;
       set out_c_desc = "ILLEGAL_ARGUMENT.";   
       select out_c_desc;
       leave label_pro;
    end if;
    
    call sp_update_order_to_Sale(out_i_code,out_c_desc,c_take_info,i_gaid_tmp,0);
    */
    if out_i_code = 0 then 

        update tbl_game_account set c_pass_cur=in_c_pass_a  where c_user = in_c_user;

    end if;


END //  
DELIMITER ; 