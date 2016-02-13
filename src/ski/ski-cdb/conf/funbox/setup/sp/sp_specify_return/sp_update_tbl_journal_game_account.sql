/*更新tbl_journal_game_account表*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_tbl_journal_game_account` //
CREATE PROCEDURE `ski`.`sp_update_tbl_journal_game_account`(
    in i_gaid          integer,    -- 游戏账号
    in in_c_caid       varchar(64),    -- 渠道账户ID
    in t_change        datetime,   -- 变化时间
    in i_state_before  tinyint,    -- 变化前的状态
    in i_state_after   tinyint,    -- 变化后的状态
    in i_cause         tinyint     -- 成因：0-用户操作 1-系统操作 2-人工维护操作
)
BEGIN

    INSERT INTO tbl_journal_game_account(i_gaid,c_caid,t_change,i_state_before,i_state_after,i_cause) 
             VALUES (i_gaid,in_c_caid,t_change,i_state_before,i_state_after,i_cause);

END //  
DELIMITER ; 
