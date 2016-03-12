delete from tbl_instruction where i_inst = (conv(00002002, 16, 10) + 0);
insert into tbl_instruction values((conv(00002002, 16, 10) + 0), 'sp', 3, "sp_query_return(?, ?, ?, '$c_caid')");

DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_query_return` //   
CREATE PROCEDURE `ski`.`sp_query_return`(
    out out_c_code               varchar(32),
    out out_c_desc               blob,
    inout out_b_infor            MEDIUMBLOB,
    in    in_c_caid              varchar(64)
)  
BEGIN  
   declare c_poid_tmp  varchar(64);
 
   if isnull(in_c_caid) then
       set out_c_code="F0001001";
       set out_c_desc = "ERROR PARAMETER.";   
   else
       select c_poid 
         into c_poid_tmp 
         from tbl_order 
        where c_caid = in_c_caid;
       call sp_get_return_list_by_poid(out_c_code,out_c_desc,out_b_infor,c_poid_tmp);
       select convert(out_b_infor USING utf8) into out_b_infor;
       set out_c_code="0";
       set out_c_desc = "CODE_SUCCESS";   
   end if;
END //  
DELIMITER ; 