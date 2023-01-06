package grafo;

import grafo.controller.TraceController;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class that compares two LogGraph using their Node and Edges List (or Activity and Transition list)
 *
 * @author luigi.bucchicchioAtgmail.com
 */
public class GraphComparator {
    private double logUtilsGamma = (double) 0.0;

    //scores
    private double edgeEqualScore = (double) 1.0;
    private double edgeSemiScore = (double) 0.0;
    private double edgeNotEqualScore = (double) 0.0;
    private double nodeEqualScore = (double) 1.0;
    private double nodeSemiScore = (double) 0.0;
    private double nodeNotEqualScore = (double) 0.0;

    public double nodeScore = (double) 0.00;
    public double edgeScore = (double) 0.00;
    private Graph graph1;
    private Graph graph2;
    private List<Node> nodeSuperSet = new ArrayList<Node>();
    private List<Edge> edgeSuperSet = new ArrayList<Edge>();

    public GraphComparator(Graph g1, Graph g2) {
        this.graph1 = g1;
        this.graph2 = g2;

    }

    public GraphComparator() {
        this.graph1 = null;
        this.graph2 = null;

    }

    public Graph getGraph1() {
        return this.graph1;
    }

    public Graph getGraph2() {
        return this.graph2;
    }

    public void setGraph1(Graph g1) {
        this.graph1 = g1;
    }

    public void setGraph2(Graph g2) {
        this.graph2 = g2;
    }

    public void generateNodeSuperset() {

        Iterator<Node> Nit1 = graph1.nodes().iterator();
        while (Nit1.hasNext()) {
            Node n = Nit1.next();
            boolean duplicate = false;
            for (int i = 0; i < nodeSuperSet.size(); i++) {
                if (n.getId().equals(nodeSuperSet.get(i).getId()))
                    duplicate = true;
            }
            if (!duplicate)
                nodeSuperSet.add(n);
        }

        Iterator<Node> Nit2 = graph2.nodes().iterator();
        while (Nit2.hasNext()) {
            Node n = Nit2.next();
            boolean duplicate = false;
            for (int i = 0; i < nodeSuperSet.size(); i++) {
                if (n.getId().equals(nodeSuperSet.get(i).getId()))
                    duplicate = true;
            }
            if (!duplicate)
                nodeSuperSet.add(n);
        }

    }

    public void generateEdgeSuperset() {
        Iterator<Edge> Eit1 = graph1.edges().iterator();
        while (Eit1.hasNext()) {
            Edge e = Eit1.next();
            boolean duplicate = false;
            for (int i = 0; i < edgeSuperSet.size(); i++) {
                if (e.getId().equals(edgeSuperSet.get(i).getId()))
                    duplicate = true;
            }
            if (!duplicate)
                edgeSuperSet.add(e);
        }

        Iterator<Edge> Eit2 = graph2.edges().iterator();
        while (Eit2.hasNext()) {
            Edge e = Eit2.next();
            boolean duplicate = false;
            for (int i = 0; i < edgeSuperSet.size(); i++) {
                if (e.getId().equals(edgeSuperSet.get(i).getId()))
                    duplicate = true;
            }
            if (!duplicate)
                edgeSuperSet.add(e);
        }
    }

    public int getSizeNodeSuperSet() {
        return this.nodeSuperSet.size();
    }

    public int getSizeEdgeSuperSet() {
        return this.edgeSuperSet.size();
    }

    public void NodeCompare() {

        Iterator<Node> nIt = nodeSuperSet.iterator();
        while (nIt.hasNext()) {
            Node n = nIt.next();

            if (graph1.getNode(n.getId()) == null) {

                nodeScore = nodeScore + nodeNotEqualScore;

            } else {

                if (graph2.getNode(n.getId()) == null) {

                    nodeScore = nodeScore + nodeNotEqualScore;

                } else {

                    Node node1 = graph1.getNode(n.getId());
                    // t12
                    // t56
                    Node node2 = graph2.getNode(n.getId());

                    String string1 = (String) node1.getAttribute("ui.label");
                    String string2 = (String) node2.getAttribute("ui.label");

                    // t56
                    // R_t56

                    if (string1.equals(string2)) {
                        nodeScore = nodeScore + nodeEqualScore;
                    } else {
                        //R_A A
                        if (string1.length() > 1 && string2.length() == 1) {
                            nodeScore = nodeScore + nodeSemiScore;
                            //A R_A
                        } else if (string2.length() > 1 && string1.length() == 1) {
                            nodeScore = nodeScore + nodeSemiScore;
                            //something R_something
                        } else if (string2.charAt(1) == '_' && string1.charAt(1) != '_') {
                            nodeScore = nodeScore + nodeSemiScore;
                            //R_something something
                        } else if (string1.charAt(1) == '_' && string2.charAt(1) != '_') {
                            nodeScore = nodeScore + nodeSemiScore;
                        } else {
                            System.out.println(string1 + " " + string2);
                            throw new IllegalArgumentException();
                        }

                    }
                }
            }
        }
    }

    public void EdgeCompare() {

        Iterator<Edge> eIt = edgeSuperSet.iterator();
        while (eIt.hasNext()) {
            Edge e = eIt.next();

            if (graph1.getEdge(e.getId()) == null) {

                if (logUtilsGamma != (double) 0.0) {
                    edgeScore = edgeScore + edgeNotEqualScore;
                } else {

                    //caso particolare

//					String firstActivity= e.getId().substring(0,(e.getId().length()/2));
//					String secondActivity= e.getId().substring(e.getId().length()/2, e.getId().length());
//					boolean same=firstActivity.equals(secondActivity);
//					if(same&&graph1.getNode(firstActivity)!=null) {
//						edgeScore=edgeScore+edgeSemiScore;
//					}else {
                    edgeScore = edgeScore + edgeNotEqualScore;
//					}
                }

            } else {

                if (graph2.getEdge(e.getId()) == null) {

                    if (logUtilsGamma != (double) 0.0) {
                        edgeScore = edgeScore + edgeNotEqualScore;
                    } else {

                        //caso particolare

//						String firstActivity= e.getId().substring(0,(e.getId().length()/2));
//						String secondActivity= e.getId().substring(e.getId().length()/2, e.getId().length());
//						boolean same=firstActivity.equals(secondActivity);
//						if(same&&graph2.getNode(firstActivity)!=null) {
//							edgeScore=edgeScore+edgeSemiScore;
//						}else {
                        edgeScore = edgeScore + edgeNotEqualScore;
//						}
                    }

                } else {

                    Edge edge1 = graph1.getEdge(e.getId());
                    Edge edge2 = graph2.getEdge(e.getId());

                    String label1 = (String) edge1.getAttribute("ui.label");
                    String label2 = (String) edge2.getAttribute("ui.label");

                    //R , null

                    if (label1 == null) {
                        if (label2 == null) {
                            edgeScore = edgeScore + edgeEqualScore;
                        } else if (label2.charAt(0) == 'R') {
                            edgeScore = edgeScore + edgeSemiScore;
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else {
                        if (label2 == null) {
                            if (label1.charAt(0) == 'R')
                                edgeScore = edgeScore + edgeSemiScore;
                            else throw new IllegalArgumentException();
                        } else {
                            if (label1.equals(label2))
                                edgeScore = edgeScore + edgeEqualScore;
                            else
                                throw new IllegalArgumentException();
                        }
                    }

                }
            }
        }
    }

    public double getMetrics(double gamma) {

        double negativeGamma = 1 - gamma;
        generateNodeSuperset();
        generateEdgeSuperset();
        NodeCompare();
        EdgeCompare();

        double totalNodeScore;
        if (getSizeNodeSuperSet() == 0)
            totalNodeScore = (double) 0.0;
        else
            totalNodeScore = ((gamma) * this.nodeScore) / getSizeNodeSuperSet();

        double totalEdgeScore;
        if (getSizeEdgeSuperSet() == 0)
            totalEdgeScore = (double) 0.0;
        else
            totalEdgeScore = ((negativeGamma) * this.edgeScore) / getSizeEdgeSuperSet();

        return totalNodeScore + totalEdgeScore;
    }

    public double getEdgeEqualScore() {
        return edgeEqualScore;
    }

    public void setEdgeEqualScore(double edgeEqualScore) {
        this.edgeEqualScore = edgeEqualScore;
    }

    public double getEdgeSemiScore() {
        return edgeSemiScore;
    }

    public void setEdgeSemiScore(double edgeSemiScore) {
        this.edgeSemiScore = edgeSemiScore;
    }

    public double getEdgeNotEqualScore() {
        return edgeNotEqualScore;
    }

    public void setEdgeNotEqualScore(double edgeNotEqualScore) {
        this.edgeNotEqualScore = edgeNotEqualScore;
    }

    public double getNodeEqualScore() {
        return nodeEqualScore;
    }

    public void setNodeEqualScore(double nodeEqualScore) {
        this.nodeEqualScore = nodeEqualScore;
    }

    public double getNodeSemiScore() {
        return nodeSemiScore;
    }

    public void setNodeSemiScore(double nodeSemiScore) {
        this.nodeSemiScore = nodeSemiScore;
    }

    public double getNodeNotEqualScore() {
        return nodeNotEqualScore;
    }

    public void setNodeNotEqualScore(double nodeNotEqualScore) {
        this.nodeNotEqualScore = nodeNotEqualScore;
    }

    public double getLogUtilsGamma() {
        return logUtilsGamma;
    }

    public void setLogUtilsGamma(double logUtilsGamma) {
        this.logUtilsGamma = logUtilsGamma;
    }

}
