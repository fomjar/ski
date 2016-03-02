DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_get_gaid_by_caid` //   
CREATE PROCEDURE `ski`.`sp_get_gaid_by_caid`(
    out   out_c_code             varchar(32),
    inout out_c_desc             blob,
    out   out_i_gaid             integer,
    in    in_i_caid              varchar(64)
)  
BEGIN  
   declare i_poid_tmp integer default 0;
   declare i_pid_tmp  integer default 0;
   declare i_gid_tmp  integer default 0;
   
   select i_poid into i_poid_tmp from tbl_order where c_caid = in_i_caid;
   select i_poid_tmp;
   select i_inst_id  into out_i_gaid  from tbl_order_product where i_poid = i_poid_tmp;
   select out_i_gaid;
   set out_c_code="0";
   set @out_c_desc = "CODE_SUCCESS";   
    
END //  
DELIMITER ; 