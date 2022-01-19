package src.basis.enumerate;

/**
 * @author caoyang
 */

public enum ResponseCodeEnum {
    /**
     * 成功
     */
    SUCCESS(200) {
        @Override
        public String getDescription() {
            return "[" + getCode() + "] SUCCESS";
        }
    },
    NOT_AUTHORIZED(403) {
        @Override
        public String getDescription() {
            return "[" + getCode() + "] Not Authorized";
        }
    },
    NOT_FOUND(404) {
        @Override
        public String getDescription() {
            return "[" + getCode() + "] Not Found";
        }
    },
    INTERNAL_ERROR(500) {
        @Override
        public String getDescription() {
            return "[" + getCode() + "] Internal Error";
        }
    },
    BAD_GATEWAY(502) {
        @Override
        public String getDescription() {
            return "[" + getCode() + "] Bad GateWay";
        }
    };

    private final int code;

    ResponseCodeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public abstract String getDescription();
}
