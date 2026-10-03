package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation;

/** SRC second-order spring, integrated with bounded steps on the animation clock. */
final class LegacySecondaryMotion {
    private double time=Double.NaN, input, value, velocity;
    double sample(double now,double target,double frequency,double damping,double response) {
        if(Double.isNaN(time)||now<time){time=now;input=value=target;velocity=0;return value;}
        double dt=now-time;
        if(dt<=0)return value;
        time=now;
        frequency=Math.max(0,Math.min(5,frequency));damping=Math.max(0,Math.min(1,damping));
        if(frequency==0||dt>1){input=value=target;velocity=0;return value;}
        double k1=damping/(Math.PI*frequency),k2=1/Math.pow(2*Math.PI*frequency,2),k3=response*damping/(2*Math.PI*frequency);
        double derivative=(target-input)/dt;input=target;
        int steps=Math.max(1,(int)Math.ceil(dt/Math.min(.01,Math.sqrt(4*k2+k1*k1)-k1)));
        double step=dt/steps;
        for(int i=0;i<steps;i++){value+=step*velocity;velocity+=step*(k3*derivative+target-value-k1*velocity)/k2;}
        return LegacyKeyframeClip.finite(value);
    }
}
