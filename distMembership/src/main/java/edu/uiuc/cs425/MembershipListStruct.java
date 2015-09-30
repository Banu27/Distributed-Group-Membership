package edu.uiuc.cs425;

public class MembershipListStruct {

	String 			m_sNodeId;
	int 			m_nHeatbeatCounter;
	int 			m_nLocalTime;
	
	public MembershipListStruct(String nodeId, int heartbeatCounter, int localTime)
	{
		m_sNodeId = nodeId;
		m_nHeatbeatCounter = heartbeatCounter;
		m_nLocalTime = localTime;
	}
	
	public void ResetLocalTime(int localTime)
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
	
	public int GetLocalTime()
	{
		return m_nLocalTime;
	}
	
	public String GetNodeId()
	{
		return m_sNodeId;
	}
	
}
