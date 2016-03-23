DELIMITER // 
/*存储过程说明：当in_c_poid一样的时候in_unique_id必须是唯一的，否则会生成相同的随机取货号*/
DROP PROCEDURE IF EXISTS `ski`.`sp_generate_takInfo` //
CREATE PROCEDURE `ski`.`sp_generate_takInfo`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_c_poid              varchar(64),
    in    in_i_gaid              integer,
    out   out_c_take_info        varchar(64)
    )    
BEGIN   
    declare c_take_str  varchar(128);
    select CONCAT(in_c_poid,in_i_gaid) INTO c_take_str; 
    select md5(c_take_str) into out_c_take_info;
    select in_c_poid,in_i_gaid,c_take_str,out_c_take_info;
    set out_i_code= 0;
    set out_c_desc = "CODE_SUCCESS";       
END //  
DELIMITER ; 