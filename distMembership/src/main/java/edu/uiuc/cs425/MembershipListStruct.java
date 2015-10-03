package edu.uiuc.cs425;

public class MembershipListStruct {

	String 			m_sUniqueId;
	String 			m_sIP;
	int 			m_nHeatbeatCounter;
	long 			m_nLocalTime;
	boolean 		m_bSuspect;
	
	public MembershipListStruct(String IP, String uniqueId, int heartbeatCounter, long localTime)
	{
		m_sUniqueId = uniqueId;
		m_sIP = IP;
		m_nHeatbeatCounter = heartbeatCounter;
		m_nLocalTime = localTime;
		m_bSuspect = false;
	}
	
	public void Print()
	{
		System.out.println(m_sUniqueId + " " + String.valueOf(m_nHeatbeatCounter) + " " +String.valueOf(m_nLocalTime)
					+ " " + String.valueOf(m_bSuspect));
	}
	
	public void ResetLocalTime(long localTime)
	{
		m_nLocalTime = localTime;
	}
	
	public void ResetHeartbeatCounter(int heartbeatCounter)
	{
		m_nHeatbeatCounter = heartbeatCounter;
	}
	
	public int GetHeartbeatCounter()
	{
		return m_nHeatbeatCounter;
	}
	
	public String GetUniqueId()
	{
		return m_sUniqueId;
	}
	
	public long GetLocalTime()
	{
		return m_nLocalTime;
	}
	
	public boolean IsSuspect()
	{
		return m_bSuspect;
	}
		
	public void setAsSuspect()
	{
		m_bSuspect = true;
	}
	
	public String GetIP()
	{
		return m_sIP;
	}
	
}
