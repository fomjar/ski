delimiter //
drop procedure if exists sp_query_channel_commodity_by_osn //
create procedure sp_query_channel_commodity_by_osn (
    out i_code  integer,
    out c_desc  mediumblob,
    in  osn     integer
)  
begin  
    declare i_osn           integer         default -1;     -- 操作序列号
    declare i_cid           integer         default -1;     -- 商品ID(对应的)
    declare t_time          datetime        default null;   -- 生成时间
    declare i_channel       tinyint         default -1;     -- 渠道类型: 0-淘宝 1-微信 2-支付宝
    declare c_item_url      varchar(250)    default null;   -- 商品引用链接
    declare c_item_cover    varchar(250)    default null;   -- 商品封面链接
    declare c_item_name     varchar(250)    default null;   -- 商品名称
    declare c_item_remark   varchar(250)    default null;   -- 商品备注
    declare i_item_sold     integer         default -1;     -- 商品售出数量
    declare c_item_price    varchar(32)     default null;   -- 商品价格
    declare i_express_price decimal(4, 2)   default -1;     -- 快递价格
    declare c_shop_url      varchar(250)    default null;   -- 店铺(级别)链接
    declare c_shop_name     varchar(64)     default null;   -- 店铺名称
    declare c_shop_owner    varchar(64)     default null;   -- 店铺卖家
    declare c_shop_rate     varchar(64)     default null;   -- 店铺级别
    declare c_shop_score    varchar(64)     default null;   -- 店铺评分
    declare c_shop_addr     varchar(100)    default null;   -- 店铺地址

    declare done        integer default 0;
    declare rs          cursor for
                        select cc.i_osn, cc.i_cid, cc.t_time, cc.i_channel, cc.c_item_url, cc.c_item_cover, cc.c_item_name, cc.c_item_remark, cc.i_item_sold, cc.c_item_price, cc.i_express_price, cc.c_shop_url, cc.c_shop_name, cc.c_shop_owner, cc.c_shop_rate, cc.c_shop_score, cc.c_shop_addr
                          from tbl_channel_commodity cc
                         where cc.i_osn = (
                            case osn
                                when -1
                                then (select max(cc2.i_osn) from tbl_channel_commodity cc2)
                                else osn
                            end
                         )
                         order by cc.t_time desc;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_osn, i_cid, t_time, i_channel, c_item_url, c_item_cover, c_item_name, c_item_remark, i_item_sold, c_item_price, i_express_price, c_shop_url, c_shop_name, c_shop_owner, c_shop_rate, c_shop_score, c_shop_addr;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_osn, 10, 16),
                '\t',
                conv(i_cid, 10, 16),
                '\t',
                ifnull(t_time, ''),
                '\t',
                ifnull(i_channel, '-1'),
                '\t',
                ifnull(c_item_url, ''),
                '\t',
                ifnull(c_item_cover, ''),
                '\t',
                ifnull(c_item_name, ''),
                '\t',
                ifnull(c_item_remark, ''),
                '\t',
                conv(i_item_sold, 10, 16),
                '\t',
                ifnull(c_item_price, '0.0'),
                '\t',
                ifnull(i_express_price, '0.0'),
                '\t',
                ifnull(c_shop_url, ''),
                '\t',
                ifnull(c_shop_name, ''),
                '\t',
                ifnull(c_shop_owner, ''),
                '\t',
                ifnull(c_shop_rate, ''),
                '\t',
                ifnull(c_shop_score, ''),
                '\t',
                ifnull(c_shop_addr, '')
        );

        fetch rs into i_osn, i_cid, t_time, i_channel, c_item_url, c_item_cover, c_item_name, c_item_remark, i_item_sold, c_item_price, i_express_price, c_shop_url, c_shop_name, c_shop_owner, c_shop_rate, c_shop_score, c_shop_addr;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
