-- 修改租赁实例状态
delimiter //
drop function if exists fn_change_rent_state // 
create function fn_change_rent_state (
    in user         varchar(64),    -- 待修改实例所属用户
    in prod_type    integer,        -- 待修改的产品类型
    in prod_inst    integer,        -- 待修改的产品实例
    in state        tinyint         -- 目标状态
)
returns integer
begin
    declare i_count         integer default -1;
    declare i_state_before  tinyint default -1;
    declare i_state_after   tinyint default -1;

    select count(1)
      into i_count
      from tbl_rent
     where c_caid = user
       and i_prod_type = prod_type
       and i_prod_inst = prod_inst;

    if i_count < 1 then
        return -1;
    end if;

    select i_state
      into i_state_before
      from tbl_rent
     where c_caid = user
       and i_prod_type = prod_type
       and i_prod_inst = prod_inst;

    set i_state_after = state;

    insert into tbl_rent_history values(
        user,
        prod_type,
        prod_inst,
        i_state_before,
        i_state_after,
        now()
    );

    update tbl_rent
       set i_state = state
     where c_caid = user
       and i_prod_type = prod_type
       and i_prod_inst = prod_inst;

    return 0;
end //
delimiter ;
