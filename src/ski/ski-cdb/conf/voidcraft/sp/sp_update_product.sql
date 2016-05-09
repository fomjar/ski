delete from tbl_instruction where i_inst = (conv(00002405, 16, 10) + 0);
insert into tbl_instruction values((conv(00002405, 16, 10) + 0), 'sp', 2, "sp_update_product(?, ?, $pid, $prod_type, $prod_inst)");

-- 更新产品
delimiter //
drop procedure if exists sp_update_product // 
create procedure sp_update_product (
    out i_code      integer,
    out c_desc      mediumblob,
    in  pid         integer,    -- 产品ID
    in  prod_type   integer,    -- 产品类型：0-PS4游戏A租赁 1-PS4游戏B租赁 2-PS4游戏出售 3-PS4主机租赁 4-PS4主机出售
    in  prod_inst   integer     -- 产品实例，比如游戏ID
)
begin
    declare i_pid   integer default -1;
    declare i_count integer default -1;

    if pid is null then
        select max(i_pid)
          into i_pid
          from tbl_product;

        set i_pid = i_pid + 1;

        insert into tbl_product (
            i_pid,
            i_prod_type,
            i_prod_inst
        )
        values (
            pid,
            prod_type,
            prod_inst
        );
    else
        select count(1)
          into i_count
          from tbl_product
         where i_pid = pid;

        if i_count <= 0 then
            insert into tbl_product (
                i_pid,
                i_prod_type,
                i_prod_inst
            )
            values (
                pid,
                prod_type,
                prod_inst
            );
        else
            if prod_type is not null then
                update tbl_product
                   set i_prod_type = prod_type
                 where i_pid = pid;
            end if;
            if prod_inst is not null then
                update tbl_product
                   set i_prod_inst = prod_inst
                 where i_pid = pid;
            end if;
        end if;
    end if;
    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
