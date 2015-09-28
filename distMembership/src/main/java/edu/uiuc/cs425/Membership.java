package edu.uiuc.cs425;

import java.nio.ByteBuffer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

import edu.uiuc.cs425.MembershipList.Member;
import edu.uiuc.cs425.MembershipList.MemberList;

public class Membership {
	
	private MembershipList m_oMembershipList;
	private int m_nTfail;
	private MemberList.Builder m_oMembershipListBuilder;
	//private String m_sFileName;
	
	public void InitializeMemberList()
	{
		m_oMembershipListBuilder = MemberList.newBuilder();
	}
	
	public void AddMember(String nodeId, int heartbeatCounter, int localTime)
	{
		  
		Member.Builder member = Member.newBuilder();
		member.setNodeId(nodeId);
		member.setHeartbeatCounter(heartbeatCounter);
		member.setLocalTime(localTime);
		m_oMembershipListBuilder.addMemberList(member.build());
		m_oMembershipList.build();
	 }
	

	/*public void UpdateMemberList(String nodeId, int heartbeatCounter, int localTime)
	{
		/*MemberList.Builder memberList = MemberList.newBuilder();

	    // Read the existing address book.
	    try {
	      memberList.mergeFrom(new FileInputStream(fileName));
	    } catch (FileNotFoundException e) {
	      System.out.println(fileName + ": File not found.  Creating a new file.");
	    }

	    // Add a member
	    m_oMembershipListBuilder.addMemberList(AddMember(nodeId, heartbeatCounter, localTime));

	    // Write the new member list back to disk.
	    ///FileOutputStream output = new FileOutputStream(m_sFileName);
	    m_oMembershipList.build();
	    //.writeTo(output);
	    //output.close();
	}*/
	
	public ByteBuffer GetMemeberList() {
		//return m_oMembershipList.getByteBuffer();
		return null;
	}
	
	int MergeList(MembershipList incomingList)
	{
		//MemberList memberList = MemberList.parseFrom(new FileInputStream(fileName));
		for(Member member : m_oMembershipList.getMemberList())
		{ 
			//How do I search??
			if(member.getHeartbeatCounter() == incomingListMember.getHeartbeatCounter())
			{
				if(member.getLocalTime() > m_nTfail)
				{
					DetectFailure(member.getNodeId());
				}
			}
			else
			{
				member.setHeartbeatCounter(incomingListMember.getHeartbeatCounter());
			}
			
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
	
	
}
