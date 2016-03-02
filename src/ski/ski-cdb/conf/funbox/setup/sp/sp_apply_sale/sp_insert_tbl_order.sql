DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_insert_tbl_order` //
CREATE PROCEDURE `ski`.`sp_insert_tbl_order`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in    in_c_poid              varchar(64),
    in    in_i_coid              integer,
    in    in_i_channel           integer,
    in    in_c_caid              varchar(64),
    in    in_t_place             datetime
)
label_pro:BEGIN
  
   declare i_tmp  integer default 0;
   select count(in_c_poid) into i_tmp from tbl_order where c_poid =  in_c_poid;
   select i_tmp;
   if i_tmp <> 0  then
       select conv(F0000102, 16, 10) into out_i_code;
       set out_c_desc = "ILLEGAL_ARGUMENT.";   
       select out_c_desc; 
       leave label_pro;
   end if;
   
   insert into tbl_order(c_poid,i_coid,i_channel,c_caid,t_place) VALUES (in_c_poid,in_i_coid,in_i_channel,in_c_caid,in_t_place);
   set out_i_code= 0;
   set out_c_desc = "CODE_SUCCESS";   
END //  
DELIMITER ; 