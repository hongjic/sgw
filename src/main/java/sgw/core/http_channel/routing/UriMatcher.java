package sgw.core.http_channel.routing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface UriMatcher<T> {

    UriMatchResult<T> lookup(String path);

    T register(String pattern, T obj);

    void registerAll(Map<String, T> map);

    void initialize(Map<String, T> map);

    T unregister(String pattern);

    void unregisterAll(Collection<String> patterns);

    void clear();

    class UriMatchResult<E> extends PatternCounted{
        private E obj;
        private Map<String, Object> params;

        UriMatchResult(E obj) {
            this(obj, new HashMap<>());
        }

        UriMatchResult(E obj, Map<String, Object> map) {
            super();
            this.obj = obj;
            this.params = map;
        }

        UriMatchResult(E obj, Map<String, Object> map, PatternCounted pc) {
            super(pc);
            this.obj = obj;
            this.params = map;
        }

        void setObject(E obj) {
            this.obj = obj;
        }

        void addParsedParam(String name, Object value) {
            params.put(name, value);
        }

        void setParams(Map<String, Object> params) {
            this.params = params;
        }

        E getObject() {
            return obj;
        }

        Map<String, Object> getParams() {
            return params;
        }

    }

    abstract class PatternCounted {
        int starCount;
        int qmarkCount;
        int varCount;
        int allPatternCount;

        PatternCounted() { }

        PatternCounted(PatternCounted pc) {
            this(pc.starCount, pc.qmarkCount, pc.varCount);
        }

        PatternCounted(int starCount, int qmarkCount, int varCount) {
            this.starCount = starCount;
            this.qmarkCount = qmarkCount;
            this.varCount = varCount;
            this.allPatternCount = starCount + qmarkCount + varCount;
        }

        void accumulatePatternCount(PatternCounted pc) {
            starCount += pc.starCount;
            qmarkCount += pc.qmarkCount;
            varCount += pc.varCount;
            allPatternCount += pc.allPatternCount;
        }

        void setPatternCounting(PatternCounted pc) {
            starCount = pc.starCount;
            qmarkCount = pc.qmarkCount;
            varCount = pc.varCount;
            allPatternCount = pc.allPatternCount;
        }

        /**
         *
         * @param pc1
         * @param pc2
         * @return true if {@param pc1} is better than {@param pc2}
         */
        static boolean betterMatch(PatternCounted pc1, PatternCounted pc2) {
            return pc1.allPatternCount < pc2.allPatternCount
                    || pc1.starCount < pc2.starCount
                    || pc1.varCount < pc2.varCount
                    || pc1.qmarkCount < pc2.qmarkCount;
        }
    }

}
