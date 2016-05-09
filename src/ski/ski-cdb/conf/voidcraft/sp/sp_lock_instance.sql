delete from tbl_instruction where i_inst = (conv(00002201, 16, 10) + 0);
insert into tbl_instruction values((conv(00002201, 16, 10) + 0), 'sp', 2, "sp_lock_instance(?, ?, '$instance')");

-- 锁定产品实例
delimiter //
drop procedure if exists sp_lock_instance // 
create procedure sp_lock_instance (
    out i_code          integer,
    out c_desc          mediumblob,
    in  user            varchar(64),    -- 待锁定实例所属用户
    in  prod_type       integer,        -- 待锁定的产品类型
    in  prod_inst       integer         -- 待锁定的产品实例
)
begin
    declare i_count     integer default -1;
    declare i_state     tinyint default -1;

    select count(1)
      into i_count
      from tbl_rent
     where c_caid = user
       and i_prod_type = prod_type
       and i_prod_inst = prod_inst;

    -- 租赁表实例是否存在
    if i_count < 0 then
        set i_code = -1;
        set c_desc = "user or instance does not exist";
    else
        select r.i_state
          into i_state
          from tbl_rent
         where r.caid = user
           and r.i_prod_type = prod_type
           and r.i_prod_inst = prod_inst;

        -- 当前实例状态是否允许锁定
        if i_state <> 1 then
            set i_code = -2;
            set c_desc = "instance can not lock now";
        else
            select fn_change_rent_state(user, prod_type, prod_inst, 2);
            set i_code = 0;
            set c_desc = null;
        end if;
    end if;

end //
delimiter ;
