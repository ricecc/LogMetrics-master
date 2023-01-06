package grafo;

import java.util.ArrayList;
import java.util.List;

public class GraphLogAnalyzerTest {

    public static void main(String[] args) {

        GraphLogAnalyzer analyzer = new GraphLogAnalyzer();
        Trace trace1 = new Trace();
        trace1.setTraceId("trace1");
        trace1.setTraceLine("t11t21t35t26t36t62t54t41t66t75t64t51t53t43t34t61t25t35t71t62t73t43t63t42t34t51t24t62t26t71t73t63t42t62t54t51t62t71t81t65t75t82t91");
        trace1.setLogId("log1");

        Trace trace2 = new Trace();
        trace2.setTraceId("trace2");
        trace2.setTraceLine("t11t21t32t41t51t62t26t36t72t62t63t42t54t66t82t51t62t71t73t63t42t51t62t72t63t42t51t62t72t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t72t63t42t51t62t71t81t91");
        trace2.setLogId("log1");

        Trace trace3 = new Trace();
        trace3.setTraceId("trace3");
        trace3.setTraceLine("t11t32t26t62t54t41t65t51t75t62t64t72t63t53t54t42t65t82t51t62t72t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t72t63t42t51t62t72t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t71t73t63t42t51t62t71t73t63t42t51t62t72t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t71t81t91");
        trace3.setLogId("log1");

        List<Trace> traceSet = new ArrayList<Trace>();
        traceSet.add(trace1);
        traceSet.add(trace2);
        traceSet.add(trace3);

        analyzer.setTraceSet(traceSet);
        analyzer.LogAnalyze();
        analyzer.GraphImage("traceTestGraph");

    }

}
