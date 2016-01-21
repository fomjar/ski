/*更改状态至A待租B待租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_AForRentBForRent` //
CREATE PROCEDURE `ski`.`sp_update_to_AForRentBForRent`(
    out out_i_state_after        integer,
    inout out_c_desc              blob,
    in  in_i_gaid                integer
)
BEGIN

    update tbl_game_account_rent set i_rent=0  where i_gaid=in_i_gaid;
    set out_i_state_after = 0;
    select CONCAT(out_c_desc,' (update status to 0)') into out_c_desc;
    
END //  
DELIMITER ; 


/*更改状态至A不可租B不可租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_ANotRentBNotRent` //
CREATE PROCEDURE `ski`.`sp_update_to_ANotRentBNotRent`(
    out out_i_state_after      integer,
    inout out_c_desc           blob,
    in  in_i_gaid              integer

)
BEGIN

    update tbl_game_account_rent set i_rent=22  where i_gaid=in_i_gaid;
    set out_i_state_after = 22;
    select CONCAT(out_c_desc,' (update status to 22)') into out_c_desc;
    
END //  
DELIMITER ; 


/*更改状态至A已租B待租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_AAlreadyRentBForRent` //
CREATE PROCEDURE `ski`.`sp_update_to_AAlreadyRentBForRent`(
    out out_i_state_after        integer,
    inout out_c_desc             blob,
    in  in_i_gaid                integer
)
BEGIN

    update tbl_game_account_rent set i_rent=10  where i_gaid=in_i_gaid;
    set out_i_state_after = 10;
    select CONCAT(out_c_desc,' (update status to 10)') into out_c_desc;
    
END //  
DELIMITER ; 


/*更改状态至A待租B已租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_AForRentBAlreadyRent` //
CREATE PROCEDURE `ski`.`sp_update_to_AForRentBAlreadyRent`(
    out out_i_state_after      integer,
    inout out_c_desc           blob,
    in  in_i_gaid              integer
)
BEGIN

    update tbl_game_account_rent set i_rent=1  where i_gaid=in_i_gaid;
    set out_i_state_after = 1;
    select CONCAT(out_c_desc,' (update status to 01)') into out_c_desc;
    
END //  
DELIMITER ; 

/*更改状态至A不可租B已租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_ANotRentBAlreadyRent` //
CREATE PROCEDURE `ski`.`sp_update_to_ANotRentBAlreadyRent`(
    out out_i_state_after      integer,
    inout out_c_desc           blob,
    in  in_i_gaid              integer
)
BEGIN

    update tbl_game_account_rent set i_rent=21  where i_gaid=in_i_gaid;
    set out_i_state_after = 21;
    select CONCAT(out_c_desc,' (update status to 21)') into out_c_desc;
    
END //  
DELIMITER ; 



/*更改状态至A已租B已租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_AAlreadyRentBAlreadyRent` //
CREATE PROCEDURE `ski`.`sp_update_to_AAlreadyRentBAlreadyRent`(
    out out_i_state_after        integer,
    inout out_c_desc             blob,
    in  in_i_gaid                integer
)
BEGIN

    update tbl_game_account_rent set i_rent=11  where i_gaid=in_i_gaid;
    set out_i_state_after = 11;
    select CONCAT(out_c_desc,' (update status to 11)') into out_c_desc;
    
END //  
DELIMITER ; 



/*更改状态至需要检查*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_NeedCheck` //
CREATE PROCEDURE `ski`.`sp_update_to_NeedCheck`(
    out out_i_state_after        integer,
    inout out_c_desc             blob,
    in  in_i_gaid                integer
)
BEGIN

    update tbl_game_account_rent set i_rent=6  where i_gaid=in_i_gaid;
    set out_i_state_after = 6;
    select CONCAT(out_c_desc,' (update status to 6)') into out_c_desc;
    
END //  
DELIMITER ; 


/*更改状态至错误状态，异常账号*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_ErrorStatus` //
CREATE PROCEDURE `ski`.`sp_update_to_ErrorStatus`(
    out out_i_state_after      integer,
    inout out_c_desc           blob,
    in  in_i_gaid              integer
)
BEGIN

    update tbl_account_rent set i_rent=7  where i_gaid=in_i_gaid;
    set out_i_state_after = 7;
    select CONCAT(out_c_desc,' (update status to 7)') into out_c_desc;
    
END //  
DELIMITER ; 