package edu.uiuc.cs425;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;

import edu.uiuc.cs425.MemberIntroducer.Iface;

public class Introducer implements Iface { //Why implements Iface??

	private int m_nSerialNumber = 0;
	private Membership m_oMembershipObject; //The membership object of the introducer
	
	public int JoinGroup() throws TException {
		// TODO Auto-generated method stub
		//No threading so no lock
		
		m_nSerialNumber = m_nSerialNumber + 1;
		return m_nSerialNumber;
	}

	public ByteBuffer GetMembershipList() throws TException {
		// TODO Auto-generated method stub
		
		return m_oMembershipObject.GetMemberList();
		
	}

}
