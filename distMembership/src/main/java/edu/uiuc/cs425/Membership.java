package edu.uiuc.cs425;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
	
	private List<MembershipListStruct> 	    			m_oMembershipListStructArray;
	private HashMap<Integer,MembershipListStruct> 		m_oHmap;
	private long  										m_nTfail;
	private Thread	 									m_oSuspectedNodeThread;
	private int 										m_nSerialNumber;
	private String 										m_nIP;
	private int 										m_nMyHeartBeat;
	
	public void Initialize()
	{
		m_oHmap = new HashMap<Integer, MembershipListStruct>();
		m_nMyHeartBeat = 1;
		m_oMembershipListStructArray = new ArrayList<MembershipListStruct>();
	}
	
	public void AddSelf(int serialNumber)
	{
		m_nSerialNumber = serialNumber;
		AddMemberToStruct(m_nIP, m_nMyHeartBeat, GetMyLocalTime(), m_nSerialNumber);
	}
	
	public void AddMemberToStruct(String IP, int heartbeatCounter, long localTime, int serialNumber)
	{
		//m_oMembershipListStructArray.add(new MembershipListStruct(nodeId, heartbeatCounter, localTime, serialNumber));
		MembershipListStruct newMember = new MembershipListStruct(IP, heartbeatCounter, localTime, serialNumber);
		m_oHmap.put(serialNumber,newMember);
	}
	
	public void IncrementHeartbeat()
	{
		m_nMyHeartBeat = m_nMyHeartBeat + 1;
	}
	
	public MemberList CreateObject()
	{	 
		MemberList.Builder memberListBuilder  = MemberList.newBuilder();
		List<Member> memberList = new ArrayList<Member>();
		Set<Entry<Integer, MembershipListStruct>> set = m_oHmap.entrySet();
	    Iterator<Entry<Integer, MembershipListStruct>> iterator = set.iterator();
	    while(iterator.hasNext()) {
	         Map.Entry mentry = (Map.Entry)iterator.next();
	         MembershipListStruct memberStruct = m_oHmap.get(mentry.getKey());
	         Member.Builder member = Member.newBuilder();
	         member.setHeartbeatCounter(memberStruct.GetHeartbeatCounter());
	         member.setIP(memberStruct.GetIP());
	         member.setLocalTime(memberStruct.GetLocalTime());
	         member.setSerialNumber(memberStruct.GetSerialNumber());
	         memberList.add(member.build());
	    }	      
		memberListBuilder.addAllMember(memberList);
		return memberListBuilder.build();
	}
		
	public byte [] GetMemberList() throws Exception 
	{	
		return ObjectToByteBuffer(CreateObject());
	}
	
	// TODO: Is this method needed?
	/*public void ReceiveMembershipList(byte [] incomingListBuffer)
	{
		MembershipList incomingList = ObjectFromByteBuffer(incomingListBuffer);
	}*/
	
	public int MergeList(byte [] incomingListBuffer) throws Exception
	{
		MemberList incomingList = ObjectFromByteBuffer(incomingListBuffer);
		for(Member member : incomingList.getMemberList())
		{ 
			boolean found = false;
			if(m_oHmap.containsKey(member.getSerialNumber()))
			{
				MembershipListStruct matchedMember = m_oHmap.get(member.getSerialNumber());
				if(member.getHeartbeatCounter() == matchedMember.GetHeartbeatCounter())
				{
					if(matchedMember.GetLocalTime() > m_nTfail)
					{
						DetectFailure(matchedMember.GetIP());
					}
				}
				else
				{
					matchedMember.ResetHeartbeatCounter(member.getHeartbeatCounter());
				}
			}
			else
			{
				String IP = member.getIP();
				int heartbeatCounter = member.getHeartbeatCounter();
				long localTime = GetMyLocalTime(); //Our machine localTime
				int serialNumber = member.getSerialNumber();
				AddMemberToStruct(IP, heartbeatCounter, localTime, serialNumber);
			}
		}
			
		return Commons.SUCCESS;
	}

	public long GetMyLocalTime()
	{
		return new Date().getTime();
	}
	
	public byte[] ObjectToByteBuffer(MemberList membershipList) throws Exception 
	{
		return membershipList.toByteArray();
	}
	
	public MemberList ObjectFromByteBuffer(byte[] buffer) throws Exception 
	{
		return MemberList.parseFrom(buffer);   //Need to make sure the message is the currect return type
	 }
	
	public void DetectFailure(String nodeId) // will be called from thread as of now.
	{
		m_oSuspectedNodeThread = new Thread(this);
    	m_oSuspectedNodeThread.start();
	}
	
	public String GetIP(int serialNumber)
	{
		return m_oHmap.get(serialNumber).GetIP();
	}
	
	public void run()
	{
		while(true) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				
				e.printStackTrace();
			}
			Set<Entry<Integer, MembershipListStruct>> set = m_oHmap.entrySet();
			Iterator<Entry<Integer, MembershipListStruct>> iterator = set.iterator();
			while(iterator.hasNext()) {
				Map.Entry mentry = (Map.Entry)iterator.next();
				MembershipListStruct memberStruct = m_oHmap.get(mentry.getKey());
				if(memberStruct.IsSuspect())
				{
					if(memberStruct.GetLocalTime() - GetMyLocalTime() > 2*m_nTfail)
					{
						m_oHmap.remove(mentry.getKey());
					}
				}
				else
				{
					if(memberStruct.GetLocalTime() - GetMyLocalTime() > m_nTfail)
					{
						memberStruct.setAsSuspect();
					}
				}
			}
		}
	}

	 public ArrayList<Integer> GetMemberIds() 
	 {
		 return new ArrayList<Integer>(m_oHmap.keySet());
	 }
}
