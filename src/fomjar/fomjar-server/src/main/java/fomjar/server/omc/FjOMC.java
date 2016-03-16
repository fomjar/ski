package fomjar.server.omc;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjISIS;

/**
 * 操作维护中心的统一入口（Operation Maintenance Center）
 * 
 * @author fomjar
 */
public class FjOMC {

    public static void dot(String fs, String ts, FjDscpMessage msg) {
        FjDscpMessage trcmsg = new FjDscpMessage();
        trcmsg.json().put("fs",   fs);
        trcmsg.json().put("ts",   ts);
        trcmsg.json().put("sid",  msg.sid());
        trcmsg.json().put("inst", FjISIS.INST_SYS_TRACE);
        trcmsg.json().put("args", msg);
        FjServerToolkit.getSender(fs).send(trcmsg);
    }
    
}
