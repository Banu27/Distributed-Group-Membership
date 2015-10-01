package edu.uiuc.cs425;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

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
	
	public Controller()
	{
		m_oConfig 		= new ConfigAccessor();
		m_oCommServ		= new CommServer();
		m_oMember       = new Membership();
		m_oIntroducer   = null;
		m_oHeartbeat    = new Heartbeat();
	}
	
	public int Initialize(String sXML) throws UnknownHostException
	{
		
		
		if( Commons.FAILURE == m_oConfig.Initialize(sXML))
		{
			System.out.println("Failed to Initialize XML");
			return Commons.FAILURE;
		}
		
		System.out.println("IP: " + InetAddress.getLocalHost().getHostAddress());
		
		try {
			if( InetAddress.getLocalHost().getHostAddress() == m_oConfig.IntroducerIP())
				m_sNodeType = Commons.NODE_INTROCUDER;
			else
				m_sNodeType = Commons.NODE_PARTICIPANT;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Commons.FAILURE;
		}
		
		System.out.println("Nodetype: " + m_sNodeType);
		
		m_oMember.Initialize();
		
		if(m_sNodeType == Commons.NODE_INTROCUDER)
		{
			m_oIntroducer = new Introducer();
			if( Commons.FAILURE == m_oCommServ.Initialize(m_oConfig.HeartBeatPort(), 
					m_oMember, m_oIntroducer) )
			{
				System.out.println("Failed to initialize the communication server");
				return Commons.FAILURE;
			}
		} else 
		{
			if( Commons.FAILURE == m_oCommServ.Initialize(m_oConfig.HeartBeatPort(), 
					m_oMember))
			{
				System.out.println("Failed to initialize the communication server");
				return Commons.FAILURE;
			}
		}
		
		if( Commons.FAILURE == m_oHeartbeat.Initialize(m_oMember, m_oConfig))
		{
			System.out.println("Failed to initialize the heartbeat sender");
			return Commons.FAILURE;
		}
		
		return Commons.SUCCESS;
	}
	
	public int StartAllServices()
	{
		if( m_sNodeType == Commons.NODE_INTROCUDER)
		{
			m_oCommServ.StartIntroService(m_oConfig.IntroducerPort());
		}
		// bring up the heartbeat receiver
		m_oCommServ.StartHeartBeatRecvr();
		
		// start heart beating thread
		m_HBThread = new Thread(m_oHeartbeat);
		m_HBThread.start();
		
		//start failure detection thred
		m_FailDetThread = new Thread(m_oMember);
		m_FailDetThread.start();
		
		return Commons.SUCCESS;
	}
	
	public int IntroduceSelf()
	{
		MemberIntroProxy proxy = new MemberIntroProxy();
		if (Commons.FAILURE == proxy.Initialize(m_oConfig.IntroducerIP(), m_oConfig.IntroducerPort()))
		{
			System.out.println("Failed to initialize proxy to the introducer");
			return Commons.FAILURE;
		}
		int serialNo;
		try {
			serialNo = proxy.JoinGroup();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Commons.FAILURE;
		}
		
		m_oMember.AddSelf(serialNo);
		ByteBuffer buf;
		try {
			buf = proxy.GetMembershipList();
		} catch (TException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return Commons.FAILURE;
		}
		byte[] bufArr = new byte[buf.remaining()];
		buf.get(bufArr);
		try {
			m_oMember.MergeList(bufArr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Commons.FAILURE;
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
			e.printStackTrace();
		}
		
		try {
			m_FailDetThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
