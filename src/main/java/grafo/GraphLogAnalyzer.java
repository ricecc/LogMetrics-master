package grafo;

import grafo.controller.TraceController;
import grafo.io.IO_Handler;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.images.Resolutions;

import java.io.IOException;
import java.util.*;

/**
 * Class that analyze the XES file, creating a Graph that represents it, using a set of Nodes(Activities) and Edges(Transitions)
 *
 * @author luigi.bucchicchioAtgmail.com
 */
public class GraphLogAnalyzer {
    private static int graphNumber = 0;
    private List<Trace> traceSet = new ArrayList<Trace>();
    private Graph graph;
    boolean firstTime = true;
    private Set<String> nodeIdSuperSet = new TreeSet<String>();
    private Set<String> edgeIdSuperSet = new TreeSet<String>();

    private Set<String> nodeIdSet;
    private Set<String> edgeIdSet;

    private String lastNodeId = null;
    private long startingTime;
    private long finishTime;
    private char delimiter = 't';

    public GraphLogAnalyzer() {
        graphNumber++;
        graph = new MultiGraph("RepeatingGraph" + graphNumber);
    }

    public List<Trace> getTraceSet() {
        return traceSet;
    }

    public void setTraceSet(List<Trace> traceSet) {
        this.traceSet = traceSet;
    }

    public String getLastNodeId() {
        return lastNodeId;
    }

    public void setLastNodeId(String lastNodeId) {
        this.lastNodeId = lastNodeId;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public long getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(long startingTime) {
        this.startingTime = startingTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public Graph getGraph() {
        return graph;
    }


    public void LogAnalyze() {


        this.startingTime = System.currentTimeMillis();

        // int grams = Integer.parseInt(IO_Handler.requireInput("Please insert grams: "));

        for (Trace value : traceSet) {

            nodeIdSet = new TreeSet<>();
            edgeIdSet = new TreeSet<>();

            //traceLineAnalyze(trace.getTraceId(),trace.getTraceLine());
            traceListAnalyze(value.getTraceId(), value.getActivitySequence());
            //System.out.println("ID of trace: " + value.getTraceId());
            //System.out.println("Trace: " + value.getTraceLine());

        }

        this.finishTime = System.currentTimeMillis() - this.startingTime;

    }


    // TODO: REFACTORING
    private void traceListAnalyze(String traceId, List<String> trace) {
        Iterator<String> it = trace.iterator();
        firstTime = true;
        lastNodeId = null;
        while (it.hasNext()) {
            String activity = it.next();

            while (activity.equals("") && it.hasNext()) {
                activity = it.next();
            }
            if (activity.equals(""))
                break;

            Node n = null;
            String edgeLabel = null;

            if (firstTime) {

                if (!nodeIdSuperSet.contains(activity)) {

                    n = graph.addNode(activity);
                    n.setAttribute("ui.label", n.getId());
                    nodeIdSuperSet.add(n.getId());
                    nodeIdSet.add(n.getId());
                    firstTime = false;
                }
                if (!nodeIdSet.contains(activity))
                    nodeIdSet.add(activity);
                firstTime = false;
            } else {

                if (!nodeIdSuperSet.contains(activity)) {
                    //new Node
                    n = graph.addNode(activity);
                    n.setAttribute("ui.label", n.getId());
                    nodeIdSuperSet.add(n.getId());
                    nodeIdSet.add(n.getId());

                    // so new Edge

                    edgeLabel = lastNodeId + activity;
                    Edge e = graph.addEdge(edgeLabel, lastNodeId, activity, true);
                    List<TraceRepeatingEdgeInfo> edgeSetInfo = new ArrayList<TraceRepeatingEdgeInfo>();

                    edgeSetInfo.add(new TraceRepeatingEdgeInfo(traceId, 0));
                    e.setAttribute("info", edgeSetInfo);

                    edgeIdSuperSet.add(edgeLabel);
                    edgeIdSet.add(edgeLabel);

                } else {
                    // already contains that node in the superSet

                    if (nodeIdSet.contains(activity)) {
                        n = graph.getNode(activity);
                        n.setAttribute("ui.label", "R_" + n.getId());
                    } else {
                        nodeIdSet.add(activity);
                    }

                    edgeLabel = lastNodeId + activity;
                    // new edge?
                    if (!edgeIdSuperSet.contains(edgeLabel)) {
                        //new Edge
                        if (lastNodeId.equals(activity)) {
                            //self repeating
                            Edge e = graph.addEdge(edgeLabel, lastNodeId, activity, true);
                            e.setAttribute("ui.label", "R");
                            List<TraceRepeatingEdgeInfo> edgeSet = new ArrayList<TraceRepeatingEdgeInfo>();
                            edgeSet.add(new TraceRepeatingEdgeInfo(traceId, 1));
                            e.setAttribute("info", edgeSet);
                            edgeIdSuperSet.add(edgeLabel);
                            edgeIdSet.add(edgeLabel);
                        } else {
                            //normal edge
                            Edge e = graph.addEdge(edgeLabel, lastNodeId, activity, true);
                            List<TraceRepeatingEdgeInfo> edgeSetInfo = new ArrayList<TraceRepeatingEdgeInfo>();

                            edgeSetInfo.add(new TraceRepeatingEdgeInfo(traceId, 0));
                            e.setAttribute("info", edgeSetInfo);

                            edgeIdSuperSet.add(edgeLabel);
                            edgeIdSet.add(edgeLabel);
                        }
                    } else {
                        // already contains that Edge in the superSet
                        // but maybe is new in the Set

                        if (!edgeIdSet.contains(edgeLabel)) {

                            edgeIdSet.add(edgeLabel);

                            Edge e = graph.getEdge(edgeLabel);
                            TraceRepeatingEdgeInfo traceEdgeInfo;
                            traceEdgeInfo = new TraceRepeatingEdgeInfo(traceId, 0);

                            @SuppressWarnings("unchecked")
                            List<TraceRepeatingEdgeInfo> list = (List<TraceRepeatingEdgeInfo>) e.getAttribute("info");
                            if (list.contains(traceEdgeInfo)) {
                                list.get(list.lastIndexOf(traceEdgeInfo)).repeating();
                            } else {
                                list.add(traceEdgeInfo);
                            }

                        } else {

                            Edge e = graph.getEdge(edgeLabel);

                            if (e.getAttribute("ui.label") == null)
                                e.setAttribute("ui.label", "R");

                            TraceRepeatingEdgeInfo traceEdgeInfo;

                            traceEdgeInfo = new TraceRepeatingEdgeInfo(traceId, 1);

                            @SuppressWarnings("unchecked")
                            List<TraceRepeatingEdgeInfo> list = (List<TraceRepeatingEdgeInfo>) e.getAttribute("info");

                            if (list.contains(traceEdgeInfo)) {
                                list.get(list.lastIndexOf(traceEdgeInfo)).repeating();
                            } else {
                                list.add(traceEdgeInfo);
                            }

                        }
                    }
                }

            }
            lastNodeId = activity;
        }
    }


//	private void traceLineAnalyze(String traceId, String trace) {
//		firstTime= true;
//		lastNodeId = null;
//		StringBuffer check=new StringBuffer("");
//		int checkpoint=0;
//
//		for(int i=0;i<trace.length();i++) {
//			i=checkpoint;
//
//			//leggi se non si sfora
//			if((i+1)<trace.length()) {
//
//				//scorri e leggi tanti task quanti il grado
//
//				//
//				//t11t54t63t753t63t432
//				int tcount=0;
//				int j;
//				for(j=i;j<trace.length();j++) {
//
//					if(trace.charAt(j)==delimiter) { 
//						if(tcount==1)
//							checkpoint=j;
//						tcount++;
//						if(tcount>1)
//							break;
//					}
//					check.append(trace.charAt(j));
//				}
//				//to exit
//				i=j-1;
//
//				// old implement.
//				//			if((i+1)<trace.length()) {
//				//			check.append(trace.charAt(i));
//
//				if(check.length()!=0) {
//
//					Node n=null;
//					String edgeLabel=null;
//
//					if(firstTime) {
//
//						if(!nodeIdSuperSet.contains(check.toString())) {
//
//							n=graph.addNode(check.toString());
//							n.setAttribute("ui.label", n.getId());
//							nodeIdSuperSet.add(n.getId());
//							nodeIdSet.add(n.getId());
//							firstTime=false;
//						}
//						firstTime=false;
//					}else {
//
//						if(!nodeIdSuperSet.contains(check.toString())) {
//							//new Node
//							n=graph.addNode(check.toString());
//							n.setAttribute("ui.label", n.getId());
//							nodeIdSuperSet.add(n.getId());
//							nodeIdSet.add(n.getId());
//
//							// so new Edge
//
//							edgeLabel=lastNodeId+check.toString();
//							Edge e=graph.addEdge(edgeLabel, lastNodeId, check.toString(),true);
//							List<TraceRepeatingEdgeInfo> edgeSetInfo = new ArrayList<TraceRepeatingEdgeInfo>();
//
//							edgeSetInfo.add(new TraceRepeatingEdgeInfo(traceId,0));
//							e.setAttribute("info", edgeSetInfo);
//
//							edgeIdSuperSet.add(edgeLabel);
//							edgeIdSet.add(edgeLabel);
//
//						}else {
//							// already contains that node in the superSet
//
//							if(nodeIdSet.contains(check.toString())) {
//								n= graph.getNode(check.toString());
//								n.setAttribute("ui.label", "R_"+check.toString());
//							}else {
//								nodeIdSet.add(check.toString());
//							}
//
//							edgeLabel=lastNodeId+check.toString();
//							// new edge?
//							if(!edgeIdSuperSet.contains(edgeLabel)) {
//								//new Edge
//								if(lastNodeId.equals(check.toString())) {
//									//self repeating
//									Edge e=graph.addEdge(edgeLabel, lastNodeId, check.toString(),true);
//									e.setAttribute("ui.label", "R");
//									List<TraceRepeatingEdgeInfo> edgeSet = new ArrayList<TraceRepeatingEdgeInfo>();
//									edgeSet.add(new TraceRepeatingEdgeInfo(traceId, 1));
//									e.setAttribute("info", edgeSet);
//									edgeIdSuperSet.add(edgeLabel);
//									edgeIdSet.add(edgeLabel);
//								}else {
//									//normal edge
//									Edge e=graph.addEdge(edgeLabel, lastNodeId, check.toString(),true);
//									List<TraceRepeatingEdgeInfo> edgeSetInfo = new ArrayList<TraceRepeatingEdgeInfo>();
//
//									edgeSetInfo.add(new TraceRepeatingEdgeInfo(traceId,0));
//									e.setAttribute("info", edgeSetInfo);
//
//									edgeIdSuperSet.add(edgeLabel);
//									edgeIdSet.add(edgeLabel);
//								}
//							}else {
//								// already contains that Edge in the superSet
//								// but maybe is new in the Set
//
//								if(!edgeIdSet.contains(edgeLabel)) {
//
//									edgeIdSet.add(edgeLabel);
//
//									Edge e= graph.getEdge(edgeLabel);
//									TraceRepeatingEdgeInfo traceEdgeInfo;
//									traceEdgeInfo =new TraceRepeatingEdgeInfo(traceId,0);
//
//									@SuppressWarnings("unchecked")
//									List<TraceRepeatingEdgeInfo> list = (List<TraceRepeatingEdgeInfo>) e.getAttribute("info");
//									if (list.contains(traceEdgeInfo)) {
//										list.get(list.lastIndexOf(traceEdgeInfo)).repeating();
//									}else {
//										list.add(traceEdgeInfo);
//									}
//
//								}else {
//
//									Edge e= graph.getEdge(edgeLabel);
//
//									if(e.getAttribute("ui.label")==null)
//										e.setAttribute("ui.label", "R");
//
//									TraceRepeatingEdgeInfo traceEdgeInfo;
//
//									traceEdgeInfo =new TraceRepeatingEdgeInfo(traceId,1);
//
//									@SuppressWarnings("unchecked")
//									List<TraceRepeatingEdgeInfo> list = (List<TraceRepeatingEdgeInfo>) e.getAttribute("info");
//
//									if (list.contains(traceEdgeInfo)) {
//										list.get(list.lastIndexOf(traceEdgeInfo)).repeating();
//									}else {
//										list.add(traceEdgeInfo);
//									}
//
//								}
//							}
//						}
//
//					}
//
//
//				}
//				lastNodeId= ""+check.toString()+"";
//				check.delete(0, check.length());
//			}
//		}
//
//	}

    public void printNodeSet() {
        Iterator<Node> nodeIt = this.graph.nodes().iterator();
        System.out.print("[");
        while (nodeIt.hasNext()) {
            Node n = nodeIt.next();
            String label = (String) n.getAttribute("ui.label");
            if (!(label.charAt(0) == 'R'))
                System.out.print(n.getId() + "");
            else System.out.print(label);

            if (nodeIt.hasNext())
                System.out.print(",");
        }
        System.out.print("]\n\n");
    }

    public void printEdgeSet() {
        Iterator<Edge> edgeIt = this.graph.edges().iterator();
        System.out.print("[");
        while (edgeIt.hasNext()) {
            Edge e = edgeIt.next();
            String label = (String) e.getAttribute("ui.label");
            if (label == null)
                System.out.print(e.getId() + ", ");
            else System.out.print(e.getId() + "," + label + "");

            if (edgeIt.hasNext())
                System.out.print(",");
        }
        System.out.print("]\n\n");
    }

    public void GraphImage(String graphName) {
        System.setProperty("org.graphstream.ui", "swing");
        graph.setAutoCreate(true);
        graph.setStrict(false);
        //			System.out.println("Edge set info for the Graph "+graphName+":");
        //			Iterator<Edge> it= graph.edges().iterator();
        //			while(it.hasNext()) {
        //				Edge e = it.next();
        //				List<TraceRepeatingEdgeInfo> infos = (List<TraceRepeatingEdgeInfo>) e.getAttribute("info");
        //				Iterator<TraceRepeatingEdgeInfo> infoIt= infos.iterator();
        //				boolean repeating=false;
        //				while(infoIt.hasNext()) {
        //					TraceRepeatingEdgeInfo info = infoIt.next();
        //					if(info.isRepeating()) {
        //						System.out.println("edge: "+e.getId()+ " in the trace: "+info.getTraceId()+" is repeating: "+info.getRepetitions()+" times");
        //						repeating=true;
        //					}else {
        //						System.out.println("edge: "+e.getId()+ " in the trace: "+info.getTraceId()+" is not repeating");
        //					}
        //					}
        //			}
        graph.setAttribute("ui.stylesheet",
                "graph { fill-color: white; } node { size: 30px, 30px; shape: box; fill-color: yellow; stroke-mode: plain; stroke-color: black; }  node:clicked { fill-color: red;} edge { shape: line; text-alignment: above;  fill-mode: dyn-plain; fill-color: #222, #555, green, yellow; arrow-size: 8px, 3px;}");

        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.display();


        FileSinkImages pic = FileSinkImages.createDefault();
        pic.setOutputType(OutputType.PNG);
        pic.setResolution(Resolutions.HD1080);

        pic.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);

        try {

            pic.writeAll(graph, graphName + ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
