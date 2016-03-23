/*¸ü¸Ä×´Ì¬ÖÁA´ý×âB´ý×â*/
DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_update_to_BNotRent` //
CREATE PROCEDURE `ski`.`sp_update_to_BNotRent`(
    out   out_i_code              integer,
    inout out_c_desc              blob,
    in    in_i_gaid               integer
)
BEGIN
    declare i_rent_tmp integer default 0;
    select i_rent into i_rent_tmp from tbl_game_account_rent where i_gaid= in_i_gaid;
    
    if i_rent = 0 then 
        call sp_update_to_AForRentBAlreadyRent(out_i_code,out_c_desc,in_i_gaid);
    elseif  i_rent = 1 then 
        call sp_update_to_AAlreadyRentBAlreadyRent(out_i_code,out_c_desc,in_i_gaid);
    end if;
    
     set out_i_code= 0;
     set out_c_desc = "CODE_SUCCESS";   

END //  
DELIMITER ; 

