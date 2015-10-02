package edu.uiuc.cs425;

public class MembershipListStruct {

	String 			m_nIP;
	int 			m_nHeatbeatCounter;
	long 			m_nLocalTime;
	int 			m_nSerialNumber;
	boolean 		m_bSuspect;
	
	public MembershipListStruct(String IP, int heartbeatCounter, long localTime, int serialNumber)
	{
		m_nIP = IP;
		m_nHeatbeatCounter = heartbeatCounter;
		m_nLocalTime = localTime;
		m_nSerialNumber = serialNumber;
		m_bSuspect = false;
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
	
	public int GetSerialNumber()
	{
		return m_nSerialNumber;
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
		return m_nIP;
	}
	
}
