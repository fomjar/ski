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
    public static final int INST_SYS_UNKNOWN_INST   = 0xFFFFFFFF;
    /** 系统PING */
    public static final int INST_SYS_PING           = 0x00000001;
    /** 消息跟踪 */
    public static final int INST_SYS_TRACE          = 0x00000002;
}
