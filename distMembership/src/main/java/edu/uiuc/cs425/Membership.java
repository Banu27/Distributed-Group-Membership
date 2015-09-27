package edu.uiuc.cs425;

import java.nio.ByteBuffer;

public class Membership {
	
	private MembershipList m_oMembershipList;
	private int m_nTfail;
	
	public ByteBuffer GetMemeberList() {
		//return m_oMembershipList.getByteBuffer();
		return null;
	}
	
	int MergeList(MembershipList incomingList)
	{
		for(int i=0; i<incomingList.length(); i++)
		{
			//Find the same id in our list copy
			//Update heartbeat counter
				//If heartbeat counter hasnt changed, check local timestamp
				//If suspected, call DetectFailure on this id alone
			//Update the timestamp
		}
		
		
	}
	
	void DetectFailure(String nodeId) // will be called from thread as of now.
	{
		//Keep checking for Tcleanup = 2*Tfail secs and then do a detect.
		
	}

	
	
	
	
	int MergeList(byte[] list_)
	{
		return Commons.SUCCESS;
	}
	
	MembershipList m;
}
