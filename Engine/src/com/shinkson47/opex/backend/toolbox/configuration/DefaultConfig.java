package com.shinkson47.opex.backend.toolbox.configuration;

//TODO interface
public class DefaultConfig implements OPEXConfig {

	// Error Management
	public static int ErrorMargin = 5;
	public static boolean AllowEIS = true;
	public static boolean AllowErrNotif = true;
	public static boolean AllowCascadeDetection = true;
	public static long MillisTollerance = 100;
	public static int CascadeTollerance = 3;

}
