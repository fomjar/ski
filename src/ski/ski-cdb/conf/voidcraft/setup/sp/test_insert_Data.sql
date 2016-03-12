渠道ID为：1
平台订单ID：100
i_gaid : 1,2,3
i_gid:1,2
i_poid:100
i_caid:1
i_pid:1,2,3

INSERT INTO tbl_game_account( i_gaid,c_user,c_pass_cur,c_pass_a,c_pass_b ) VALUES (1,'zhaoqiang1','zhaoqiang1','zhaoqiang1','zhaoqiang1');
INSERT INTO tbl_game_account( i_gaid,c_user,c_pass_cur,c_pass_a,c_pass_b )  VALUES (2,'zhaoqiang2','zhaoqiang2','zhaoqiang2','zhaoqiang2');
INSERT INTO tbl_game_account( i_gaid,c_user,c_pass_cur,c_pass_a,c_pass_b )  VALUES (3,'zhaoqiang3','zhaoqiang3','zhaoqiang3','zhaoqiang3');

INSERT INTO tbl_game_account_game( i_gaid,i_gid ) VALUES (1,1);
INSERT INTO tbl_game_account_game( i_gaid,i_gid ) VALUES (2,2);
INSERT INTO tbl_game_account_game( i_gaid,i_gid ) VALUES (3,1);

INSERT INTO tbl_game(i_gid,c_name_cns) VALUES (1,"刺客信条1");
INSERT INTO tbl_game(i_gid,c_name_cns) VALUES (2,"GTA罪恶都市");

INSERT INTO tbl_order(c_poid,c_caid ) VALUES (100,"oRojEwPTK3o2cYrLsXuuX-FuypBM");

INSERT INTO tbl_order_product(c_poid,i_inst_id,i_prod_type,i_pid) VALUES (100,1,0,1);
INSERT INTO tbl_order_product(c_poid,i_inst_id,i_prod_type,i_pid) VALUES (101,2,1,2);
INSERT INTO tbl_order_product(c_poid,i_inst_id,i_prod_type,i_pid) VALUES (102,3,1,3);


set @x4 ='';
call sp_query_return(@x1,@x2,@x4,"oRojEwPTK3o2cYrLsXuuX-FuypBM");
select @x4;

/*----------------测试状态机锁定用户-----------------------*/

/*账号状态*/
INSERT INTO tbl_game_account_rent(i_gaid,i_rent) VALUES (1,10);
INSERT INTO tbl_game_account_rent(i_gaid,i_rent) VALUES (2,11);
INSERT INTO tbl_game_account_rent(i_gaid,i_rent) VALUES (3,21);

update tbl_game_account_rent set i_rent=10  where i_gaid =1;

call sp_lock_account_return(@x1,@x2,1,'oRojEwPTK3o2cYrLsXuuX-FuypBM');

call sp_update_return(@x1,@x2,'zhaoqiang1','cur',"","");

call sp_unlock_account_return(@x1,@x2,1,'oRojEwPTK3o2cYrLsXuuX-FuypBM');
call sp_unlock_account_return(@x1,@x2,"zhaoqiang2",1);
call sp_unlock_account_return(@x1,@x2,"zhaoqiang3",1);

/*测试生产流水号用例*/
call sp_generate_poid(@x1,@x2,@x3);
select @x3;


/*测试级联账户*/
i_gid  1234

INSERT INTO tbl_game(i_gid,c_name_cns) VALUES (1234,"国家宝藏");

INSERT INTO  tbl_game_account_game(i_gid,i_gaid)   VALUES (1234,2222);
INSERT INTO  tbl_game_account_game(i_gid,i_gaid)   VALUES (1234,2223);
INSERT INTO  tbl_game_account_game(i_gid,i_gaid)   VALUES (1234,2224);
INSERT INTO  tbl_game_account_game(i_gid,i_gaid)   VALUES (1234,2225);
INSERT INTO  tbl_game_account_game(i_gid,i_gaid)   VALUES (1234,2226);
INSERT INTO  tbl_game_account_game(i_gid,i_gaid)   VALUES (1234,2227);
INSERT INTO  tbl_game_account_game(i_gid,i_gaid)   VALUES (1234,2228);
INSERT INTO  tbl_game_account_game(i_gid,i_gaid)   VALUES (1234,2229);

INSERT INTO  tbl_game_account_rent(i_gaid,i_rent)   VALUES (2222,01);
INSERT INTO  tbl_game_account_rent(i_gaid,i_rent)   VALUES (2223,01);
INSERT INTO  tbl_game_account_rent(i_gaid,i_rent)   VALUES (2224,01);
INSERT INTO  tbl_game_account_rent(i_gaid,i_rent)   VALUES (2225,01);
INSERT INTO  tbl_game_account_rent(i_gaid,i_rent)   VALUES (2226,22);
INSERT INTO  tbl_game_account_rent(i_gaid,i_rent)   VALUES (2227,03);
INSERT INTO  tbl_game_account_rent(i_gaid,i_rent)   VALUES (2228,0);
INSERT INTO  tbl_game_account_rent(i_gaid,i_rent)   VALUES (2229,0);


INSERT INTO  tbl_journal_game_account(i_gaid,c_caid,i_state_before,i_state_after,t_change) VALUES (2222,"zhangsan",01,02,now());
INSERT INTO  tbl_journal_game_account(i_gaid,c_caid,i_state_before,i_state_after,t_change) VALUES (2222,"zhangsan",02,04,now());
INSERT INTO  tbl_journal_game_account(i_gaid,c_caid,i_state_before,i_state_after,t_change) VALUES (2222,"zhangsan",04,00,now());


INSERT INTO  tbl_journal_game_account(i_gaid,c_caid,i_state_before,i_state_after,t_change) VALUES (2223,"zhangsan",22,21,now());
INSERT INTO  tbl_journal_game_account(i_gaid,c_caid,i_state_before,i_state_after,t_change) VALUES (2223,"zhangsan",21,01,now());
INSERT INTO  tbl_journal_game_account(i_gaid,c_caid,i_state_before,i_state_after,t_change) VALUES (2223,"zhangsan",02,00,now());

INSERT INTO  tbl_journal_game_account(i_gaid,c_caid,i_state_before,i_state_after,t_change) VALUES (2224,"zhangsan",21,01,now());
INSERT INTO  tbl_journal_game_account(i_gaid,c_caid,i_state_before,i_state_after,t_change) VALUES (2225,"zhangsan",02,00,now());



INSERT INTO tbl_game_account( i_gaid,c_user,c_pass_cur,c_pass_a,c_pass_b ) VALUES (2222,'zhaoqiang1','zhaoqiang1','zhaoqiang1','zhaoqiang1');
INSERT INTO tbl_game_account( i_gaid,c_user,c_pass_cur,c_pass_a,c_pass_b )  VALUES (2223,'zhaoqiang2','zhaoqiang2','zhaoqiang2','zhaoqiang2');
INSERT INTO tbl_game_account( i_gaid,c_user,c_pass_cur,c_pass_a,c_pass_b )  VALUES (2224,'zhaoqiang3','zhaoqiang3','zhaoqiang3','zhaoqiang3');

select tbl_game.i_gid,tbl_game_account_game.i_gaid,tbl_game_account_rent.i_rent,tbl_journal_game_account.t_change 
    from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent inner join tbl_journal_game_account 
    where  (tbl_game.i_gid = tbl_game_account_game.i_gid) and (tbl_game_account_rent.i_gaid = tbl_game_account_game.i_gaid )
    and (tbl_game_account_game.i_gaid = tbl_journal_game_account.i_gaid) 
    and (tbl_game_account_rent.i_rent = 1)
    and (tbl_game.i_gid = 1234) 
  
    
    
    
select tbl_game.i_gid,tbl_game_account_game.i_gaid,tbl_game_account_rent.i_rent
    from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent inner join tbl_journal_game_account 
    where  (tbl_game.i_gid = tbl_game_account_game.i_gid) and (tbl_game_account_rent.i_gaid = tbl_game_account_game.i_gaid )
    and (tbl_game_account_rent.i_rent = 1)
    and (tbl_game.i_gid = 1234) 
    order by  tbl_game_account_game.i_gaid asc limit 1;
    
    
    
        
    
select tbl_game.i_gid,tbl_game_account_game.i_gaid,tbl_game_account_rent.i_rent
    from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent 
    where  (tbl_game.i_gid = tbl_game_account_game.i_gid) 
    and (tbl_game_account_rent.i_gaid = tbl_game_account_game.i_gaid )
    and (tbl_game_account_rent.i_rent = 1)
    and (tbl_game.i_gid = 1234);
            
                select tbl_game_account_game.i_gaid
                from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent  
                where  (tbl_game.i_gid = 1234) 
                       and (tbl_game.i_gid = tbl_game_account_game.i_gid)
                       and (tbl_game_account_game.i_gaid = tbl_game_account_rent.i_gaid)
                       and (tbl_game_account_rent.i_rent = 0)
                order by  tbl_game_account_game.i_gaid asc limit 1;           
                
  
/*使用LEFT JOIN 找到合集*/
select tbl_game.i_gid,tbl_game_account_game.i_gaid,tbl_game_account_rent.i_rent,tbl_journal_game_account.t_change 
    from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent 
    left join tbl_journal_game_account  on (tbl_game_account_game.i_gaid = tbl_journal_game_account.i_gaid) 
    where  (tbl_game.i_gid = tbl_game_account_game.i_gid) 
    and (tbl_game_account_rent.i_gaid = tbl_game_account_game.i_gaid ) 
    and (tbl_game.i_gid = 1234) ;
    
    
    
    select tbl_game.i_gid,tbl_game_account_game.i_gaid,tbl_game_account_rent.i_rent,tbl_journal_game_account.t_change 
    from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent inner join tbl_journal_game_account 
    where  (tbl_game.i_gid = tbl_game_account_game.i_gid) and (tbl_game_account_rent.i_gaid = tbl_game_account_game.i_gaid )
    and (tbl_game_account_game.i_gaid = tbl_journal_game_account.i_gaid) 
    and (tbl_game.i_gid = 1234) ;
    
 
call sp_apply_sale(@out_i_code,@out_c_desc,5678,0,"zhaoqiangchampion",now(),199,3,"国家宝藏",0,"take_info_rand"); 

/*测试生产随机提货码*/    
call  sp_generate_takInfo(@x1,@x2,'vc20160131234500001',2244,@x3);
call  sp_generate_takInfo(@x1,@x2,'vc20160131234500001',2245,@x3);
call  sp_generate_takInfo(@x1,@x2,'vc20160131234500001',2224,@x3);
call  sp_generate_takInfo(@x1,@x2,'vc20160131234500001',2225,@x3);



select tbl_order_product.c_take_info
    from tbl_order_product inner join tbl_order 
    where  (tbl_order_product.c_poid = tbl_order.c_poid) and tbl_order_product.i_state = 0
    order by  tbl_order.t_place asc limit 1;
    
    /*测试指定账号*/
    call sp_specify_sale(@x1,@x2,@x3,@x4,@x5,5678,'zhaoqiangchampion','国家宝藏',0);
    select @x3;
    
    
    set @x3 = '9ee5da2f18554896bf68f8046c5fb586';
    call sp_specify_sale(@x1,@x2,@x3,@x4,@x5,@x6,@x7,'国家宝藏',0);
    select @x4;
    select @x5;
    
    
         select tbl_game_account.c_user,tbl_game_account.c_user.c_pass_cur
                    from tbl_game_account inner join tbl_order_product 
                    where  (tbl_order_product.i_inst_id = tbl_game_account.i_gaid)
                    and tbl_order_product.c_take_info = 0;
    
    
            
 

