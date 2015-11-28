delete from tbl_cmd_map where c_cmd = 'user.register';
insert into tbl_cmd_map values('user.register', 'sp', 2, 'sp_user_register(?, ?, $account, $password)');

drop procedure if exists sp_user_register;
DELIMITER //
create procedure sp_user_register(
    out out_err     varchar(255),
    out out_uid     varchar(32),
    in  in_account  varchar(32),
    in  in_password varchar(32)
)
comment '注册用户'
BEGIN
    declare i_ERROR_USER_EXIST integer default conv(01000001, 16, 10);
    declare i_temp integer;
    
    select count(1)
      into i_temp
      from tbl_user
     where s_account  = in_s_account;
    
    if i_temp > 0 then
        set out_i_mc = i_ERROR_USER_EXIST;
        leave label_begin;
    end if;
    
    insert into tbl_user values(null, in_s_account, in_s_password, null, null);
    select i_uid
      into out_i_uid
      from tbl_user
     where s_account = in_s_account;
    
    set out_i_mc = 0;
END;
//
DELIMITER ;