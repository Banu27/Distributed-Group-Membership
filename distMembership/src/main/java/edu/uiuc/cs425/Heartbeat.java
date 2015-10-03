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
	private int 					m_nHBCount;
	
	public int Initialize(Membership oMem, ConfigAccessor oConfig)
	{
		m_oMship = oMem;
		m_oConfig = oConfig;
		m_nHBCount = 0;
		m_nGossipNodes			= oConfig.GossipNodes();
		m_nGossipInterval		= oConfig.HeartBeatInterval();
		m_nHBSendPort			= oConfig.HeartBeatPort();
		System.out.println("Initialized HeartBeat: GossipNodes=" + String.valueOf(m_nGossipNodes)
					+ " GossipInterval=" + String.valueOf(m_nGossipInterval) +
					" m_nHBSendPort=" + String.valueOf(m_nHBSendPort));
		
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
			ArrayList<String> vUniqueIds = m_oMship.GetMemberIds();
			int size = vUniqueIds.size();
			int currGossip = m_nGossipNodes;
			if(size < m_nGossipNodes) currGossip = size;
					
			Set<Integer> rands = Commons.RandomK(currGossip,size);
			// hack. always ask for k+ 1 and remove self or someother node
			System.out.println("Heartbeat count: " + String.valueOf(++m_nHBCount));
			for (Integer i : rands)
			{
				String ip = m_oMship.GetIP(vUniqueIds.get(i));
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
			long diff = (System.nanoTime() - start_time)/1000000;
			try {
				Thread.sleep(m_nGossipInterval - diff);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
	
	
	public void run() {
		SendHBs();
	}

}
