package edu.uiuc.cs425;

import java.nio.ByteBuffer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

import edu.uiuc.cs425.MembershipList.MemberList;

public class Membership {
	
	//private MembershipList m_oMembershipList;
	private int m_nTfail;
	
	private MemberList.Member AddMember(String nodeId, int heartbeatCounter, int localTime)
	{
		MemberList.Member.Builder memberList = MemberList.Member.newBuilder().setNodeId(nodeId);
		memberList.setHeartbeatCounter(heartbeatCounter);
		memberList.setLocalTime(localTime);
	    
	    return memberList.build();
	 }
	

	private void AddToFile(String fileName, String nodeId, int heartbeatCounter, int localTime)
	{
		MemberList.Builder memberList = MemberList.newBuilder();

	    // Read the existing address book.
	    try {
	      memberList.mergeFrom(new FileInputStream(fileName));
	    } catch (FileNotFoundException e) {
	      System.out.println(fileName + ": File not found.  Creating a new file.");
	    }

	    // Add an address.
	    memberList.addMemberList(AddMember(nodeId, heartbeatCounter, localTime));

	    // Write the new address book back to disk.
	    FileOutputStream output = new FileOutputStream(fileName);
	    memberList.build().writeTo(output);
	    output.close();
	}
	

	
	public ByteBuffer GetMemeberList() {
		//return m_oMembershipList.getByteBuffer();
		return null;
	}
	
	/*int MergeList(MembershipList incomingList)
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
	*/
	void DetectFailure(String nodeId) // will be called from thread as of now.
	{
		//Keep checking for Tcleanup = 2*Tfail secs and then do a detect.
		
	}

	
	
	
	
	int MergeList(byte[] list_)
	{
		return Commons.SUCCESS;
	}
	
	
}
