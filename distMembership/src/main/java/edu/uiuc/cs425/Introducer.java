package edu.uiuc.cs425;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;

import edu.uiuc.cs425.MemberIntroducer.Iface;

public class Introducer implements Iface { //Why implements Iface??

	private Membership m_oMembershipObject; //The membership object of the introducer
	
	public Introducer(Membership member)
	{
		m_oMembershipObject = member;
	}
	
	public int JoinGroup() throws TException {
		// TODO Auto-generated method stub
		//No threading so no lock
		
		return Commons.SUCCESS;
	}

	public ByteBuffer GetMembershipList() throws TException {
		// TODO Auto-generated method stub
		
		try {
			return ByteBuffer.wrap(m_oMembershipObject.GetMemberList());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
			throw new TException();
		}
		
	}

}
