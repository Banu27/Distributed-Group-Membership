package edu.uiuc.cs425;

import java.nio.ByteBuffer;
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
	
	private MembershipList 					m_oMembershipList;
	private int 							m_nTfail;
	private MemberList.Builder 				m_oMembershipListBuilder;
	private Thread	 						m_oSuspectedNodeThread;
	//private ByteBuffer m_oMembershipListBuffer;
	//private String m_sFileName;
	
	public void InitializeMemberList()
	{
		m_oMembershipListBuilder = MemberList.newBuilder();
	}
	
	public void AddMember(String nodeId, int heartbeatCounter, int localTime)
	{
		  
		Member.Builder member = Member.newBuilder();
		member.setNodeId(nodeId);
		member.setHeartbeatCounter(heartbeatCounter);
		member.setLocalTime(localTime);
		m_oMembershipListBuilder.addMemberList(member.build());
		m_oMembershipList.build();
	 }
	

	/*public void UpdateMemberList(String nodeId, int heartbeatCounter, int localTime)
	{
		/*MemberList.Builder memberList = MemberList.newBuilder();

	    // Read the existing address book.
	    try {
	      memberList.mergeFrom(new FileInputStream(fileName));
	    } catch (FileNotFoundException e) {
	      System.out.println(fileName + ": File not found.  Creating a new file.");
	    }

	    // Add a member
	    m_oMembershipListBuilder.addMemberList(AddMember(nodeId, heartbeatCounter, localTime));

	    // Write the new member list back to disk.
	    ///FileOutputStream output = new FileOutputStream(m_sFileName);
	    m_oMembershipList.build();
	    //.writeTo(output);
	    //output.close();
	}*/
	
	public ByteBuffer GetMemeberList() {
		//return m_oMembershipList.getByteBuffer();
		return objectToByteBuffer(m_oMembershipList);
	}
	
	public void ReceiveMembershipList(ByteBuffer incomingListBuffer)
	{
		MembershipList incomingList = objectFromByteBuffer(incomingListBuffer);
	}
	
	int MergeList(MembershipList incomingList)
	{
		//MemberList memberList = MemberList.parseFrom(new FileInputStream(fileName));
		for(Member member : m_oMembershipList.getMemberList())
		{ 
			//How do I search??
			if(member.getHeartbeatCounter() == incomingListMember.getHeartbeatCounter())
			{
				if(member.getLocalTime() > m_nTfail)
				{
					DetectFailure(member.getNodeId());
				}
			}
			else
			{
				member.setHeartbeatCounter(incomingListMember.getHeartbeatCounter());
			}
			
			//Find the same id in our list copy
			//Update heartbeat counter
				//If heartbeat counter hasnt changed, check local timestamp
				//If suspected, call DetectFailure on this id alone
			//Update the timestamp
		}		
		
	}
	
	//public byte[] objectToByteBuffer(Object o) throws Exception {
	public ByteBuffer objectToByteBuffer(Object o) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    Message message = (Message) o;
	    byte[] name = message.getDescriptorForType().getFullName()
	       .getBytes("UTF-8");
	    baos.write(name.length); // TODO: Length as int and not byte
	    
	    // Write the full descriptor name, i.e. protobuf.Person
	    baos.write(name);
	    byte[] messageBytes = message.toByteArray();
	    baos.write(messageBytes.length); // TODO: Length as int and not byte
	    baos.write(messageBytes);
	    return ByteBuffer.wrap(baos.toByteArray());
	}
	
	public Object objectFromByteBuffer(byte[] buffer) throws Exception {
	    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
	    byte[] name = new byte[bais.read()];
	    bais.read(name); // TODO: Read fully??
	    // Get the class name associated with the descriptor name
	    String className = mapping.get(new String(name, "UTF-8"));
	    Class clazz = Thread.currentThread().getContextClassLoader()
	       .loadClass(className);
	    Method parseFromMethod = clazz.getMethod("parseFrom", byte[].class);
	    byte[] message = new byte[bais.read()];
	    bais.read(message); // TODO: Read fully??
	    return parseFromMethod.invoke(null, message);
	 }
	
	private bool sendMembershipList()
	{
		ByteBuffer eventTypeBuffer = ByteBuffer.allocate(1);
		eventTypeBuffer.put(0x1c);
		eventTypeBuffer.flip();
		ByteString eventType = ByteString.copyFrom(eventTypeBuffer);
		System.out.println(eventType.size() + " " + eventTypeBuffer.array().length);

		Header.Builder mh = Header.newBuilder();
		mh.setEventType(eventType);
	}
	
	void DetectFailure(String nodeId) // will be called from thread as of now.
	{
		m_oSuspectedNodeThread = new Thread(this);
    	m_oSuspectedNodeThread.start();
	}
	
	void run()
	{
		
	}

	
	
}
