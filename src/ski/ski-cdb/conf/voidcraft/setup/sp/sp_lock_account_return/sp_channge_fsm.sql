/*更改状态至A待租B待租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_AForRentBForRent` //
CREATE PROCEDURE `ski`.`sp_update_to_AForRentBForRent`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN
    declare i_state_before integer;
    
    select i_rent 
      into i_state_before 
      from tbl_game_account_rent 
     where i_gaid = in_i_gaid;

    if(i_state_before <> 22 ) && (i_state_before <> 1) then
       select conv(F0000103, 16, 10) into out_i_code;
       set out_c_desc = "ERROR RENT STATUS.";   
       select out_c_desc;
       leave label_pro;
    end if;
    
    update tbl_game_account_rent 
       set i_rent=0  
     where i_gaid=in_i_gaid;
    
    set out_i_state_after = 0;
    /*select CONCAT(out_c_desc,' (update status to 0)') into out_c_desc;*/
    
    set out_i_code = 0;
    
    
END //  
DELIMITER ; 


/*更改状态至A不可租B不可租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_ANotRentBNotRent` //
CREATE PROCEDURE `ski`.`sp_update_to_ANotRentBNotRent`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN

    declare i_state_before integer default 0;

    select i_rent 
      into i_state_before 
      from tbl_game_account_rent 
     where i_gaid = in_i_gaid;
  
    if(i_state_before <> 10 ) && (i_state_before <> 0) then
       select conv(F0000103, 16, 10) into out_i_code;
       set out_c_desc = "ERROR RENT STATUS.";   
       select out_c_desc;
       leave label_pro;
    end if;

    update tbl_game_account_rent 
       set i_rent=22  
     where i_gaid=in_i_gaid;
     
    set out_i_state_after = 22;
    /*select CONCAT(out_c_desc,' (update status to 22)') into out_c_desc;*/
 
    set out_i_code = 0;

END //  
DELIMITER ; 


/*更改状态至A已租B待租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_AAlreadyRentBForRent` //
CREATE PROCEDURE `ski`.`sp_update_to_AAlreadyRentBForRent`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN
    declare i_state_before integer;
    
    select i_rent 
      into i_state_before 
      from tbl_game_account_rent 
     where i_gaid = in_i_gaid;

    if(i_state_before <> 22 ) && (i_state_before <> 11) then
       select conv(F0000103, 16, 10) into out_i_code;
       set out_c_desc = "ERROR RENT STATUS.";   
       select out_c_desc;
       leave label_pro;
    end if;
    
    update tbl_game_account_rent 
       set i_rent=10  
     where i_gaid=in_i_gaid;
     
    set out_i_state_after = 10;
    /*select CONCAT(out_c_desc,' (update status to 10)') into out_c_desc;*/
        
    set out_i_code = 0;
  
    
END //  
DELIMITER ; 


/*更改状态至A待租B已租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_AForRentBAlreadyRent` //
CREATE PROCEDURE `ski`.`sp_update_to_AForRentBAlreadyRent`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN
    declare i_state_before integer;
    
    select i_rent 
      into i_state_before 
      from tbl_game_account_rent 
     where i_gaid = in_i_gaid;

    if(i_state_before <> 21 ) && (i_state_before <> 0) then
       select conv(F0000103, 16, 10) into out_i_code;
       set out_c_desc = "ERROR RENT STATUS.";   
       select out_c_desc;
       leave label_pro;
    end if;
    
    update tbl_game_account_rent 
       set i_rent=1  
     where i_gaid=in_i_gaid;
     
    set out_i_state_after = 1;
    /*select CONCAT(out_c_desc,' (update status to 01)') into out_c_desc;*/
    set out_i_code = 0;
    
END //  
DELIMITER ; 

/*更改状态至A不可租B已租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_ANotRentBAlreadyRent` //
CREATE PROCEDURE `ski`.`sp_update_to_ANotRentBAlreadyRent`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN
    declare i_state_before integer;
    
    select i_rent 
      into i_state_before 
      from tbl_game_account_rent 
     where i_gaid = in_i_gaid;

    if(i_state_before <> 11 ) && (i_state_before <> 1) then
       select conv(F0000103, 16, 10) into out_i_code;
       set out_c_desc = "ERROR RENT STATUS.";   
       select out_c_desc;
       leave label_pro;
    end if;
    
    update tbl_game_account_rent 
       set i_rent=21  
     where i_gaid=in_i_gaid;
     
    set out_i_state_after = 21;
   /* select CONCAT(out_c_desc,' (update status to 21)') into out_c_desc;*/
    set out_i_code = 0;
    
END //  
DELIMITER ; 



/*更改状态至A已租B已租*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_AAlreadyRentBAlreadyRent` //
CREATE PROCEDURE `ski`.`sp_update_to_AAlreadyRentBAlreadyRent`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN
    declare i_state_before integer;
    
    select i_rent 
      into i_state_before 
      from tbl_game_account_rent 
     where i_gaid = in_i_gaid;

    if(i_state_before <> 21 ) && (i_state_before <> 10) then
       select conv(F0000103, 16, 10) into out_i_code;
       set out_c_desc = "ERROR RENT STATUS.";   
       select out_c_desc;
       leave label_pro;
    end if;

    update tbl_game_account_rent 
       set i_rent=11  
     where i_gaid=in_i_gaid;
     
    set out_i_state_after = 11;
    /*select CONCAT(out_c_desc,' (update status to 11)') into out_c_desc;*/
    set out_i_code = 0;
    
END //  
DELIMITER ; 



/*更改状态至需要检查*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_NeedCheck` //
CREATE PROCEDURE `ski`.`sp_update_to_NeedCheck`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN

    update tbl_game_account_rent 
       set i_rent=6  
     where i_gaid=in_i_gaid;
     
    set out_i_state_after = 6;
    /*select CONCAT(out_c_desc,' (update status to 6)') into out_c_desc;*/
    set out_i_code = 0;
    
END //  
DELIMITER ; 


/*更改状态至错误状态，异常账号*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_ErrorStatus` //
CREATE PROCEDURE `ski`.`sp_update_to_ErrorStatus`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN

    update tbl_account_rent 
       set i_rent=7  
     where i_gaid=in_i_gaid;
     
    set out_i_state_after = 7;
    /*select CONCAT(out_c_desc,' (update status to 7)') into out_c_desc;*/
    set out_i_code = 0;
    
END //  
DELIMITER ; 


DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_ANotRent` //
CREATE PROCEDURE `ski`.`sp_update_to_ANotRent`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN
    declare i_state_before integer;
    
    select i_rent 
      into i_state_before 
      from tbl_game_account_rent 
     where i_gaid = in_i_gaid;
    
    if ((i_state_before = 0) || (i_state_before = 10)) then 
        call sp_update_to_ANotRentBNotRent(out_i_code,out_c_desc,out_i_state_after,in_i_gaid);
    end if;

    if ((i_state_before = 1) || (i_state_before = 11)) then 
        call sp_update_to_ANotRentBAlreadyRent(out_i_code,out_c_desc,out_i_state_after,in_i_gaid);
    end if;

    /*返回值由调用的函数决定*/
    
END //  


DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_AAlreadyRent` //
CREATE PROCEDURE `ski`.`sp_update_to_AAlreadyRent`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN

    declare i_state_before integer;
    
    select i_rent 
      into i_state_before 
      from tbl_game_account_rent 
     where i_gaid = in_i_gaid;
    
    if ((i_state_before = 22) || (i_state_before = 11)) then 
        call sp_update_to_AAlreadyRentBForRent(out_i_code,out_c_desc,out_i_state_after,in_i_gaid);
    end if;

    if ((i_state_before = 21) || (i_state_before = 10)) then 
        call sp_update_to_AAlreadyRentBAlreadyRent(out_i_code,out_c_desc,out_i_state_after,in_i_gaid);
    end if;

     /*返回值由调用的函数决定*/
   
END //  
DELIMITER ;


DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_BAlreadyRent` //
CREATE PROCEDURE `ski`.`sp_update_to_BAlreadyRent`(
    out out_i_code               BIGINT,
    inout out_c_desc              blob,
    out out_i_state_after        integer,
    in  in_i_gaid                integer
)
label_pro:BEGIN
    declare i_state_before integer;
    
    select i_rent 
      into i_state_before 
      from tbl_game_account_rent 
     where i_gaid = in_i_gaid;
    
    if ((i_state_before = 21) || (i_state_before = 0)) then 
        call sp_update_to_AForRentBAlreadyRent(out_i_code,out_c_desc,out_i_state_after,in_i_gaid);
    end if;

    if ((i_state_before = 21) || (i_state_before = 10)) then 
        call sp_update_to_AAlreadyRentBAlreadyRent(out_i_code,out_c_desc,out_i_state_after,in_i_gaid);
    end if;

     /*返回值由调用的函数决定*/

    
END //  

DELIMITER ;