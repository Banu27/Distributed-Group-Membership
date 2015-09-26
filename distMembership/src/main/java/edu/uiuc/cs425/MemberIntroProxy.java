package edu.uiuc.cs425;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import edu.uiuc.cs425.MemberIntroducer.Iface;
import edu.uiuc.cs425.MemberIntroducer;

public class MemberIntroProxy implements Iface {
	
	private MemberIntroducer.Client m_oClient;
	
	public MemberIntroProxy()
	{
		m_oClient = null;
	}
	
	public int Initialize(String sIP,int nPort)
	{
		TTransport transport = new TSocket(sIP, nPort);
	    try {
			transport.open();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to initialize MemberIntro proxy");
			return Commons.FAILURE;
		}
	    m_oClient = new MemberIntroducer.Client(new  TBinaryProtocol(transport));
		return Commons.SUCCESS;
	}

	public int JoinGroup() throws TException {
		// TODO Auto-generated method stub
		return m_oClient.JoinGroup();
	}

	public ByteBuffer GetMemebershipList() throws TException {
		// TODO Auto-generated method stub
		return m_oClient.GetMemebershipList();
	}

}
