
delimiter //
drop procedure if exists sp_message_focus_count //
create procedure sp_message_focus_count (
    out result  integer,
    in  mid     varchar(128)
)
begin
    declare di_count        tinyint         default 0;
    declare dc_statement    varchar(300)    default null;

    select count(1)
      into di_count
      from information_schema.tables
     where table_name = concat('tbl_message_', left(mid, 6), '_focus');

    if di_count = 0 then
        set result = 0;
    else
        set dc_statement = concat("select count(1) into @result_1 from tbl_message_", left(mid, 6), "_focus where c_mid = \"", mid ,"\" and i_type = 1");
        set @s = dc_statement;
        prepare s from @s;
        execute s;
        deallocate prepare s;

        set dc_statement = concat("select count(1) into @result_2 from tbl_message_", left(mid, 6), "_focus where c_mid = \"", mid ,"\" and i_type = 2");
        set @s = dc_statement;
        prepare s from @s;
        execute s;
        deallocate prepare s;

        set result = @result_1 - @result_2;
    end if;
end //
delimiter ;



delimiter //
drop procedure if exists sp_message_reply_count //
create procedure sp_message_reply_count (
    out result  integer,
    in  mid     varchar(128)
)
begin
    declare di_count        tinyint         default 0;
    declare dc_statement    varchar(300)    default null;

    select count(1)
      into di_count
      from information_schema.tables
     where table_name = concat('tbl_message_', left(mid, 6), '_reply');

    if di_count = 0 then
        set result = 0;
    else
        set dc_statement = concat("select count(1) into @result from tbl_message_", left(mid, 6), "_reply where c_mid = \"", mid ,"\"");
        set @s = dc_statement;
        prepare s from @s;
        execute s;
        deallocate prepare s;

        set result = @result;
    end if;
end //
delimiter ;


delimiter //
drop function if exists fn_distance_great_circle;
create function fn_distance_great_circle (
    lat1    decimal(24, 20),
    lng1    decimal(24, 20),
    lat2    decimal(24, 20),
    lng2    decimal(24, 20)
)
returns integer
begin

    declare EARTH_RADIUS    integer         default 6378137;
    declare rlat1           decimal(24, 20) default 0;
    declare rlat2           decimal(24, 20) default 0;
    declare a               decimal(24, 20) default 0;
    declare b               decimal(24, 20) default 0;

    set rlat1 = radians(lat1);
    set rlat2 = radians(lat2);

    set a = rlat1 - rlat2;
    set b = radians(lng1) - radians(lng2);

    return 2 * asin(sqrt(pow(sin(a / 2), 2) + cos(rlat1) * cos(rlat2) * pow(sin(b / 2), 2))) * EARTH_RADIUS;

end //
delimiter ;


delimiter //
drop function if exists fn_distance_flattern;
create function fn_distance_flattern (
    lat1    decimal(24, 20),
    lng1    decimal(24, 20),
    lat2    decimal(24, 20),
    lng2    decimal(24, 20)
)
returns integer
begin

    declare EARTH_RADIUS    integer         default 6378137;
    declare f               decimal(24, 20) default 0;
    declare g               decimal(24, 20) default 0;
    declare l               decimal(24, 20) default 0;
    declare sf              decimal(24, 20) default 0;
    declare sg              decimal(24, 20) default 0;
    declare sl              decimal(24, 20) default 0;
    declare s               decimal(24, 20) default 0;
    declare c               decimal(24, 20) default 0;
    declare w               decimal(24, 20) default 0;
    declare r               decimal(24, 20) default 0;
    declare fl              decimal(24, 20) default 0;
    declare h1              decimal(24, 20) default 0;
    declare h2              decimal(40, 20) default 0;

    if lat1 = lat2 and lng1 = lng2 then
        return 0;
    else
        set f = radians((lat1 + lat2) / 2);
        set g = radians((lat1 - lat2) / 2);
        set l = radians((lng1 - lng2) / 2);

        set sf = sin(f);
        set sg = sin(g);
        set sl = sin(l);

        set fl = 1/298.257;

        set sf = sf * sf;
        set sg = sg * sg;
        set sl = sl * sl;

        set s = sg * (1 - sl) + (1 - sf) * sl;
        set c = (1 - sg) * (1 - sl) + sf * sl;

        set w = atan(sqrt(s / c));
        set r = sqrt(s * c) / w;

        set h1 = (3 * r - 1) / 2 / c;
        set h2 = (3 * r + 1) / 2 / s;

        return 2 * w * EARTH_RADIUS * (1 + fl * (h1 * sf * (1 - sg) - h2 * (1 - sf) * sg));
    end if;

end //
delimiter ;


delimiter //
drop procedure if exists sp_generate_geohash;
create procedure sp_generate_geohash (
    in  geohash6 varchar(16)
)
begin

    declare n   integer         default 0;
    declare cl  tinyint         default 0;
    declare ch  varchar(16)     default null;
    declare st  varchar(500)    default null;

    delete from tmp_geohash;

    select count(1)
      into n
      from information_schema.tables
     where table_name = concat('tbl_message_', geohash6);

    if 0 < n then
        insert into tmp_geohash (c_geohash) values (geohash6);
    end if;

    set cl = length(geohash6);
    while cl > 0 and length(geohash6) - cl < 3 do
        set cl = cl - 1;
        set ch = left(geohash6, cl);

        set st = concat("insert into tmp_geohash ",
                        "select right(table_name, 6) ",
                        "  from information_schema.tables ",
                        " where table_name like 'tbl_message_", ch , "%' ",
                        "   and table_name not like 'tbl_message_%_focus' ",
                        "   and table_name not like 'tbl_message_%_reply'");

        set @s = st;
        prepare s from @s;
        execute s;
        deallocate prepare s;

    end while;

    update tmp_geohash
       set c_geohash = right(c_geohash, 6);

end //
delimiter ;




