package grafo;

import java.util.List;

/**
 * Support Class to store relevant infos about the traces
 *
 * @author Luigi Bucchicchio, Donici Ionut Bogdan, Riccardo Ceccarani, Roberta Nerla
 */
public class Trace implements Comparable<Trace> {

    // Contenitore per i Grams
    private List<List<String>> grams;

    private List<String> activitySequence;
    private String traceLine;
    private String traceId;
    private String logId;

    public List<String> getActivitySequence() {
        return activitySequence;
    }

    public void setActivitySequence(List<String> activitySet) {
        this.activitySequence = activitySet;
    }

    public String getTraceLine() {
        return traceLine;
    }

    public void setTraceLine(String traceLine) {
        this.traceLine = traceLine;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    @Override
    public int compareTo(Trace o) {
        if (this.traceLine.equals(o.traceLine))
            return 0;
        else return this.traceId.compareTo(o.getTraceId());
    }

    public int getTraceLength() {
        return this.activitySequence.size();
    }


    public List<List<String>> getGrams() {
        return grams;
    }

    public void setGrams(List<List<String>> grams) {
        this.grams = grams;
    }
}
