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
	
	private HashMap<Integer,MembershipListStruct> 		m_oHmap;
	private long  										m_nTfail;
	private Thread	 									m_oSuspectedNodeThread;
	private int 										m_nSerialNumber;
	private String 										m_nIP;
	private int 										m_nMyHeartBeat;
	private ReentrantReadWriteLock 				m_oReadWriteLock; 
	private Lock 									m_oLockR; 
	private Lock 									m_oLockW; 
	
	
	public void Initialize()
	{
		m_oHmap 		= new HashMap<Integer, MembershipListStruct>();
		m_nMyHeartBeat  = 1;
		m_oReadWriteLock = new ReentrantReadWriteLock();
		m_oLockR = m_oReadWriteLock.readLock();
		m_oLockW = m_oReadWriteLock.writeLock();
		
		try {
			m_nIP  = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void AddSelf(int serialNumber)
	{
		//Write lock. Add self called from controlller
		m_oLockW.lock();
		m_nSerialNumber = serialNumber;
		AddMemberToStruct(m_nIP, m_nMyHeartBeat, GetMyLocalTime(), m_nSerialNumber);
		m_oLockW.unlock();
	}
	
	public void AddMemberToStruct(String IP, int heartbeatCounter, long localTime, int serialNumber)
	{
		//No write lock. Write lock present in Merge.
		MembershipListStruct newMember = new MembershipListStruct(IP, heartbeatCounter, localTime, serialNumber);
		m_oHmap.put(serialNumber,newMember);
	}
	
	public void IncrementHeartbeat()
	{
		m_nMyHeartBeat = m_nMyHeartBeat + 1;
	}
	
	public MemberList CreateObject()
	{	 
		//Read lock
		m_oLockR.lock();
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
			boolean found = false;
			if(m_oHmap.containsKey(member.getSerialNumber()))
			{
				MembershipListStruct matchedMember = m_oHmap.get(member.getSerialNumber());
				if(member.getHeartbeatCounter() == matchedMember.GetHeartbeatCounter())
				{
					if(member.getSerialNumber() == matchedMember.GetSerialNumber())
							matchedMember.ResetLocalTime(GetMyLocalTime());
				}
				else
				{
					matchedMember.ResetHeartbeatCounter(member.getHeartbeatCounter());
					matchedMember.ResetLocalTime(GetMyLocalTime());
				}
			}
			else
			{
				//Unseen member
				System.out.println("Adding node to memberlist " + member.getIP() );
				String IP = member.getIP();
				int heartbeatCounter = member.getHeartbeatCounter();
				long localTime = GetMyLocalTime(); //Our machine localTime
				int serialNumber = member.getSerialNumber();
				AddMemberToStruct(IP, heartbeatCounter, localTime, serialNumber);
			}
		}
		m_oLockW.unlock();
		PrintList();
		return Commons.SUCCESS;
	}
	
	public void PrintList() // only reading the list
	{
		ArrayList<Integer> vMembers = GetMemberIds();
		System.out.println("=============================");
		for(int i=0; i<vMembers.size(); ++i)
		{
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
		return MemberList.parseFrom(buffer);   //Need to make sure the message is the currect return type
	 }
	
	// The failure detection thread is called from the controller
	/*public void DetectFailure() // will be called from thread as of now.
	{
		m_oSuspectedNodeThread = new Thread(this);
    	m_oSuspectedNodeThread.start();
	}*/
	
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
			//No need for write lock. No other thread or function can set suspect. 
			//Read lock imposed due to local time read
			m_oLockR.lock();
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
			m_oLockR.unlock();
		}
	}

	//Read lock
	 public ArrayList<Integer> GetMemberIds() 
	 {
		 return new ArrayList<Integer>(m_oHmap.keySet());
	 }
}
