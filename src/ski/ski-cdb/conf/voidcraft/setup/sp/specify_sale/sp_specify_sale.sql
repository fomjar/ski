delete from tbl_cmd_map where i_cmd = (conv(00000300, 16, 10) + 0);
insert into tbl_cmd_map values((conv(00000300, 16, 10) + 0), 'sp', 2, "sp_apply_sale(?, ?, $'c_user', $'in_c_pass_cur', $'in_c_pass_a', $'in_c_pass_b')");
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_specify_sale` //
CREATE PROCEDURE `ski`.`sp_specify_sale`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    inout inout_c_take_info      varchar(64),
    out   out_c_user             varchar(32),    -- 用户名
    out   out_c_pass_cur         varchar(32),    -- 当前密码
    in    in_i_coid              integer,
    in    in_c_caid              varchar(64),
    in    in_c_name_cns          varchar(64),
    in    in_i_prod_type         integer
)
BEGIN
        declare c_take_info_tmp varchar(64);
        declare i_gaid_tmp integer;
        declare i_error integer;
        START TRANSACTION;  
        select inout_c_take_info;
        /*如果提货码是空的，则是生成提货码流程，否则是提货流程*/
        if isnull (inout_c_take_info) then
        
            select tbl_order_product.c_take_info
                into c_take_info_tmp
                from tbl_order_product inner join tbl_order 
                where  (tbl_order_product.c_poid = tbl_order.c_poid)
                and tbl_order_product.i_state = 0 
                and tbl_order_product.c_prod_name = in_c_name_cns
                and tbl_order_product.i_prod_type = in_i_prod_type
                order by  tbl_order.t_place asc limit 1;
                
             if isnull(c_take_info_tmp) then
                set i_error = 1;
             else
               call sp_update_order_to_Send(out_i_code,out_c_desc,inout_c_take_info,i_gaid_tmp,in_i_prod_type); 
             end if;
             
             set i_error = i_error + out_i_code; 
             /*发货后更改订单状态*/
            select i_error;

         else
         
             select tbl_game_account.c_user,tbl_game_account.c_pass_cur,tbl_game_account.i_gaid
                into out_c_user,out_c_pass_cur,i_gaid_tmp
                from tbl_game_account inner join tbl_order_product 
                where  tbl_order_product.i_inst_id = tbl_game_account.i_gaid
                and tbl_order_product.c_take_info = inout_c_take_info
                and tbl_order_product.i_state = 1;
                /*打印出账号的和密码*/
                select out_c_user , out_c_pass_cur;

                if isnull(out_c_user) then
                    set i_error = i_error + 1;
                else
                     call sp_update_order_to_Sale(out_i_code,out_c_desc,inout_c_take_info,i_gaid_tmp,in_i_prod_type); 
                end if;

         end if;
         
         if i_error<>0 then  
            select "RollBack";
            ROLLBACK;  
        else  
            select "COMMINT";
            set inout_c_take_info = c_take_info_tmp;
            COMMIT;  
        end if;
         
         
END //  
DELIMITER ; 


