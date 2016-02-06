


DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_order_to_NotSend` //
CREATE PROCEDURE `ski`.`sp_update_order_to_NotSend`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_c_take_info        varchar(64)
)
BEGIN

    update tbl_order_product set i_state=0  where c_take_info=in_c_take_info;
    set out_i_code = 0;
    set out_c_desc="success";
    
END //  
DELIMITER ;


DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_order_to_Send` //
CREATE PROCEDURE `ski`.`sp_update_order_to_Send`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_c_take_info        varchar(64)
)
BEGIN

    update tbl_order_product set i_state=1  where c_take_info=in_c_take_info;
    set out_i_code = 0;
    set out_c_desc="success";
    
END //  
DELIMITER ; 


DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_order_to_Sale` //
CREATE PROCEDURE `ski`.`sp_update_order_to_Sale`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_c_take_info        varchar(64)
)
BEGIN

    update tbl_order_product set i_state=2  where c_take_info=in_c_take_info;
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



