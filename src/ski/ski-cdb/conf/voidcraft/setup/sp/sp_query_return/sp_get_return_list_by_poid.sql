DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_get_return_list_by_poid` //   
CREATE PROCEDURE `ski`.`sp_get_return_list_by_poid`(
    out   out_c_code             varchar(32),
    inout out_c_desc             blob,
    inout out_b_infor            MEDIUMBLOB,
    in    in_c_poid             varchar(64)
)  
BEGIN  

   declare i_gaid_tmp integer default 0;
   declare i_type_tmp integer default 0;
   declare c_type_tmp varchar(32);
   declare i_pid_tmp integer default 0;
   declare c_gaid_tmp varchar(32);
   declare i_gid_tmp integer default 0;
   declare c_name_cns_tmp varchar(64);
   declare c_user_tmp varchar(32);
   declare c_pass_tmp varchar(32);
   declare i integer default 0;
   declare Done INT DEFAULT 0;

   /* 声明游标,根据i_poid找到i_gaid_tmp*/
   DECLARE rs CURSOR FOR SELECT i_inst_id,i_prod_type,i_pid FROM tbl_order_product where c_poid = in_c_poid;
   /* 异常处理 */
   DECLARE CONTINUE HANDLER FOR SQLSTATE '02000' SET Done = 1;
   /* 打开游标 */
   OPEN rs;  
   /* 逐个取出当前记录i_gaid_tmp值*/
   FETCH rs INTO i_gaid_tmp,i_type_tmp,i_pid_tmp;  
   select i_gaid_tmp;
   select i_type_tmp;
   /* 遍历数据表 */
   REPEAT
      /*将游戏类型拼接到out_b_infor*/
      select conv(i_type_tmp, 10, 16) into i_type_tmp;
      select convert(i_type_tmp USING ascii) into c_type_tmp;
      call sp_add_str_to_blob(out_c_code,out_c_desc,out_b_infor,c_type_tmp,"\t");
      /*将产品PID拼接到out_b_infor*/
      select conv(i_gaid_tmp, 10, 16) into i_gaid_tmp;
      select convert(i_gaid_tmp USING ascii) into c_gaid_tmp;
      call sp_add_str_to_blob(out_c_code,out_c_desc,out_b_infor,c_gaid_tmp,"\t");
      
      /*将游戏名字拼接到out_b_infor*/
      select i_gid 
        into i_gid_tmp 
        from tbl_game_account_game 
       where i_gaid = i_gaid_tmp;
       
      select c_name_cns 
        into c_name_cns_tmp 
        from tbl_game 
       where i_gid = i_gid_tmp;
       
      select convert(c_name_cns_tmp USING utf8) into c_name_cns_tmp;
      select c_name_cns_tmp;
      call sp_add_str_to_blob(out_c_code,out_c_desc,out_b_infor,c_name_cns_tmp,"\t");
      
      /*将游戏账号拼接到out_b_infor*/
      select c_user 
      into c_user_tmp 
      from tbl_game_account 
      where i_gaid = i_gaid_tmp;
      
      select c_user_tmp;
      call sp_add_str_to_blob(out_c_code,out_c_desc,out_b_infor,c_user_tmp,"\t");
      
      /*将游戏密码a拼接到out_b_infor*/
      select c_pass_a 
        into c_pass_tmp 
        from tbl_game_account 
       where i_gaid = i_gaid_tmp;
       
      select c_pass_tmp;
      call sp_add_str_to_blob(out_c_code,out_c_desc,out_b_infor,c_pass_tmp,"\t");
      
      /*将游戏密码b拼接到out_b_infor*/
      select c_pass_b 
       into c_pass_tmp 
       from tbl_game_account
      where i_gaid = i_gaid_tmp;
      
      select c_pass_tmp;
      call sp_add_str_to_blob(out_c_code,out_c_desc,out_b_infor,c_pass_tmp,"\t");
      
      /*将游戏cur密码拼接到out_b_infor*/
      select c_pass_cur 
        into c_pass_tmp 
        from tbl_game_account 
       where i_gaid = i_gaid_tmp;
      select c_pass_tmp;
      call sp_add_str_to_blob(out_c_code,out_c_desc,out_b_infor,c_pass_tmp,"\t");
      
      /*将\r\n拼接到out_b_infor*/
      call sp_add_str_to_blob(out_c_code,out_c_desc,out_b_infor,"","\n");
      select out_b_infor;
      
   FETCH rs INTO i_gaid_tmp,i_type_tmp,i_pid_tmp;  
   UNTIL Done END REPEAT;
   /* 关闭游标 */
   CLOSE rs;

   set out_c_code="0";
   set out_c_desc = 'CODE_SUCCESS';   
 
END //  
DELIMITER ; 