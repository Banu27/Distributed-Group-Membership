package edu.uiuc.cs425;

/**
 * Hello world!
 *
 */
public class App 
{
	
	
    public static void main( String[] args )
    {
    	if( args.length !=2 )
		{
			System.out.println("Usage: java -cp ~/distMemFinal.jar edu.uiuc.cs425.App <xml_path> <intro/part>");
			System.exit(Commons.FAILURE);
		}
    	
    	// validation
		String sXML = args[0];
		String sNodeType = args[1];
		if(sNodeType != Commons.NODE_INTROCUDER && sNodeType != Commons.NODE_PARTICIPANT)
		{
			System.out.println("Usage: java -cp ~/distMemFinal.jar edu.uiuc.cs425.App <xml_path> <intro/part>");
			System.exit(Commons.FAILURE);
		}
		
		Controller m_oController = new Controller();
		if( Commons.FAILURE == m_oController.Initialize(sXML, sNodeType))
		{
			System.out.println("Failed to initialize the controller. Shutting down...");
			System.exit(Commons.FAILURE);
		}
		
		m_oController.StartAllServices();
		
		m_oController.WaitForServicesToEnd();
		
		
		
    }
		
		
}
