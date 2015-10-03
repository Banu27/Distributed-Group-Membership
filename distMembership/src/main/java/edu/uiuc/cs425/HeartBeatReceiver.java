package edu.uiuc.cs425;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class HeartBeatReceiver {
	
	private byte[]  		m_msgBuffer;
	private DatagramSocket  m_socket;
	private DatagramPacket  m_packet;
	private Membership		m_oMembership;
	
	public HeartBeatReceiver() {
		m_msgBuffer = new byte[2048];
	}
	
	public int Initialize(int nPort)
	{
		try
        {
                // DatagramSocket created and listening in 
                m_socket = new DatagramSocket(nPort);
               
                // DatagramPacket for receiving the incoming data from UDP Client
                m_packet = new DatagramPacket(m_msgBuffer, m_msgBuffer.length);

         
        }
        catch (Exception e)
        {
                e.printStackTrace();
                System.out.println("Error while initializing HB server");
                return Commons.FAILURE;
        }
		return Commons.SUCCESS;
	}
	
	// TODO: uncomment the arg
	public void SetMembershipObj(Membership obj)
	{
		m_oMembership = obj;
	}
	
	// Blocking call
	void StartService()
	{
		 while (true)
         {
			 try {
				m_socket.receive(m_packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 byte[] memberShipBlob = Arrays.copyOf(m_msgBuffer, m_packet.getLength());
			 try {
				m_oMembership.MergeList(memberShipBlob);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
             m_packet.setLength(m_msgBuffer.length);
         }
	}

}
