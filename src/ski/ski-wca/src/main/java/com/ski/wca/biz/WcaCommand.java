package com.ski.wca.biz;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.wca.WechatInterface;
import com.ski.wca.WechatInterface.WechatInterfaceException;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public abstract class WcaCommand {
    
    private static final Logger logger = Logger.getLogger(WcaCommand.class);
    
    public String cmd;
    
    public WcaCommand(String cmd) {this.cmd = cmd;}
    
    public abstract String execute(String server, String user, String... args);

    
    public static boolean isCommand(String line) {
        String[] fields = line.split(" ");
        if (null != getCommand(fields[0])) return true;
        return false;
    }
    
    public static WcaCommand getCommand(String cmd) {
        for (WcaCommand c : commands) {
            if (c.cmd.equalsIgnoreCase(cmd)) return c;
        }
        return null;
    }
    
    public static void dispatch(String server, String user, String content) {
        String[] fields = content.split(" ");
        String scmd = null;
        List<String> args = new LinkedList<String>();
        for (String field : fields) {
            if (null == field || 0 == field.length()) continue;
            
            if (null == scmd) scmd = field;
            else args.add(field);
        }
        
        WcaCommand cmd = getCommand(scmd);
        if (null == cmd) {
            logger.error("can not find a command for: " + content);
            return;
        }
        String error = cmd.execute(server, user, args.toArray(new String[args.size()]));
        if (null != error) {
            try {WechatInterface.customSendTextMessage(user, error);}
            catch (WechatInterfaceException e) {logger.error("send custom service message failed: " + error, e);}
        }
    }
    
    
    private static final String ERROR_WRONG_FORMAT    = "格式错误";
    private static final String ERROR_WRONG_ARGUMENT  = "参数错误";
    private static final List<WcaCommand> commands = new LinkedList<WcaCommand>();
    
    static {
        commands.add(new CmdRegister());
    }
    
    private static class CmdRegister extends WcaCommand {

        public CmdRegister() {super("reg");}

        @Override
        public String execute(String server, String user, String... args) {
            if (null == args || 2 != args.length) return ERROR_WRONG_FORMAT;
            // args[0]
            if (CommonService.getChannelAccountByUserName(args[0]).isEmpty()) return ERROR_WRONG_ARGUMENT;    // 用户不存在
            BeanChannelAccount user_taobao = CommonService.getChannelAccountByUserName(args[0]).get(0);
            if (CommonService.USER_TYPE_TAOBAO != user_taobao.i_channel) return ERROR_WRONG_ARGUMENT;     // 用户存在，但不是淘宝用户
            // args[1]
            if (!user_taobao.c_phone.equals(args[1])) { // 电话不匹配，尝试获取支付宝用户
                if (CommonService.getChannelAccountByUserName(args[1]).isEmpty()) return ERROR_WRONG_ARGUMENT;  // 电话和支付宝账号均不存在
                BeanChannelAccount user_alipay = CommonService.getChannelAccountByUserName(args[1]).get(0);
                if (CommonService.USER_TYPE_ALIPAY != user_alipay.i_channel) return ERROR_WRONG_ARGUMENT;   // 用户存在，但不是支付宝用户
            }
            
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("paid_to",     CommonService.getPlatformAccountByChannelAccount(user_taobao.i_caid));
            args_cdb.put("paid_from",   CommonService.getPlatformAccountByChannelAccount(CommonService.getChannelAccountByUserName(user).get(0).i_caid));
            FjDscpMessage req = new FjDscpMessage();
            req.json().put("fs", server);
            req.json().put("ts", "cdb");
            req.json().put("inst", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE);
            req.json().put("args", args_cdb);
            FjServerToolkit.getAnySender().send(req);
            
            return null;
        }
        
    }
    
}
