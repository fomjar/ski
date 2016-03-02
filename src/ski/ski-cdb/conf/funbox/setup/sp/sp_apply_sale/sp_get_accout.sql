DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_get_accout` //
CREATE PROCEDURE `ski`.`sp_get_accout`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    out   i_gaid_tmp             integer,
    in    i_gid_tmp              integer,
    in    in_i_prod_type         integer
)
label_pro:BEGIN
   select in_i_prod_type;
  /*如果是A类账户出租*/
     if in_i_prod_type = 0 then
        select IFNULL(tbl_game_account_game.i_gaid,0)
            into i_gaid_tmp
            from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent inner join tbl_journal_game_account 
            where  (tbl_game.i_gid = i_gid_tmp) 
            and (tbl_game.i_gid = tbl_game_account_game.i_gid)
            and (tbl_game_account_game.i_gaid = tbl_game_account_rent.i_gaid)
            and (tbl_game_account_rent.i_rent = 1)
            and (tbl_game_account_game.i_gaid = tbl_journal_game_account.i_gaid) 
            order by tbl_journal_game_account.t_change  asc limit 1; -- 有多条时只显示离现在最近的一条  
            
       /*当没有01状态的账户时候寻找00状态的用户*/ 
        if ISNULL(i_gaid_tmp) then  
                select IFNULL(tbl_game_account_game.i_gaid,0)
                    into i_gaid_tmp
                    from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent  
                    where  (tbl_game.i_gid = i_gid_tmp) 
                           and (tbl_game.i_gid = tbl_game_account_game.i_gid)
                           and (tbl_game_account_game.i_gaid = tbl_game_account_rent.i_gaid)
                           and (tbl_game_account_rent.i_rent = 0)
                    order by  tbl_game_account_game.i_gaid asc limit 1;
        end if;
        
  
        
     /*如果是B类账户出租*/
     elseif in_i_prod_type = 1 then 
     
        select IFNULL(tbl_game_account_game.i_gaid,0)
            into i_gaid_tmp
            from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent inner join tbl_journal_game_account 
            where  (tbl_game.i_gid = i_gid_tmp) 
            and (tbl_game.i_gid = tbl_game_account_game.i_gid)
            and (tbl_game_account_game.i_gaid = tbl_game_account_rent.i_gaid)
            and (tbl_game_account_rent.i_rent = 10)
            and (tbl_game_account_game.i_gaid = tbl_journal_game_account.i_gaid)
            order by tbl_journal_game_account.t_change  asc limit 1; -- 有多条时只显示离现在最近的一条  
            
       /*当没有01状态的账户时候寻找00状态的用户*/ 
        if ISNULL(i_gaid_tmp) then  
                select IFNULL(tbl_game_account_game.i_gaid,0)
                    into i_gaid_tmp
                    from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent  
                    where  (tbl_game.i_gid = i_gid_tmp) 
                           and (tbl_game.i_gid = tbl_game_account_game.i_gid)
                           and (tbl_game_account_game.i_gaid = tbl_game_account_rent.i_gaid)
                           and (tbl_game_account_rent.i_rent = 0)
                    order by  tbl_game_account_game.i_gaid asc limit 1;
        end if;
        

     end if;
     select i_gaid_tmp;
     if isnull(i_gaid_tmp) then  
        set out_i_code= 1;
        set out_c_desc = "ERORR";  
        leave label_pro;
     end if;
    
     set out_i_code= 0;
     set out_c_desc = "CODE_SUCCESS";   
END //  
DELIMITER ;
