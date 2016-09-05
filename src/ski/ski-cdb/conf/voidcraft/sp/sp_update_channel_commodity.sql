delete from tbl_instruction where i_inst = (conv('00002410', 16, 10) + 0);
insert into tbl_instruction values((conv('00002410', 16, 10) + 0), 'sp', 2, "sp_update_channel_commodity(?, ?, $osn, $cid, '$time', $channel, '$item_url', '$item_cover', '$item_name', '$item_remark', $item_sold, '$item_price', $express_price, '$shop_url', '$shop_name', '$shop_owner', '$shop_rate', '$shop_score', '$shop_addr')"); 
-- 更新游戏
delimiter //
drop procedure if exists sp_update_order //
create procedure sp_update_order (
    out i_code          integer,
    out c_desc          mediumblob,
    in  osn             integer,        -- 操作序列号
    in  cid             integer,        -- 商品ID(对应的)
    in  time            datetime,       -- 生成时间
    in  channel         tinyint,        -- 渠道类型: 0-淘宝 1-微信 2-支付宝
    in  item_url        varchar(250),   -- 商品引用链接
    in  item_cover      varchar(250),   -- 商品封面链接
    in  item_name       varchar(250),   -- 商品名称
    in  item_remark     varchar(250),   -- 商品备注
    in  item_sold       integer,        -- 商品售出数量
    in  item_price      varchar(32),    -- 商品价格(或范围)
    in  express_price   decimal(4, 2),  -- 快递价格
    in  shop_url        varchar(250),   -- 店铺(级别)链接
    in  shop_name       varchar(64),    -- 店铺名称
    in  shop_owner      varchar(64),    -- 店铺卖家
    in  shop_rate       integer,        -- 店铺级别
    in  shop_score      varchar(64),    -- 店铺评分
    in  shop_addr       varchar(100)    -- 店铺地址
)
begin
    declare di_count    integer default -1;
    declare di_osn      integer default -1;

    if oid is null then
        set i_code = 2;
        set c_desc = 'illegal argument, oid must be not null';
    elseif cid is null then
        set i_code = 2;
        set c_desc = 'illegal argument, cid must be not null';
    else
        insert into tbl_channel_commodity (
            i_osn,
            i_cid,
            t_time,
            i_channel,
            c_item_url,
            c_item_cover,
            c_item_name,
            c_item_remark,
            i_item_sold,
            c_item_price,
            i_express_price,
            c_shop_url,
            c_shop_name,
            c_shop_owner,
            i_shop_rate,
            c_shop_score,
            c_shop_addr
        ) values (
            osn,
            cid,
            ifnull(time, now()),
            channel,
            item_url,
            item_cover,
            item_name,
            item_remark,
            item_sold,
            item_price,
            express_price,
            shop_url,
            shop_name,
            shop_owner,
            shop_rate,
            shop_score,
            shop_addr
        );
    end if;

    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
