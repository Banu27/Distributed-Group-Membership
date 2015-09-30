package edu.uiuc.cs425;
import edu.uiuc.cs425.MemberIntroImpl;

import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TSimpleServer;

import edu.uiuc.cs425.HeartBeatReceiver;

public class CommServer {
	
	private MemberIntroImpl 		m_oIntroImpl;
	private HeartBeatReceiver       m_oHBRecvr;
	private Thread 					m_oIntroServThread;
	private Thread 					m_oHBRecvrThread;
	
	
	public int Initialize(int nHBPort, Membership oMember, Introducer oIntroducer)
	{
		m_oIntroImpl 		= new MemberIntroImpl();
		if( Commons.FAILURE == m_oIntroImpl.Initialize())
		{
			System.out.println("Failed to initialize the the thrift introducer");
			return Commons.FAILURE;
		}
		m_oIntroImpl.SetIntoObj(oIntroducer);
		
		
		Initialize(nHBPort,oMember);
		return Commons.SUCCESS;
	}
	
	
	public int Initialize(int nHBPort, Membership oMember)
	{
		m_oHBRecvr 			= new HeartBeatReceiver();
		if( Commons.FAILURE == m_oHBRecvr.Initialize(nHBPort))
		{
			System.out.println("Failed to initialize the heartbeat receiver");
			return Commons.FAILURE;
		}
		m_oHBRecvr.SetMembershipObj(oMember);
		
		m_oIntroServThread  = null;
		m_oHBRecvrThread    = null;
		return Commons.SUCCESS;
	}
	
	public void StartIntroService(final int nPort)
	{
		m_oIntroServThread = new Thread(new Runnable() {           
            public void run() { 
            	try {
        			TServerTransport serverTransport = new TServerSocket(nPort);
        		    TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(new MemberIntroducer.Processor(m_oIntroImpl)));
        		    server.serve();
        		} catch (TException e)
        		{
        			e.printStackTrace();
        		}
        		return;
        	} 
        });
		m_oIntroServThread.start();
	}
	
	public void StartHeartBeatRecvr()
	{
		m_oHBRecvrThread = new Thread(new Runnable() {           
            public void run() { 
            	try {
        			m_oHBRecvr.StartService();
        		} catch (Exception e)
        		{
        			System.out.println("Failed to start the heartbeat receiver");
        			e.printStackTrace();
        		}
        		return;
        	} 
        });
		m_oHBRecvrThread.start();
		
		
	}
	
	public void WaitForIntroServiceToStop()
	{
		try {
			m_oIntroServThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void WaitForHBRecvrToStop()
	{
		try {
			m_oHBRecvrThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
