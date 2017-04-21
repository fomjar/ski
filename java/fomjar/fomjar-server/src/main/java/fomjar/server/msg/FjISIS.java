package fomjar.server.msg;

/**
 * <p>
 * ISIS - 互联网服务指令集，Internet Service Instruction Set
 * </p>
 * <p>
 * 注意：指令定义必须要符合协议规范
 * </p>
 *
 * @author fomjar
 */
public final class FjISIS {
    //////////////////////////////// 系统指令 ////////////////////////////////
    /** 未知指令 */
    public static final int INST_UNKNOWN    = 0xFFFFFFFF;
    /** 系统PING */
    public static final int INST_PING       = 0x00000001;
    /** 消息跟踪 */
    public static final int INST_TRACE      = 0x00000002;
    
    public static final int INST_AUTHORIZE  = 0x00000010;
    
    //////////////////////////////// 系统结果码 ////////////////////////////////
    /** 成功 */
    public static final int CODE_SUCCESS            = 0x00000000;
    
    public static final int CODE_UNKNOWN_ERROR      = 0x00000001;
    
    public static final int CODE_INTERNAL_ERROR     = 0x00000002;
    
    public static final int CODE_UNAUTHORIZED       = 0x00000010;
    
    public static final int CODE_ILLEGAL_INST       = 0x00000021;
    
    public static final int CODE_ILLEGAL_ARGS       = 0x00000022;
}
