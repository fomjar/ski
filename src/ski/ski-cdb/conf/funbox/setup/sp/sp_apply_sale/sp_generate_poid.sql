DELIMITER // 
DROP PROCEDURE IF EXISTS `ski`.`sp_generate_poid` //
CREATE PROCEDURE `ski`.`sp_generate_poid`(
    out   out_i_code             BIGINT,
    inout out_c_desc             blob,
    out   newOrderNo             varchar(64)
     )    
BEGIN    
  DECLARE currentDate varCHAR (15) ;-- 当前日期,有可能包含时分秒     
  DECLARE maxNo INT DEFAULT 0 ; -- 离现在最近的满足条件的订单编号的流水号最后5位，如：SH2013011000002的maxNo=2     
  DECLARE oldOrderNo VARCHAR (64) DEFAULT '' ;-- 离现在最近的满足条件的订单编号     
  DECLARE orderNamePre char (2) default 'vc';
-- 根据年月日时分生成订单编号     
  SELECT 
     DATE_FORMAT(NOW(), '%Y%m%d%H%i') INTO currentDate ;-- 订单形式：前缀+年月日时分+流水号,如：SH20130110100900005      
      
  SELECT IFNULL(c_poid, '') INTO oldOrderNo     
  FROM tbl_order     
  WHERE SUBSTRING(c_poid, 3, 12) = currentDate     
    AND SUBSTRING(c_poid, 1, 2) = orderNamePre
  ORDER BY CONVERT(SUBSTRING(c_poid, -5), DECIMAL) DESC LIMIT 1 ; -- 有多条时只显示离现在最近的一条  
     
  select  oldOrderNo;
  
  IF oldOrderNo != '' THEN     
    SET maxNo = CONVERT(SUBSTRING(oldOrderNo, -5), DECIMAL) ;-- SUBSTRING(oldOrderNo, -5)：订单编号如果不为‘‘截取订单的最后5位     
  END IF ;    
  
  SELECT 
    CONCAT(orderNamePre, currentDate,  LPAD((maxNo + 1), 5, '0')) INTO newOrderNo ; -- LPAD((maxNo + 1), 5, '0')：如果不足5位，将用0填充左边     
    
  SELECT     
    newOrderNo ; 

     set out_i_code= 0;
     set out_c_desc = "CODE_SUCCESS";       
END //  
DELIMITER ; 