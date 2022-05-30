package src.rhino.onelimiter.strategy.pid;

/**
 * @description: PID控制器
 * 设计文档：https://km.sankuai.com/page/786841194
 * @author: zhangxiudong
 * @date: 2021-05-31
 **/
public class PIDController {

    private double kp;

    private double ki;

    private double kd;

    private double integral;

    private double lastErr;

    private double lastOutput;

    public PIDController(double kp, double ki, double kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    public double calcOutput(double actual, double target) {
        double err = target - actual;
        //p
        double outputP = this.kp * err;
        double errorDiff = err - this.lastErr;
        //d
        double outputD = this.kd * errorDiff;
        this.lastErr = err;
        double maxOutput = 2.0D;
        double minOutput = -1.5D;
        if (this.lastOutput >= minOutput && this.lastOutput <= maxOutput) {
            this.integral += err;
        } else if ((this.lastOutput > maxOutput && err < 0.0D) || (this.lastOutput < minOutput && err > 0.0D)) {
            this.integral += err;
        }
        //i
        double outputI = this.ki * this.integral;
        double output = outputP + outputI + outputD;
        double emaAlpha = 0.2D;
        output = this.lastOutput * emaAlpha + output * (1.0D - emaAlpha);
        this.lastOutput = output;
        return output;
    }

}
