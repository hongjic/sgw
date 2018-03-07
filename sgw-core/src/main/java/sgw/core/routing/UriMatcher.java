package sgw.core.routing;

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
        private Map<String, String> params;

        UriMatchResult(E obj) {
            this(obj, new HashMap<>());
        }

        UriMatchResult(E obj, Map<String, String> map) {
            super();
            this.obj = obj;
            this.params = map;
        }

        UriMatchResult(E obj, Map<String, String> map, PatternCounted pc) {
            super(pc);
            this.obj = obj;
            this.params = map;
        }

        void setObject(E obj) {
            this.obj = obj;
        }

        void setParams(Map<String, String> params) {
            this.params = params;
        }

        public E getObject() {
            return obj;
        }

        public Map<String, String> getParams() {
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
            if (pc1.allPatternCount != pc2.allPatternCount)
                return pc1.allPatternCount < pc2.allPatternCount;
            if (pc1.starCount != pc2.starCount)
                return pc1.starCount < pc2.starCount;
            if (pc1.varCount != pc2.varCount)
                return pc1.varCount < pc2.varCount;
            return pc1.qmarkCount < pc2.qmarkCount;
        }
    }

}
