package com.framework.util;


public class CallStackTracer
{
  private static ThreadLocal<TraceNode> rootNode = new ThreadLocal();
  private static ThreadLocal<TraceNode> currentNode = new ThreadLocal();

  private static void setRootNode(TraceNode node) {
    rootNode.set(node);
  }

  private static TraceNode getRootNode() {
    return ((TraceNode)rootNode.get());
  }

  private static void removeRootNode() {
    rootNode.remove();
  }

  private static void setCurrentNode(TraceNode node) {
    currentNode.set(node);
  }

  private static TraceNode getCurrentNode() {
    return ((TraceNode)currentNode.get());
  }

  private static void removeCurrentNode() {
    currentNode.remove();
  }

  private static TraceNode addNode(String className, String methodName) {
    TraceNode traceNode = new TraceNode(className, methodName);
    traceNode.setStartTime(System.currentTimeMillis());

    if (getRootNode() == null)
      setRootNode(traceNode);
    else {
      getCurrentNode().addChild(traceNode);
    }

    return traceNode;
  }

  public static void startNode(String className, String methodName) {
  }

  public static void endNode() {
  }

  public static void removeThreadLocalNode() {
    try {
      removeCurrentNode();
      removeRootNode();
    } catch (Exception localException) {
    }
  }

  private static DataObject getNodeDataObject(TraceNode node) {
    DataObject nodeObject = new DataObject();
    try {
      nodeObject.put("nodeId", node.getNodeId());
      nodeObject.put("className", node.getClassName());
      nodeObject.put("methodName", node.getMethodName());
      if (node.getParentNode() != null)
        nodeObject.put("parentNodeId", node.getParentNode().getNodeId());
      else {
        nodeObject.put("parentNodeId", "");
      }
      nodeObject.put("cost", node.getCost());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return nodeObject;
  }

  private static void recursiveTraceNode(TraceNode node, DataStore resultDs)
  {
    DataObject vdo = null;
    TraceNode childNode = null;
    if (node != null) {
      vdo = new DataObject();
      vdo = getNodeDataObject(node);
      resultDs.addRow(vdo);

      for (int i = 0; i < node.getChildren().size(); ++i) {
        childNode = (TraceNode)node.getChildren().get(i);
        recursiveTraceNode(childNode, resultDs);
      }
    }
  }

  public static String getCallStackJson() {
    String resultStr = "";
    try {
    } catch (Exception e) {
      return "";
    }
    return resultStr;
  }
}