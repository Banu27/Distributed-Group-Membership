package edu.uiuc.cs425;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

public class Heartbeat implements Runnable {
	private Membership 				m_oMship;
	private ConfigAccessor			m_oConfig;
	private	int						m_nGossipNodes;
	private int 					m_nGossipInterval;
	private int 					m_nHBSendPort;
	
	public int Initialize(Membership oMem, ConfigAccessor oConfig)
	{
		m_oMship = oMem;
		m_oConfig = oConfig;
		m_nGossipNodes			= oConfig.GossipNodes();
		m_nGossipInterval		= oConfig.HeartBeatInterval();
		m_nHBSendPort			= oConfig.HeartBeatPort();
		return Commons.SUCCESS;
	}
	
	private void SendHBs()
	{
		try {
			Thread.sleep(m_nGossipInterval);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		while(true)
		{
			long start_time = System.nanoTime();
			m_oMship.IncrementHeartbeat();
			ArrayList<Integer> vSerialNumbers = m_oMship.GetMemberIds();
			int size = vSerialNumbers.size();
			Set<Integer> rands = Commons.RandomK(m_nGossipNodes,size);
			
			for (Integer i : rands)
			{
				String ip = m_oMship.GetIP(i);
				HeartBeatProxy proxy = new HeartBeatProxy();
				proxy.Initialize(ip,m_nHBSendPort);
				try {
					proxy.SendMembershipList(m_oMship.GetMemberList());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				} 
			}
			double diff = (System.nanoTime() - start_time)/1e6;
			try {
				Thread.sleep(m_nGossipInterval - (long)diff);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
	
	
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
