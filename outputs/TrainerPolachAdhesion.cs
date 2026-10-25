using System;

namespace RailwayDynamics.Traction {
    public struct PolachParameters {
        public double Mu0,A,B,Ka,Ks;
        public PolachParameters(double mu0,double a,double b,double ka,double ks){Mu0=mu0;A=a;B=b;Ka=ka;Ks=ks;}
        // Polach (2005) Table 2, not a calibrated 2ES6 rail condition.
        public static PolachParameters DryRail{get{return new PolachParameters(.55,.40,.60,1,.40);}}
        public static PolachParameters WetRail{get{return new PolachParameters(.30,.40,.20,.30,.10);}}
        // Illustrative sanded parameters; not identified from measurements.
        public static PolachParameters SandedRail{get{return new PolachParameters(.60,.50,.50,1,.50);}}
    }
    /// <summary>
    /// No-spin reduction of Polach (2005), Eqs.4/9/11; directional Kalker
    /// stiffness approximation for combined longitudinal/lateral creep.
    /// mu(w) is phenomenological: no temperature or wear state is simulated.
    /// SI inputs; output N on the VEHICLE: Fx>0 when R*omega>V, Fy opposes Vy.
    /// Wheel reaction torque is -R*Fx. Contact dissipation is
    /// Fx*(R*omega-V)-Fy*Vy >= 0; combined vehicle/wheel power is its negative.
    /// </summary>
    public static class PolachAdhesionSolver {
        public const double ShearModulusG=8.4e10;
        public static void ComputeCreepForces(double wheelLoadQ,double vehicleSpeedV,double wheelOmega,
            double wheelRadius,double lateralVelocityVy,double semiAxisA,double semiAxisB,
            double kalkerC11,double kalkerC22,PolachParameters parameters,out double fx,out double fy) {
            fx=fy=0;
            Nonnegative(wheelLoadQ,"wheelLoadQ");Positive(wheelRadius,"wheelRadius");
            Positive(semiAxisA,"semiAxisA");Positive(semiAxisB,"semiAxisB");
            Nonnegative(kalkerC11,"kalkerC11");Nonnegative(kalkerC22,"kalkerC22");
            CheckFinite(vehicleSpeedV,"vehicleSpeedV");CheckFinite(wheelOmega,"wheelOmega");CheckFinite(lateralVelocityVy,"lateralVelocityVy");
            Nonnegative(parameters.Mu0,"Mu0");Unit(parameters.A,"A");Nonnegative(parameters.B,"B");Unit(parameters.Ka,"Ka");Unit(parameters.Ks,"Ks");
            if(parameters.Ks>parameters.Ka)throw new ArgumentException("Require 0 <= Ks <= Ka <= 1");
            if(wheelLoadQ==0||parameters.Mu0==0||parameters.Ka==0)return;
            double wx=wheelOmega*wheelRadius-vehicleSpeedV;
            if(!Finite(wx))throw new ArithmeticException("Contact slip overflow");
            double vRef=Math.Max(Math.Abs(vehicleSpeedV),.05); // explicit low-speed regularization
            double sx=wx/vRef,sy=-lateralVelocityVy/vRef,s=Hypot(sx,sy);
            if(s==0)return;
            double w=Hypot(wx,lateralVelocityVy);
            double mu=parameters.Mu0*((1-parameters.A)*Math.Exp(-parameters.B*w)+parameters.A);
            if(mu==0)return; // frictionless limit, including exponential underflow
            double coefficient=Hypot(kalkerC11*(sx/s),kalkerC22*(sy/s));
            // Eq.4: pi/4 is essential to the Kalker small-creep slope.
            double epsilon=(Math.PI*.25*ShearModulusG*semiAxisA*semiAxisB*coefficient/(wheelLoadQ*mu))*s;
            double ka=parameters.Ka*epsilon,ks=parameters.Ks==0?0:parameters.Ks*epsilon;
            double rational=double.IsPositiveInfinity(ka)?0:(ka>1?(1/ka)/(1+(1/ka)*(1/ka)):ka/(1+ka*ka));
            double magnitude=(2/Math.PI)*wheelLoadQ*mu*(rational+Math.Atan(ks));
            fx=magnitude*(sx/s);fy=magnitude*(sy/s);
            if(!Finite(fx)||!Finite(fy))throw new ArithmeticException("Contact force overflow");
        }
        static double Hypot(double x,double y){x=Math.Abs(x);y=Math.Abs(y);double m=Math.Max(x,y);if(m==0)return 0;
            double h=m*Math.Sqrt((x/m)*(x/m)+(y/m)*(y/m));if(!Finite(h))throw new ArithmeticException("Contact norm overflow");return h;}
        static bool Finite(double v){return !double.IsNaN(v)&&!double.IsInfinity(v);}
        static void CheckFinite(double v,string name){if(!Finite(v))throw new ArgumentOutOfRangeException(name);}
        static void Nonnegative(double v,string name){CheckFinite(v,name);if(v<0)throw new ArgumentOutOfRangeException(name);}
        static void Positive(double v,string name){CheckFinite(v,name);if(v<=0)throw new ArgumentOutOfRangeException(name);}
        static void Unit(double v,string name){Nonnegative(v,name);if(v>1)throw new ArgumentOutOfRangeException(name);}
    }
}
