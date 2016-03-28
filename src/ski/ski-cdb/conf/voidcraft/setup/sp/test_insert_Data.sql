delete from tbl_order;
delete from tbl_order_product;
delete from tbl_game_account;

insert into tbl_order(c_poid, i_coid, i_channel, c_caid, t_place) values ('abcd12345', 12345, 0, "orojewptk3o2cyrlsxuux-fuypbm", now());

insert into tbl_order_product(c_poid, i_pid, i_prod_type, c_prod_name, i_prod_inst, i_price, i_state, c_take_info, t_return_apply, t_return_done, i_refund)
                    values('abcd12345', 1, 0, '刺客信条1账号租赁A', 1, 1.5, 1, '提取码abc', now(), now(), 30.5);
insert into tbl_order_product(c_poid, i_pid, i_prod_type, c_prod_name, i_prod_inst, i_price, i_state, c_take_info, t_return_apply, t_return_done, i_refund)
                    values('abcd12345', 2, 0, '刺客信条2账号租赁A', 2, 1.6, 1, '提取码def', now(), now(), 20.5);
insert into tbl_order_product(c_poid, i_pid, i_prod_type, c_prod_name, i_prod_inst, i_price, i_state, c_take_info, t_return_apply, t_return_done, i_refund)
                    values('abcd12345', 3, 0, '刺客信条3账号租赁A', 3, 1.7, 1, '提取码ghi', now(), now(), 40.5);

insert into tbl_game_account(i_pid, i_gaid, c_user, c_pass_cur, c_pass_a, c_pass_b) values (1, 1, 'user1', 'pass_cur_1', 'pass_a_1', 'pass_b_1');
insert into tbl_game_account(i_pid, i_gaid, c_user, c_pass_cur, c_pass_a, c_pass_b) values (2, 2, 'user2', 'pass_cur_2', 'pass_a_2', 'pass_b_2');
insert into tbl_game_account(i_pid, i_gaid, c_user, c_pass_cur, c_pass_a, c_pass_b) values (3, 3, 'user3', 'pass_cur_3', 'pass_a_3', 'pass_b_3');

set @x4 = '';
call sp_query_order(@x1, @x2, @x4, "orojewptk3o2cyrlsxuux-fuypbm");
select @x4;

/*----------------测试状态机锁定用户-----------------------*/

/*账号状态*/
insert into tbl_game_account_rent(i_gaid, i_rent) values (1, 10);
insert into tbl_game_account_rent(i_gaid, i_rent) values (2, 11);
insert into tbl_game_account_rent(i_gaid, i_rent) values (3, 21);

update tbl_game_account_rent set i_rent=10 where i_gaid =1;

call sp_lock_account_return(@x1, @x2, 1, 'orojewptk3o2cyrlsxuux-fuypbm');

call sp_update_return(@x1, @x2, 'zhaoqiang1', 'cur', "", "");

call sp_unlock_account_return(@x1, @x2, 1, 'orojewptk3o2cyrlsxuux-fuypbm');
call sp_unlock_account_return(@x1, @x2, "zhaoqiang2", 1);
call sp_unlock_account_return(@x1, @x2, "zhaoqiang3", 1);

/*测试生产流水号用例*/
call sp_generate_poid(@x1, @x2, @x3);
select @x3;


/*测试级联账户*/
i_gid 1234

insert into tbl_game(i_gid, c_name_cns) values (1234, "国家宝藏");

insert into tbl_game_account_game(i_gid, i_gaid) values (1234, 2222);
insert into tbl_game_account_game(i_gid, i_gaid) values (1234, 2223);
insert into tbl_game_account_game(i_gid, i_gaid) values (1234, 2224);
insert into tbl_game_account_game(i_gid, i_gaid) values (1234, 2225);
insert into tbl_game_account_game(i_gid, i_gaid) values (1234, 2226);
insert into tbl_game_account_game(i_gid, i_gaid) values (1234, 2227);
insert into tbl_game_account_game(i_gid, i_gaid) values (1234, 2228);
insert into tbl_game_account_game(i_gid, i_gaid) values (1234, 2229);

insert into tbl_game_account_rent(i_gaid, i_rent) values (2222, 01);
insert into tbl_game_account_rent(i_gaid, i_rent) values (2223, 01);
insert into tbl_game_account_rent(i_gaid, i_rent) values (2224, 01);
insert into tbl_game_account_rent(i_gaid, i_rent) values (2225, 01);
insert into tbl_game_account_rent(i_gaid, i_rent) values (2226, 22);
insert into tbl_game_account_rent(i_gaid, i_rent) values (2227, 03);
insert into tbl_game_account_rent(i_gaid, i_rent) values (2228, 0);
insert into tbl_game_account_rent(i_gaid, i_rent) values (2229, 0);


insert into tbl_journal_game_account(i_gaid, c_caid, i_state_before, i_state_after, t_change) values (2222, "zhangsan", 01, 02, now());
insert into tbl_journal_game_account(i_gaid, c_caid, i_state_before, i_state_after, t_change) values (2222, "zhangsan", 02, 04, now());
insert into tbl_journal_game_account(i_gaid, c_caid, i_state_before, i_state_after, t_change) values (2222, "zhangsan", 04, 00, now());


insert into tbl_journal_game_account(i_gaid, c_caid, i_state_before, i_state_after, t_change) values (2223, "zhangsan", 22, 21, now());
insert into tbl_journal_game_account(i_gaid, c_caid, i_state_before, i_state_after, t_change) values (2223, "zhangsan", 21, 01, now());
insert into tbl_journal_game_account(i_gaid, c_caid, i_state_before, i_state_after, t_change) values (2223, "zhangsan", 02, 00, now());

insert into tbl_journal_game_account(i_gaid, c_caid, i_state_before, i_state_after, t_change) values (2224, "zhangsan", 21, 01, now());
insert into tbl_journal_game_account(i_gaid, c_caid, i_state_before, i_state_after, t_change) values (2225, "zhangsan", 02, 00, now());



insert into tbl_game_account(i_gaid, c_user, c_pass_cur, c_pass_a, c_pass_b) values (2222, 'zhaoqiang1', 'zhaoqiang1', 'zhaoqiang1', 'zhaoqiang1');
insert into tbl_game_account(i_gaid, c_user, c_pass_cur, c_pass_a, c_pass_b) values (2223, 'zhaoqiang2', 'zhaoqiang2', 'zhaoqiang2', 'zhaoqiang2');
insert into tbl_game_account(i_gaid, c_user, c_pass_cur, c_pass_a, c_pass_b) values (2224, 'zhaoqiang3', 'zhaoqiang3', 'zhaoqiang3', 'zhaoqiang3');

select tbl_game.i_gid, tbl_game_account_game.i_gaid, tbl_game_account_rent.i_rent, tbl_journal_game_account.t_change 
 from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent inner join tbl_journal_game_account 
 where (tbl_game.i_gid = tbl_game_account_game.i_gid) and (tbl_game_account_rent.i_gaid = tbl_game_account_game.i_gaid)
 and (tbl_game_account_game.i_gaid = tbl_journal_game_account.i_gaid) 
 and (tbl_game_account_rent.i_rent = 1)
 and (tbl_game.i_gid = 1234) 
 
 
 
 
select tbl_game.i_gid, tbl_game_account_game.i_gaid, tbl_game_account_rent.i_rent
 from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent inner join tbl_journal_game_account 
 where (tbl_game.i_gid = tbl_game_account_game.i_gid) and (tbl_game_account_rent.i_gaid = tbl_game_account_game.i_gaid)
 and (tbl_game_account_rent.i_rent = 1)
 and (tbl_game.i_gid = 1234) 
 order by tbl_game_account_game.i_gaid asc limit 1;
 
 
 
 
 
select tbl_game.i_gid, tbl_game_account_game.i_gaid, tbl_game_account_rent.i_rent
 from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent 
 where (tbl_game.i_gid = tbl_game_account_game.i_gid) 
 and (tbl_game_account_rent.i_gaid = tbl_game_account_game.i_gaid)
 and (tbl_game_account_rent.i_rent = 1)
 and (tbl_game.i_gid = 1234);
 
 select tbl_game_account_game.i_gaid
 from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent 
 where (tbl_game.i_gid = 1234) 
 and (tbl_game.i_gid = tbl_game_account_game.i_gid)
 and (tbl_game_account_game.i_gaid = tbl_game_account_rent.i_gaid)
 and (tbl_game_account_rent.i_rent = 0)
 order by tbl_game_account_game.i_gaid asc limit 1; 
 
 
/*使用left join 找到合集*/
select tbl_game.i_gid, tbl_game_account_game.i_gaid, tbl_game_account_rent.i_rent, tbl_journal_game_account.t_change 
 from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent 
 left join tbl_journal_game_account on (tbl_game_account_game.i_gaid = tbl_journal_game_account.i_gaid) 
 where (tbl_game.i_gid = tbl_game_account_game.i_gid) 
 and (tbl_game_account_rent.i_gaid = tbl_game_account_game.i_gaid) 
 and (tbl_game.i_gid = 1234) ;
 
 
 
 select tbl_game.i_gid, tbl_game_account_game.i_gaid, tbl_game_account_rent.i_rent, tbl_journal_game_account.t_change 
 from tbl_game inner join tbl_game_account_game inner join tbl_game_account_rent inner join tbl_journal_game_account 
 where (tbl_game.i_gid = tbl_game_account_game.i_gid) and (tbl_game_account_rent.i_gaid = tbl_game_account_game.i_gaid)
 and (tbl_game_account_game.i_gaid = tbl_journal_game_account.i_gaid) 
 and (tbl_game.i_gid = 1234) ;
 
 
call sp_apply_sale(@out_i_code, @out_c_desc, 5678, 0, "zhaoqiangchampion", now(), 199, 3, "国家宝藏", 0, "take_info_rand"); 

/*测试生产随机提货码*/ 
call sp_generate_takinfo(@x1, @x2, 'vc20160131234500001', 2244, @x3);
call sp_generate_takinfo(@x1, @x2, 'vc20160131234500001', 2245, @x3);
call sp_generate_takinfo(@x1, @x2, 'vc20160131234500001', 2224, @x3);
call sp_generate_takinfo(@x1, @x2, 'vc20160131234500001', 2225, @x3);



select tbl_order_product.c_take_info
 from tbl_order_product inner join tbl_order 
 where (tbl_order_product.c_poid = tbl_order.c_poid) and tbl_order_product.i_state = 0
 order by tbl_order.t_place asc limit 1;
 
 /*测试指定账号*/
 call sp_specify_sale(@x1, @x2, @x3, @x4, @x5, 5678, 'zhaoqiangchampion', '国家宝藏', 0);
 select @x3;
 
 
 set @x3 = '9ee5da2f18554896bf68f8046c5fb586';
 call sp_specify_sale(@x1, @x2, @x3, @x4, @x5, @x6, @x7, '国家宝藏', 0);
 select @x4;
 select @x5;
 
 
 select tbl_game_account.c_user, tbl_game_account.c_user.c_pass_cur
 from tbl_game_account inner join tbl_order_product 
 where (tbl_order_product.i_inst_id = tbl_game_account.i_gaid)
 and tbl_order_product.c_take_info = 0;
 
 
 
 

