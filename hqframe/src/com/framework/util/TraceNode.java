package com.framework.util;

import java.util.ArrayList;

import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

public class TraceNode {
	private String nodeId;
	private String className;
	private String methodName;
	private long startTime;
	private long endTime;
	private long cost;
	private TraceNode parentNode;
	private ArrayList<TraceNode> children = new ArrayList();

	public TraceNode(String className, String methodName) {
		UUIDGenerator generator = UUIDGenerator.getInstance();
		UUID uuid = generator.generateRandomBasedUUID();
		String randomString = uuid.toString();
		randomString = randomString.replaceAll("-", "_");
		this.nodeId = randomString;
		this.className = className;
		this.methodName = methodName;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeId() {
		return this.nodeId;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return this.className;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void addChild(TraceNode node) {
		this.children.add(node);
		node.setParentNode(this);
	}

	public ArrayList<TraceNode> getChildren() {
		return this.children;
	}

	public void setCost(long cost) {
		this.cost = cost;
	}

	public long getCost() {
		return this.cost;
	}

	public void setParentNode(TraceNode parentNode) {
		this.parentNode = parentNode;
	}

	public TraceNode getParentNode() {
		return this.parentNode;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getEndTime() {
		return this.endTime;
	}
}