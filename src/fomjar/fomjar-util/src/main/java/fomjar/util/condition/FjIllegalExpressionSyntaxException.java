package fomjar.util.condition;

public class FjIllegalExpressionSyntaxException extends Exception {

    private static final long serialVersionUID = 9070793722900808383L;

    public FjIllegalExpressionSyntaxException() {
        super();
    }

    public FjIllegalExpressionSyntaxException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FjIllegalExpressionSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public FjIllegalExpressionSyntaxException(String message) {
        super(message);
    }

    public FjIllegalExpressionSyntaxException(Throwable cause) {
        super(cause);
    }

}
