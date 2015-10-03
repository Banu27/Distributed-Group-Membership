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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.ByteArrayOutputStream;

import edu.uiuc.cs425.MembershipList.Member;
import edu.uiuc.cs425.MembershipList.MemberList;

public class Membership implements Runnable{
	
	private HashMap<String,MembershipListStruct> 		m_oHmap;
	private long  										m_nTfail;
	private String 										m_sIP;
	private String 										m_sUniqueId;
	private int 										m_nMyHeartBeat;
	private ReentrantReadWriteLock 						m_oReadWriteLock; 
	private Lock 										m_oLockR; 
	private Lock 										m_oLockW; 
	
	public String UniqueId()
	{
		return m_sUniqueId;
	}
	
	public int Initialize(int tFail)
	{
		m_oHmap 		= new HashMap<String, MembershipListStruct>();
		m_nMyHeartBeat  = 0;
		m_oReadWriteLock = new ReentrantReadWriteLock();
		m_oLockR = m_oReadWriteLock.readLock();
		m_oLockW = m_oReadWriteLock.writeLock();
		m_nTfail = tFail;
		
		try {
			m_sIP  = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return Commons.FAILURE;
		}
		return Commons.SUCCESS;
	}
	
	public void AddSelf()
	{
		//Write lock. Add self called from controller
		//m_nSerialNumber = serialNumber;
		m_sUniqueId = m_sIP + String.valueOf(GetMyLocalTime());
		m_oLockW.lock();
		AddMemberToStruct( m_sUniqueId, m_sIP, m_nMyHeartBeat, GetMyLocalTime());
		m_oLockW.unlock();
	}
	
	public void AddMemberToStruct(String uniqueId, String IP, int heartbeatCounter, long localTime)
	{
		//No write lock. Write lock present in Merge.
		MembershipListStruct newMember = new MembershipListStruct(uniqueId, IP, heartbeatCounter, localTime);
		m_oHmap.put(uniqueId,newMember);
	}
	
	public void IncrementHeartbeat()
	{
		m_nMyHeartBeat = m_nMyHeartBeat + 1;
		m_oHmap.get(m_sUniqueId).ResetHeartbeatCounter(m_nMyHeartBeat);
		m_oHmap.get(m_sUniqueId).ResetLocalTime(GetMyLocalTime());
	}
	
	
	public MemberList CreateObject()
	{	 
		//Read lock
		m_oLockR.lock();
		MemberList.Builder memberListBuilder  = MemberList.newBuilder();
		List<Member> memberList = new ArrayList<Member>();
		Set<Entry<String, MembershipListStruct>> set = m_oHmap.entrySet();
	    Iterator<Entry<String, MembershipListStruct>> iterator = set.iterator();
	    while(iterator.hasNext()) {
	         Map.Entry mentry = (Map.Entry)iterator.next();
	         MembershipListStruct memberStruct = m_oHmap.get(mentry.getKey());
	         Member.Builder member = Member.newBuilder();
	         member.setHeartbeatCounter(memberStruct.GetHeartbeatCounter());
	         member.setIP(memberStruct.GetIP());
	         member.setHasLeft(memberStruct.HasLeft());
	         member.setLocalTime(memberStruct.GetLocalTime());
	         member.setUniqueId(memberStruct.GetUniqueId());
	         memberList.add(member.build());
	    }	      
		memberListBuilder.addAllMember(memberList);
		m_oLockR.unlock();
		return memberListBuilder.build();
		
	}
		
	public byte [] GetMemberList() throws Exception 
	{	
		return ObjectToByteBuffer(CreateObject());
	}
	
	public int MergeList(byte [] incomingListBuffer) throws Exception
	{
		MemberList incomingList = ObjectFromByteBuffer(incomingListBuffer);
		
		//Write lock 
		m_oLockW.lock();
		for(Member member : incomingList.getMemberList())
		{ 
			if(m_oHmap.containsKey(member.getUniqueId()))
			{
				MembershipListStruct matchedMember = m_oHmap.get(member.getUniqueId());
				//> Can never happen for self
				if(member.getHasLeft())
				{
					matchedMember.setAsLeft();
					matchedMember.ResetLocalTime(GetMyLocalTime());
				}
				if(!matchedMember.HasLeft() 
						&& member.getHeartbeatCounter() > matchedMember.GetHeartbeatCounter())
				{
					matchedMember.ResetHeartbeatCounter(member.getHeartbeatCounter());
					matchedMember.ResetLocalTime(GetMyLocalTime());
				}
			}
			else
			{
				//Unseen member
				if(!member.getHasLeft())
				{	
					System.out.println("Adding node to memberlist " + member.getIP() );
					String IP = member.getIP();
					int heartbeatCounter = member.getHeartbeatCounter();
					long localTime = GetMyLocalTime(); //Our machine localTime
					String uniqueId = member.getUniqueId();
					AddMemberToStruct(uniqueId, IP, heartbeatCounter, localTime);
				}
			}
		}
		m_oLockW.unlock();
		PrintList();
		return Commons.SUCCESS;
	}
	
	public void PrintList() // only reading the list
	{
		ArrayList<String> vMembers = GetMemberIds();
		System.out.println("=============================");
		for(int i=0; i<vMembers.size(); ++i)
		{
			if(!m_oHmap.get(vMembers.get(i)).HasLeft())
				m_oHmap.get(vMembers.get(i)).Print();
		}
		System.out.println("=============================");
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
		return MemberList.parseFrom(buffer);   //Need to make sure the message is the correct return type
	 }
	
	public String GetIP(String uniqueId)
	{
		return m_oHmap.get(uniqueId).GetIP();
	}
	
	public void run()
	{
		//Sleep time modify
		while(true) {
			
			long start_time = System.nanoTime();
			Set<Entry<String, MembershipListStruct>> set = m_oHmap.entrySet();
			Iterator<Entry<String, MembershipListStruct>> iterator = set.iterator();
			//No need for write lock. No other thread or function can set suspect. 
			//Read lock imposed due to local time read
			m_oLockR.lock();
			while(iterator.hasNext()) {
				Map.Entry mentry = (Map.Entry)iterator.next();
				MembershipListStruct memberStruct = m_oHmap.get(mentry.getKey());
				if(!memberStruct.GetUniqueId().equals(m_sUniqueId));
				{
					if((memberStruct.IsSuspect() || memberStruct.HasLeft()) 
							&& (memberStruct.GetLocalTime() - GetMyLocalTime() > 2*m_nTfail))
					{
						m_oHmap.remove(mentry.getKey());
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
			m_oLockR.unlock();
			long diff = (System.nanoTime() - start_time)/1000000;
			try {
				Thread.sleep(m_nTfail - diff);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
		}
	}

	public int TimeToLeave()
	{
		m_oHmap.get(m_sUniqueId).setAsLeft();
		return Commons.SUCCESS;
	}
	
	//Read lock
	 public ArrayList<String> GetMemberIds() 
	 {
		 m_oLockR.lock();
		 ArrayList<String> keyList = new ArrayList<String>(m_oHmap.keySet());
		 m_oLockR.unlock();
		 return keyList;
	 }
}
