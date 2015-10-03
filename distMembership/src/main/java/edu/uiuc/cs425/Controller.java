package edu.uiuc.cs425;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;

import org.apache.thrift.TException;

public class Controller {
	
	private ConfigAccessor  m_oConfig;
	private CommServer      m_oCommServ;
	private Membership      m_oMember;
	private Introducer      m_oIntroducer;
	private Heartbeat       m_oHeartbeat;
	private String 			m_sNodeType;
	private Thread          m_HBThread;
	private Thread 			m_FailDetThread;
	private Logger 			m_oLogger;
	private static final String sLogPath = "~/mp2/log/log.txt";
	private Scanner 		m_oUserInput;
	private String 			introIP;
	private String 			hostIP;
	
	public Controller()
	{
		m_oConfig 		= new ConfigAccessor();
		m_oCommServ		= new CommServer();
		m_oMember       = new Membership();
		m_oIntroducer   = null;
		m_oHeartbeat    = new Heartbeat();
		m_oLogger		= new Logger();
		m_oUserInput    = new Scanner(System.in);	
	}
	
	public int Initialize(String sXML)
	{
		if( Commons.FAILURE == m_oLogger.Initialize(sLogPath))
		{
			System.out.println("Failed to Initialize logger object");
			return Commons.FAILURE;
		}
		
		
		if( Commons.FAILURE == m_oConfig.Initialize(sXML))
		{
			m_oLogger.Error("Failed to Initialize XML");
			return Commons.FAILURE;
		}
		
		introIP = m_oConfig.IntroducerIP();
		hostIP = null;
		try {
			hostIP  = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			m_oLogger.Error(m_oLogger.StackTraceToString(e1));
		}
		
		m_oLogger.Info("IP: " + hostIP);
		m_oLogger.Info("Intro IP: " + introIP);
		
		
		if(introIP.equals(hostIP))
			m_sNodeType = Commons.NODE_INTROCUDER;
		else
			m_sNodeType = Commons.NODE_PARTICIPANT;
		
		
		m_oLogger.Info("Nodetype: " + m_sNodeType);
		
		m_oMember.Initialize(m_oConfig.FailureInterval(), m_oLogger);
		
		if(m_sNodeType.equals(Commons.NODE_INTROCUDER))
		{
			//Set membership obj in introducer
			
			m_oIntroducer = new Introducer(m_oMember,m_oLogger);

			if( Commons.FAILURE == m_oCommServ.Initialize(m_oConfig.HeartBeatPort(), 
					m_oMember, m_oIntroducer,m_oLogger) )
			{
				m_oLogger.Error("Failed to initialize the communication server");
				return Commons.FAILURE;
			}
		} else 
		{
			if( Commons.FAILURE == m_oCommServ.Initialize(m_oConfig.HeartBeatPort(), 
					m_oMember, m_oLogger))
			{
				m_oLogger.Error("Failed to initialize the communication server");
				return Commons.FAILURE;
			}
		}
		
		if( Commons.FAILURE == m_oHeartbeat.Initialize(m_oMember, m_oConfig, m_oLogger))
		{
			m_oLogger.Error("Failed to initialize the heartbeat sender");
			return Commons.FAILURE;
		}
		return Commons.SUCCESS;
	}
	
	public int StartAllServices()
	{
		if( m_sNodeType.equals(Commons.NODE_INTROCUDER))
		{
			m_oCommServ.StartIntroService(m_oConfig.IntroducerPort());
		}
		// bring up the heartbeat receiver
		m_oCommServ.StartHeartBeatRecvr();
		
		// start heart beating thread
		m_HBThread = new Thread(m_oHeartbeat);
		m_HBThread.start();
		
		//start failure detection thread
		m_FailDetThread = new Thread(m_oMember);
		m_FailDetThread.start();
		
		return Commons.SUCCESS;
	}
	
	
	public void LeaveList()
	{
		// stop heartbeating 
		m_oHeartbeat.StopHB();
		
		// call to memebership to set node entry to leave status
		m_oMember.TimeToLeave();
		// do final HB
		try {
			m_oHeartbeat.DoHB();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			m_oLogger.Error(m_oLogger.StackTraceToString(e));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			m_oLogger.Error(m_oLogger.StackTraceToString(e));
		}
	}
	
	
	public int IntroduceSelf()
	{
		MemberIntroProxy proxy = new MemberIntroProxy();
		int counter = 0;
		// continous pinging for introducer to connect
		while(Commons.FAILURE == proxy.Initialize(m_oConfig.IntroducerIP(), m_oConfig.IntroducerPort(), m_oLogger))
		{
			if( counter++ > 100) 
			{
				m_oLogger.Error("Failed to connect to Introducer. Exiting after 100 tries");
				return Commons.FAILURE;
			}
			
			// sleep 5 secs before next retry
			m_oLogger.Warning("Failed to connect to Introducer. Trying again in 5 secs");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				m_oLogger.Error(m_oLogger.StackTraceToString(e));
				return Commons.FAILURE;
			}
		}

		try {
			int successState = proxy.JoinGroup();
		} catch (TException e) {
			// TODO Auto-generated catch block
			m_oLogger.Error(m_oLogger.StackTraceToString(e));
			return Commons.FAILURE;
		}
		
		// checkpointing will change this part of the logic
		m_oMember.AddSelf();
		if(m_sNodeType.equals(Commons.NODE_PARTICIPANT))
		{
			ByteBuffer buf;
			try {
				buf = proxy.GetMembershipList();
			} catch (TException e1) {
				// TODO Auto-generated catch block
				m_oLogger.Error(m_oLogger.StackTraceToString(e1));
				return Commons.FAILURE;
			}
			byte[] bufArr = new byte[buf.remaining()];
			buf.get(bufArr);
			try {
				m_oMember.MergeList(bufArr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				m_oLogger.Error(m_oLogger.StackTraceToString(e));
				return Commons.FAILURE;
			}
		}
		return Commons.SUCCESS;
		
	}
	
	public void WaitForServicesToEnd()
	{
		if( m_sNodeType == Commons.NODE_INTROCUDER)
		{
			m_oCommServ.WaitForIntroServiceToStop();
		}
		
		m_oCommServ.WaitForHBRecvrToStop();
		
		try {
			m_HBThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			m_oLogger.Error(m_oLogger.StackTraceToString(e));
		}
		
		try {
			m_FailDetThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			m_oLogger.Error(m_oLogger.StackTraceToString(e));
		}
		
	}
	
	public void UserInputImpl()
	{
		Thread m_oUserInputThrd = new Thread(new Runnable() {           
            public void run() { 
            	while(true)
            	{
	            	System.out.println("=========================");
	            	System.out.println("Enter choice");
	        		System.out.println("1. Print Membership List");
	        		System.out.println("2. Leave");
	        		System.out.println("3. Print Node information");
	        		System.out.println("Enter Input ");
	        		String sInput = m_oUserInput.nextLine();
	        		if( ! sInput.equals("1") && !sInput.equals("2") && !sInput.equals("3"))
	        		{
	        			System.out.println("Invalid input");
	        			return;
	        		}
	        		
	        		if(sInput.equals("1"))
	        		{
	        			m_oMember.PrintList();
	        		} else if(sInput.equals("2"))
	        		{
	        			LeaveList();
	        		} else if(sInput.equals("3"))
	        		{
	        			System.out.println("IP: " + hostIP );
	        			System.out.println("NodeType: " + m_sNodeType );
	        			System.out.println("Unique ID: " + m_oMember.UniqueId() );
	        		}
	        		try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
        	} 
        });
		m_oUserInputThrd.start();
	}
	
}
