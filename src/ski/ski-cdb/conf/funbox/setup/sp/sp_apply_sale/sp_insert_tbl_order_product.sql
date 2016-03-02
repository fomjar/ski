DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_insert_tbl_order_product` //
CREATE PROCEDURE `ski`.`sp_insert_tbl_order_product`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in   in_c_poid              varchar(64),    -- 平台订单ID
    in   in_i_pid               integer,        -- 产品ID
    in   in_i_prod_type         integer,        -- 实例类型
    in   in_c_prod_name         varchar(64),    -- 名称
    in   in_i_prod_price        decimal(7, 2),  -- 单价
    in   in_i_state             tinyint,        -- 订单产品状态：0-未发货 1-已发货 2-已提货 3-已退货
    in   in_c_take_info         varchar(64),    -- 提取信息
    in   in_i_inst_type         integer,        -- 实例类型，如游戏ID
    in   in_i_inst_id           integer         -- 实例ID，如游戏账户ID
)
BEGIN
  
   insert into tbl_order_product(c_poid,i_pid,i_prod_type,c_prod_name,i_prod_price,i_state,c_take_info,i_inst_type,i_inst_id) 
                          VALUES (in_c_poid,in_i_pid,in_i_prod_type,in_c_prod_name,in_i_prod_price,in_i_state,in_c_take_info,in_i_inst_type,in_i_inst_id);
                          
   set out_i_code=0;
   set out_c_desc = "CODE_SUCCESS";   
END //  
DELIMITER ; 



