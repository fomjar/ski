package com.ski.game.session;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;
import com.ski.game.GameToolkit;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionPath;
import fomjar.server.session.FjSessionTask;
import net.sf.json.JSONObject;

/**
 * 请求来自WCA，向CDB请求产品详单
 * 
 * @author fomjar
 */
public class SessionTaskQueryOrder implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskQueryOrder.class);

    @Override
    public boolean onSession(FjSessionContext context, FjSessionPath path, FjMessageWrapper wrapper) {
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (msg.fs().startsWith("wca") || msg.fs().startsWith("wsi")) {
            String user = msg.argsToJsonObject().getString("user");
            context.put("user", user);
            
            JSONObject args2cdb = new JSONObject();
            args2cdb.put("user", user);
            FjDscpMessage msg2cdb = new FjDscpMessage();
            msg2cdb.json().put("fs",   server);
            msg2cdb.json().put("ts",   "cdb");
            msg2cdb.json().put("sid",  context.sid());
            msg2cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_QUERY_ORDER);
            msg2cdb.json().put("args", args2cdb);
            FjServerToolkit.getSender(server).send(msg2cdb);
            
            return true;
        } else if (msg.fs().startsWith("cdb")) {
            
            storeProductInfo(context, msg);
            
            GameToolkit.sendUserResponse(server, context.sid(), context.getString("user"), createContent(context));
            
            return true;
        } else {
            logger.error("invalid message, unsupported from: " + msg);
            return false;
        }
    }
    
    /**
     * product[0] = pid
     * product[1] = product type
     * product[2] = product name
     * product[3] = product inst
     * product[4] = account
     * product[5] = password current
     * product[6] = password a
     * product[7] = password b
     */
    private static void storeProductInfo(FjSessionContext context, FjDscpMessage msg) {
        if (null == msg.argsToJsonObject().get("desc")) {
            context.put("products", null);
            return;
        }
        
        List<Map<String, String>> products = new LinkedList<Map<String, String>>();
        String[] str_products = msg.argsToJsonObject().getJSONArray("desc").get(0).toString().split("\n");
        for (String str_product0 : str_products) {
            String[] str_product = str_product0.split("\t");
            Map<String, String> product = new HashMap<String, String>();
            product.put("pid",          str_product[0]);
            product.put("prod.type",    str_product[1]);
            product.put("prod.name",    str_product[2]);
            product.put("prod.inst",    str_product[3]);
            product.put("user",         str_product[4]);
            product.put("pass.curr",    str_product[5]);
            product.put("pass.a",       str_product[6]);
            product.put("pass.b",       str_product[7]);
            
            products.add(product);
        }
        context.put("products", products);
    }

    @SuppressWarnings("unchecked")
    private static String createContent(FjSessionContext context) {
        StringBuffer content = new StringBuffer("【" + I18N.zh(I18N.PRODUCT_LIST) + "】\n\n");
        
        FjAddress addr = FjServerToolkit.getSlb().getAddress("wsi");
        String    user = context.getString("user");
        for (Map<String, String> product : (List<Map<String, String>>)context.get("products")) {
            String url = String.format("http://%s:%d/ski-wsi?sid=%s&inst=%s&user=%s&instance=%s",
                    addr.host,
                    addr.port,
                    user,
                    Integer.toHexString(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN),
                    user,
                    product.get("prod.inst"));
            String type = null;
            switch (Integer.parseInt(product.get("prod.type"), 16)) {
                case 0: type = I18N.zh(I18N.TYPE_A); break;
                case 1: type = I18N.zh(I18N.TYPE_B); break;
                default: type = I18N.zh(I18N.UNKNOWN); break;
            }
            content.append(String.format("<a href='%s'>《%s》</a>\n%s(%s)：%s\n\n",
                    url,
                    product.get("prod.name"),
                    I18N.zh(I18N.ACCOUNT),
                    type,
                    product.get("user")));
        }
        return content.toString();
    }

}
