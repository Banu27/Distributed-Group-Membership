package edu.uiuc.cs425;

import java.io.IOException;
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
	
	public int Initialize(String sXML)
	{
		if( Commons.FAILURE == m_oConfig.Initialize(sXML))
		{
			System.out.println("Failed to Initialize XML");
			return Commons.FAILURE;
		}
		
		String introIP = m_oConfig.IntroducerIP();
		String hostIP = null;
		try {
			hostIP  = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("IP: " + hostIP);
		System.out.println("Intro IP: " + introIP);
		
		
		if(introIP.equals(hostIP))
			m_sNodeType = Commons.NODE_INTROCUDER;
		else
			m_sNodeType = Commons.NODE_PARTICIPANT;
		
		
		System.out.println("Nodetype: " + m_sNodeType);
		
		m_oMember.Initialize(m_oConfig.FailureInterval());
		
		if(m_sNodeType.equals(Commons.NODE_INTROCUDER))
		{
			//Set membership obj in introducer
			
			m_oIntroducer = new Introducer(m_oMember);

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
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public int IntroduceSelf()
	{
		MemberIntroProxy proxy = new MemberIntroProxy();
		
		// continous pinging for introducer to connect
		while(Commons.FAILURE == proxy.Initialize(m_oConfig.IntroducerIP(), m_oConfig.IntroducerPort()))
		{
			// sleep 5 secs before next retry
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return Commons.FAILURE;
			}
		}

		try {
			int successState = proxy.JoinGroup();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
