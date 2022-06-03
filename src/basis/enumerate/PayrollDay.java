package src.basis.enumerate;

/**
 * 策略枚举
 * @author caoyang
 * @create 2022-06-03 22:03
 */
public enum PayrollDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY(PayType.WEEKEND),
    SUNDAY(PayType.WEEKEND);

    private final PayType payType;

    PayrollDay(PayType payType) {
        this.payType = payType;
    }

    PayrollDay() {this(PayType.WEEKDAY);}

    public int pay(int hours, int payRate){
       return payType.pay(hours, payRate);
    }

    private enum PayType{
        WEEKDAY{
            @Override
            int overtimePay(int hours, int payRate) {
                return hours <= BASE ? 0 : (hours - BASE) * payRate;
            }
        },
        WEEKEND{
            @Override
            int overtimePay(int hours, int payRate) {
                return hours * payRate * 2;
            }
        };
        private static final int BASE = 8;
        abstract int overtimePay(int hours, int payRate);

        private int pay(int hours, int payRate){
            return BASE * payRate + overtimePay(hours, payRate);
        }
    }
}
