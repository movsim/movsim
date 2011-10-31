package org.movsim.consumption;

public class FuelConstants {

	private FuelConstants(){} //prevents instantiation

	public static double GRAVITATION  = 9.81;// grav. acceleration (m/s^2)
	
	public static double RHO_AIR = 1.29; // 1.29 (kg/m^3) @ 0 cels, 1014 hPa
	
	public static double RHO_FUEL  = 760; // density of "Benzin" (kg/m^3)
	public static double RHO_FUEL_PER_LITER = RHO_FUEL/1000.; // density of "Benzin" (kg/l)
	   
	public static double CALORIC_DENS = 44e6;// "Benzin": 44 MJ/kg (--> 0.76*44 JM/liter)

	// Tranform g/kWh => m^3/(Ws): 0.001 kg/(1000W*3600s) = 1/(3.6e9) 
	public static double CONVERSION_GRAMM_PER_KWH_TO_SI = 1./(RHO_FUEL*3.6e9);  
	
	public static double CONVERSION_BAR_TO_PASCAL = 1e5;
	
}
