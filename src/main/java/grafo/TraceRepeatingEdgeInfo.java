package grafo;

/**
 * Support class to store OPTIONAL infos about the traces
 *
 * @author luigi.bucchicchioAtgmail.com
 */
public class TraceRepeatingEdgeInfo implements Comparable<TraceRepeatingEdgeInfo> {

    private final String traceId;
    private int repetitions = 0;
    boolean repeating = false;

    public TraceRepeatingEdgeInfo(String edgeId, int initialNum) {
        this.traceId = edgeId;
        this.repetitions = initialNum;
    }

    public void repeating() {
        repetitions++;
        if (repetitions >= 1) {
            repeating = true;
        }
    }

    public boolean isRepeating() {
        return repeating;
    }

    public int getRepetitions() {
        return this.repetitions;
    }

    public String getTraceId() {
        return this.traceId;
    }

    @Override
    public int compareTo(TraceRepeatingEdgeInfo o) {
        return this.traceId.compareTo(o.getTraceId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((traceId == null) ? 0 : traceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TraceRepeatingEdgeInfo other = (TraceRepeatingEdgeInfo) obj;
        if (traceId == null) {
            if (other.traceId != null)
                return false;
        } else if (!traceId.equals(other.traceId))
            return false;
        return true;
    }

}
