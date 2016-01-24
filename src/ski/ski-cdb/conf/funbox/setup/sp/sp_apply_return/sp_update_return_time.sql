DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_add_str_to_blob` //   
CREATE PROCEDURE `ski`.`sp_add_str_to_blob`(
    out   out_c_code             varchar(32),
    inout out_c_desc             blob,
    inout out_b_infor            MEDIUMBLOB,
    in  in_string                varchar(255),
    in  in_c_separator           varchar(32)
)  
BEGIN  
    declare in_string_tmp varchar(32);
    select convert(in_string USING utf8) into in_string_tmp;
    select concat(out_b_infor,in_string_tmp) into out_b_infor;
    select concat(out_b_infor,in_c_separator) into out_b_infor;
    /*打印当前的out_c_desc至屏幕*/
    select out_b_infor;
    
    set out_c_code="0";
    set out_c_desc = "CODE_SUCCESS";   
    
END //  
DELIMITER ; 