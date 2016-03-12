delete from tbl_cmd_map where i_cmd = (conv(00002401, 16, 10) + 0);
insert into tbl_cmd_map values((conv(00002401, 16, 10) + 0), 'sp', 2, "sp_update_return(?, ?, $'c_user', $'in_c_pass_cur', $'in_c_pass_a', $'in_c_pass_b')");
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_return` //
CREATE PROCEDURE `ski`.`sp_update_return`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    in  in_c_user              varchar(32),
    in  in_c_pass_cur          varchar(32),
    in  in_c_pass_a            varchar(32),
    in  in_c_pass_b            varchar(32)
)
BEGIN
     
    declare out_c_desc_temp varchar(32);
    declare c_pass_temp varchar(32);
    set out_i_code = 0;
    set out_c_desc = '';
   
    if isnull(in_c_user) then
       set out_i_code = 4026532099;
    end if;
    
    if isnull(in_c_pass_a) and isnull(in_c_pass_b)  and isnull(in_c_pass_cur) then
      set out_i_code = 4026532099;
    end if;
    
    if out_i_code = 0 then 

        if (in_c_pass_a<>'') then
            update tbl_game_account 
               set c_pass_a=in_c_pass_a  
             where c_user = in_c_user;
        end if;
        
        if (in_c_pass_b<>'') then
            update tbl_game_account 
               set c_pass_b=in_c_pass_b  
             where c_user = in_c_user;
        end if;
        
        if (in_c_pass_cur<>'') then
            update tbl_game_account 
               set c_pass_cur=in_c_pass_cur  
             where c_user = in_c_user;
        end if;
        
    end if;

END //  
DELIMITER ; 