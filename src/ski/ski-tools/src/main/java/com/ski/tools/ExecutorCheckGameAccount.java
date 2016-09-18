package com.ski.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanGameAccount;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ExecutorCheckGameAccount implements ToolExecutor {
    
    private static int      g_gid   = -1;
    private static int      g_gaid  = -1;
    private static String   g_base  = ".";
    private static String   g_file  = null;
    private static PrintStream out  = null;

    @Override
    public void execute(Map<String, String> args) {
        args.forEach((k, v)->{
            switch (k) {
            case "gid":     g_gid   = Integer.parseInt(v, 16);  break;
            case "gaid":    g_gaid  = Integer.parseInt(v, 16);  break;
            case "base":    g_base  = v;                        break;
            default:
                System.out.println(String.format("unknown argument: %s:%s", k, v));
                break;
            }
        });
        try {
            g_file = String.format("%s/output/checkgameaccount_%s.txt", g_base, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
            out = new PrintStream(new FileOutputStream(g_file));
        } catch (FileNotFoundException e) {e.printStackTrace();}
        
        print(String.format("%-40s", "fetching game..."));
        CommonService.updateGame();
        println(" done!");
        print(String.format("%-40s", "fetching game account..."));
        CommonService.updateGameAccount();
        println(" done!");
        print(String.format("%-40s", "fetching game account game..."));
        CommonService.updateGameAccountGame();
        println(" done!");
        print(String.format("%-40s", "fetching game account rent..."));
        CommonService.updateGameAccountRent();
        println(" done!");
        
        List<String> list_normal         = new LinkedList<String>();
        List<String> list_operFail        = new LinkedList<String>();
        List<String> list_passError        = new LinkedList<String>();
        List<String> list_unbindInA     = new LinkedList<String>();
        List<String> list_bindOutA    = new LinkedList<String>();
        
        List<Integer> list_gaid2check = new LinkedList<Integer>();
        if (-1 != g_gid) {
            list_gaid2check.addAll(CommonService.getGameAccountByGid(g_gid)
                    .stream()
                    .map(game->game.i_gaid)
                    .collect(Collectors.toList()));
        }
        if (-1 != g_gaid) {
            list_gaid2check.add(g_gaid);
        }
        if (-1 == g_gid && -1 == g_gaid) {
            list_gaid2check.addAll(CommonService.getGameAccountAll().values()
                    .stream()
                    .map(game->game.i_gaid)
                    .collect(Collectors.toList()));
        }
        
        println(String.format("共 %d 个帐号待检查。", list_gaid2check.size()));
        println(String.format("====================\n开始 %s\n====================", new Date()));
        int[] i = new int[] {0};
        list_gaid2check.forEach(gaid->{
            BeanGameAccount account = CommonService.getGameAccountByGaid(gaid);
            print(String.format("(%4d/%4d) 正在检测 %15s ", ++i[0], list_gaid2check.size(), account.c_user));
            
            JSONObject args_wa = new JSONObject();
            args_wa.put("user", account.c_user);
            args_wa.put("pass", account.c_pass);
            FjDscpMessage rsp = CommonService.send("wa", CommonDefinition.ISIS.INST_ECOM_APPLY_GAME_ACCOUNT_VERIFY, args_wa);
            if (null == rsp) {
                println("操作失败");
                list_operFail.add(getGameAccountDesc(gaid));
            } else if (!CommonService.isResponseSuccess(rsp)) {
                println("密码错误");
                list_passError.add(getGameAccountDesc(gaid));
            } else if (rsp.toString().contains(" unbind")
                    && CommonService.RENT_STATE_RENT == CommonService.getGameAccountRentStateByGaid(gaid, CommonService.RENT_TYPE_A)) {
                println("A在租但未绑定");
                list_unbindInA.add(getGameAccountDesc(gaid));
            } else if (rsp.toString().contains(" bind")
                && CommonService.RENT_STATE_IDLE == CommonService.getGameAccountRentStateByGaid(gaid, CommonService.RENT_TYPE_A)) {
                println("A未租但已绑定");
                list_bindOutA.add(getGameAccountDesc(gaid));
            } else {
                println("正常");
                list_normal.add(getGameAccountDesc(gaid));
            }
            // 防止IP被封
            try {Thread.sleep(10 * 1000L);}
            catch (Exception e) {e.printStackTrace();}
        });
        
        println(String.format("%d 个帐号检测完成，正在生成报告: %s", list_gaid2check.size(), g_file));
        
        println(String.format("====================\n本次共检测了 %d 个帐号。\n====================", list_gaid2check.size()));
        printGameAccountsOneClass("正常",         list_normal);
        printGameAccountsOneClass("操作失败",     list_operFail);
        printGameAccountsOneClass("密码错误",     list_passError);
        printGameAccountsOneClass("A在租但未绑定",    list_unbindInA);
        printGameAccountsOneClass("A未租但已绑定",    list_bindOutA);
        println(String.format("====================\n完成 %s\n====================", new Date()));
    }
    
    private static void print(String s) {
        System.out.print(s);
        out.print(s);
    }
    
    private static void println(String s) {
        System.out.println(s);
        out.println(s);
    }
    
    private static void printGameAccountsOneClass(String desc, List<String> accounts) {
        Collections.sort(accounts);
        println(String.format("====================\n以下帐号为：%-20s，共 %d 个。\n====================", desc, accounts.size()));
        accounts.forEach(s->println(s));
    }
    
    private static String getGameAccountDesc(int gaid) {
        return String.format("%-15s [%-30s] A:%s B:%s",
                CommonService.getGameAccountByGaid(gaid).c_user,
                (CommonService.getGameByGaid(gaid).isEmpty() ? "无游戏" : CommonService.getGameByGaid(gaid).get(0).c_name_zh_cn),
                getGameAccountRentDesc(gaid, CommonService.RENT_TYPE_A),
                getGameAccountRentDesc(gaid, CommonService.RENT_TYPE_B));
    }
    
    private static String getGameAccountRentDesc(int gaid, int type) {
        switch (CommonService.getGameAccountRentStateByGaid(gaid, type)) {
        case CommonService.RENT_STATE_IDLE: return "空闲";
        case CommonService.RENT_STATE_RENT: return "占用";
        default: return "未知";
        }
    }
    
}
