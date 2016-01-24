delete from tbl_cmd_map where i_cmd = (conv(00000301, 16, 10) + 0);
insert into tbl_cmd_map values((conv(00000301, 16, 10) + 0), 'sp', 3, "sp_apply_return(?, ?, ?, '$c_caid')");

DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_apply_return` //   
CREATE PROCEDURE `ski`.`sp_apply_return`(
    out out_c_code               varchar(32),
    out out_c_desc               blob,
    inout out_b_infor            MEDIUMBLOB,
    in    in_c_caid              varchar(64),
    in    in_i_channel           integer,
    in    in_i_coid              integer,
    in    t_place                datetime,
    in    c_prod_name             varchar(64), 
    in    i_prod_type         integer,        -- 产品类型
    in    i_prod_price        decimal(7, 2),  -- 单价
)  
BEGIN  
   declare i_poid_tmp integer default 0;
   set out_b_infor = '';
   select in_c_caid;
   if isnull(in_c_caid) then
       set out_c_code="F0001001";
       set out_c_desc = "ERROR PARAMETER.";   
   else
       select i_poid into i_poid_tmp from tbl_order where c_caid = in_c_caid;
       select i_poid_tmp;
       call sp_get_return_list_by_poid(out_c_code,out_c_desc,out_b_infor,i_poid_tmp);
       select convert(out_b_infor USING utf8) into out_b_infor;
       set out_c_code="0";
       set out_c_desc = "CODE_SUCCESS";   
   end if;
END //  
DELIMITER ; 