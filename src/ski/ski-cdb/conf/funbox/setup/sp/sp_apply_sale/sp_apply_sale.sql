delete from tbl_cmd_map where i_cmd = (conv(00000300, 16, 10) + 0);
insert into tbl_cmd_map values((conv(00000300, 16, 10) + 0), 'sp', 2, "sp_apply_sale(?, ?, $'c_user', $'in_c_pass_cur', $'in_c_pass_a', $'in_c_pass_b')");
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_apply_sale` //
CREATE PROCEDURE `ski`.`sp_apply_sale`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_i_coid              integer,
    in    in_i_channel           integer,
    in    in_c_caid              varchar(64),
    in    in_t_place             datetime,
    in    i_prod_price           decimal(7, 2),
    in    i_prod_num             integer,
    in    c_name_cns             varchar(64),
    in    i_prod_type            integer,
    
)
BEGIN
   
   
END //  
DELIMITER ; 


