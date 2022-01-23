package ampPluginAdapter;

public class DumbLogger {
	
	public static void log(String msg) {
		System.out.println(msg) ;
	}
	
	public static void log(Object who, String msg) {
		String who_ = "" ;
		if (who instanceof Class) {
			who_ = ((Class) who).getSimpleName() ;
		}
		else {
			who_ = who.getClass().getSimpleName() ;
		}
		System.out.println(">>> " + who_ + ": -- " + msg) ;
	}

}
