package edu.uiuc.cs425;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import edu.uiuc.cs425.MemberIntroducer.Iface;
import edu.uiuc.cs425.MemberIntroducer;

public class MemberIntroProxy implements Iface {
	
	private MemberIntroducer.Client m_oClient;
	private TTransport transport;
	private Logger m_oLogger;
	
	public MemberIntroProxy()
	{
		m_oClient = null;
	}
	
	public int Initialize(String sIP,int nPort,Logger oLogger)
	{
		m_oLogger	= oLogger;
		transport = new TFramedTransport(new TSocket(sIP, nPort));
	    try {
			transport.open();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			m_oLogger.Error(m_oLogger.StackTraceToString(e));
			m_oLogger.Error(new String("Failed to initialize MemberIntro proxy")); //IP????
			return Commons.FAILURE;
		}
	    m_oClient = new MemberIntroducer.Client(new TBinaryProtocol(transport));
	    m_oLogger.Info(new String("Created Member Proxy"));
		return Commons.SUCCESS;
	}

	public int JoinGroup() throws TException {
		// TODO Auto-generated method stub
		m_oLogger.Info(new String("Joining Group"));
		return m_oClient.JoinGroup();
	}

	public ByteBuffer GetMembershipList() throws TException {
		// TODO Auto-generated method stub
		m_oLogger.Info(new String("Receiving MembershipList"));
		return m_oClient.GetMembershipList();
	}

	public void Close()
	{
		transport.close();
	}
}
