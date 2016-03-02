delete from tbl_cmd_map where i_cmd = (conv(00000300, 16, 10) + 0);
insert into tbl_cmd_map values((conv(00000300, 16, 10) + 0), 'sp', 2, "sp_apply_sale(?, ?, $'c_user', $'in_c_pass_cur', $'in_c_pass_a', $'in_c_pass_b')");
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_apply_strorage` //
CREATE PROCEDURE `ski`.`sp_apply_strorage`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,      
    in    in_c_platform      varchar(16),    
    in    in_c_country       varchar(32),    
    in    in_c_url_icon      varchar(128),   
    in    in_c_url_poster    varchar(128),   
    in    in_c_url_price     varchar(128),   
    in    in_c_url_buy       varchar(128),   
    in    in_t_sale          date,           
    in    in_c_name_cns      varchar(64),    
    in    in_c_name_cnt      varchar(64),    
    in    in_c_name_en       varchar(64),    
    in    in_c_name_ori      varchar(64)         
)
label_pro:BEGIN

    declare i_gid_tmp  integer default 0;
    declare c_name_cns_tmp 

    select i_error integer default 0;
    set out_i_code = 0;
    START TRANSACTION;  
    select i_gid into i_gid_tmp from tbl_game where c_name_cns = in_c_name_cns;
    
    if isnull(i_gid_tmp) then
       select conv(F0000104, 16, 10) into out_i_code;
       set out_c_desc = "This Game has already in storage.";   
       select out_c_desc;
       leave label_pro;
    end if;
    
    select i_error; -- 打印出ERROR信息至屏幕
    IF i_error<>0 THEN  
        SELECT "RollBack";
        ROLLBACK;  
    ELSE  
        SELECT "COMMINT";
        COMMIT;  
    END IF; 
END //  
DELIMITER ; 


