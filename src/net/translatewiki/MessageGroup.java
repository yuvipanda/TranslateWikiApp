package net.translatewiki;

public class MessageGroup {
	
	String groupId, groupName, groupDesc, groupClass , groupExists;
	
	
	public MessageGroup(String id, String name, String desc, String gClass, String exists)
	{
		this.groupId = id;
		this.groupName = name;
		this.groupDesc = desc;
		this.groupClass = gClass;
		this.groupExists = exists;
		
	}
	
	public String getGroupId() {
		return groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getGroupDesc() {
		return groupDesc;
	}

	public String getGroupClass() {
		return groupClass;
	}

	public String getGroupExists() {
		return groupExists;
	}


}
