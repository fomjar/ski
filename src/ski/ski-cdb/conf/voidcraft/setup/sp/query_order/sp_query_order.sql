delete from tbl_instruction where i_inst = (conv(00002002, 16, 10) + 0);
insert into tbl_instruction values((conv(00002002, 16, 10) + 0), 'sp', 3, "sp_query_order(?, ?, ?, '$user', '$type')");

DELIMITER //
drop procedure if exists `ski`.`sp_query_order` // 
create procedure `ski`.`sp_query_order`(
    out code    integer,
    out desc    mediumblob,
    in  user    varchar(64), -- null：所有用户；其他：指定用户caid
    in  type    varchar(16)  -- all/null：所有订单；nodeliver：未发货；deliver：已发货的订单
)
begin
    if user is null then
        call sp_query_order_all(code, desc, type);
    else
        call sp_query_order_by_user(code, desc, user, type);
    end if;

    set desc = convert(desc using utf8);
end //
DELIMITER ;
