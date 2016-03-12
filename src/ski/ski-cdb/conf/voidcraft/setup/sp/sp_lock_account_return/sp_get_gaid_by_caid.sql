DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_get_gaid_by_caid` //   
CREATE PROCEDURE `ski`.`sp_get_gaid_by_caid`(
    out   out_c_code             varchar(32),
    inout out_c_desc             blob,
    out   out_i_gaid             integer,
    in    in_i_caid              varchar(64)
)  
BEGIN  
   declare c_poid_tmp varchar(64);
   declare i_pid_tmp  integer default 0;
   declare i_gid_tmp  integer default 0;
   
   select c_poid into c_poid_tmp from tbl_order where c_caid = in_i_caid;
   select c_poid_tmp;
   select i_inst_id  into out_i_gaid  from tbl_order_product where c_poid = c_poid_tmp;
   select out_i_gaid;
   set out_c_code="0";
   set @out_c_desc = "CODE_SUCCESS";   
    
END //  
DELIMITER ; 