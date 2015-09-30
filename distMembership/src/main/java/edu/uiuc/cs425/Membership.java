package edu.uiuc.cs425;

import java.nio.ByteBuffer;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import edu.uiuc.cs425.MembershipList.Member;
import edu.uiuc.cs425.MembershipList.MemberList;

public class Membership implements Runnable{
	
	private List<MembershipListStruct> 	    m_oMembershipListStructArray;
	private int 							m_nTfail;
	private Thread	 						m_oSuspectedNodeThread;
	
	public void InitializeMemberList()
	{
		m_oMembershipListStructArray = new ArrayList<MembershipListStruct>();
	}
	
	public void AddMemberToStruct(String nodeId, int heartbeatCounter, int localTime)
	{
		m_oMembershipListStructArray.add(new MembershipListStruct(nodeId, heartbeatCounter, localTime));
	}
	
	public MembershipList CreateObject()
	{	 
		MemberList.Builder memberListBuilder  = MemberList.newBuilder();
		List<Member.Buider> memberBuilderList = new ArrayList<Member.Builder>();
		for(int i=0; i< m_oMembershipListStructArray.size(); i++)
		{
			Member.Builder member = Member.newBuilder();
			member.setNodeId(m_oMembershipListStructArray.get(i).GetNodeId());
			member.setHeartbeatCounter(m_oMembershipListStructArray.get(i).GetHeartbeatCounter());
			member.setLocalTime(m_oMembershipListStructArray.get(i).GetLocalTime());
			memberBuilderList.add(member.build);
		}
		memberListBuilder.addAll(memberBuilderList);
		return memberListBuilder.build();
	}
		
	public byte [] GetMemeberList() 
	{	
		return objectToByteBuffer(CreateObject());
	}
	
	public void ReceiveMembershipList(byte [] incomingListBuffer)
	{
		MembershipList incomingList = ObjectFromByteBuffer(incomingListBuffer);
	}
	
	int MergeList(MembershipList incomingList)
	{
		//MemberList memberList = MemberList.parseFrom(new FileInputStream(fileName));
		for(Member member : incomingList.getMemberList())
		{ 
			boolean found = false;
			for(int i=0; i< m_oMembershipListStructArray.size(); i++)
			{
				if(member.getNodeId() == m_oMembershipListStructArray.get(i).GetNodeId())
				{
					found = true;
					MembershipListStruct matchedMember = m_oMembershipListStructArray.get(i);
					if(member.getHeartbeatCounter() == matchedMember.GetHeartbeatCounter())
					{
						if(matchedMember.GetLocalTime() > m_nTfail)
						{
							DetectFailure(matchedMember.GetNodeId());
						}
					}
					else
					{
						matchedMember.ResetHeartbeatCounter(member.getHeatbeatCounter());
					}
				}
				break;
			}
			if (found == false)
			{
				String nodeId = member.getNodeId();
				int heartbeatCounter = member.getHeartbeatCounter();
				int localTime = GetLocalTime(); //Our machine localTime
				AddMemberToStruct(nodeId, heartbeatCounter, localTime);
			}
			
		}	
	}

	public long GetLocalTime()
	{
		return new Date().getTime();
	}
	
	public byte[] ObjectToByteBuffer(Object o) throws Exception 
	{
		return o.toByteArray();
	}
	
	public Object ObjectFromByteBuffer(byte[] buffer) throws Exception 
	{
		return MembershipList,parseFrom(buffer);   //Need to make sure the message is the currect return type
	 }
	
	//WHAT IS THIS FUNCTION DOING??
	private boolean sendMembershipList()
	{
		return ObjectToByteBuffer(/*Object!*/);	
	}
	
	void DetectFailure(String nodeId) // will be called from thread as of now.
	{
		m_oSuspectedNodeThread = new Thread(this);
    	m_oSuspectedNodeThread.start();
	}
	
	void run()
	{
		//Wait and recheck hearbeat counter
		
	}

	
	
}
