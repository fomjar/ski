
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_order_to_NotSend` //
CREATE PROCEDURE `ski`.`sp_update_order_to_NotSend`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_c_take_info         varchar(64),
    in    i_gaid_tmp             integer,
    in    in_i_prod_type         integer
)
BEGIN

    if isnotnull(in_c_take_info) then
         update tbl_order_product set i_state=0  where  c_take_info=in_c_take_info;
    elseif isnotnull(i_gaid_tmp) then
         update tbl_order_product set i_state=0  where  i_inst_id=i_gaid_tmp;    
    else
       select conv(F0000102, 16, 10) into out_i_code;
       set out_c_desc = "ILLEGAL_ARGUMENT.";   
       select out_c_desc;
       leave label_pro;
    end if;
    
    set out_i_code = 0;
    set out_c_desc="success";
    
END //  
DELIMITER ;


DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_order_to_Send` //
CREATE PROCEDURE `ski`.`sp_update_order_to_Send`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_c_take_info         varchar(64),
    in    i_gaid_tmp             integer,
    in    in_i_prod_type         integer
)
label_pro:BEGIN

    declare i_state_after integer;
    
    if isnotnull(in_c_take_info) then
         update tbl_order_product set i_state=1  where  c_take_info=in_c_take_info;
    elseif isnotnull(i_gaid_tmp) then
         update tbl_order_product set i_state=1  where  i_inst_id=i_gaid_tmp;    
    else
       select conv(F0000102, 16, 10) into out_i_code;
       set out_c_desc = "ILLEGAL_ARGUMENT.";   
       select out_c_desc;
       leave label_pro;
    end if;

    /*游戏A账户*/
    if in_i_prod_type = 0 then
        call sp_update_to_ANotRent(out_i_code,out_c_desc,i_state_after,i_gaid_tmp);
    
    /*游戏B账户*/ 
    elseif in_i_prod_type = 1 then
        call sp_update_to_ANotRentBAlreadyRent(out_i_code,out_c_desc,i_gaid_tmp);
    end if;

    set out_i_code = 0;
    set out_c_desc="success";
    
END //  
DELIMITER ; 


DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_order_to_Sale` //
CREATE PROCEDURE `ski`.`sp_update_order_to_Sale`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_c_take_info         varchar(64),
    in    i_gaid_tmp             integer,
    in    in_i_prod_type         integer
)
label_pro:BEGIN

    if isnotnull(in_c_take_info) then
         update tbl_order_product set i_state=2  where  c_take_info=in_c_take_info;
    elseif isnotnull(i_gaid_tmp) then
         update tbl_order_product set i_state=2  where  i_inst_id=i_gaid_tmp;    
    else
       select conv(F0000102, 16, 10) into out_i_code;
       set out_c_desc = "ILLEGAL_ARGUMENT.";   
       select out_c_desc;
       leave label_pro;
    end if;

    
    /*游戏A账户*/
    if in_i_prod_type = 0 then 
        call sp_update_to_AAlreadyRentBForRent(out_i_code,out_c_desc,i_gaid_tmp);
    /*游戏B账户*/
    elseif in_i_prod_type = 1 then
        call sp_update_to_ANotRentBAlreadyRent(out_i_code,out_c_desc,i_gaid_tmp);
    end if;
    set out_i_code = 0;
    set out_c_desc="success";
    
END //  
DELIMITER ; 



DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_order_to_Return` //
CREATE PROCEDURE `ski`.`sp_update_order_to_Return`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_c_take_info        varchar(64)
)
BEGIN

    update tbl_order_product set i_state=3  where c_take_info=in_c_take_info;
    set out_i_code = 0;
    set out_c_desc="success";
    
END //  
DELIMITER ; 



