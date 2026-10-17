using System;
namespace TrainerAir {
 public static class KvtProfile {
  // RE6 section5.2.5: four brake stages; native codes differ from215 handle numbers.
  // Nominal railway kgf convention, centres of the documented ranges.
  // Applies ONLY to independent pilot command. Automatic brake release requires its own circuit.
  public static double IndependentSetpoint(int nativeCode){
   switch(nativeCode){case 1:case 2:return 0;case 3:return 1.15;case 4:return 1.85;case 5:return 2.85;case 6:return 3.9;
    default:throw new ArgumentException("Unknown native KVT position");}
  }
 }
}
