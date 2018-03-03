package sgw.core.http_channel.routing;

import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.annotation.ThreadSafe;
import sgw.core.util.Args;
import sgw.core.util.CopyOnWriteHashMap;
import sgw.core.util.TrieNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @param <T> The type of object requests map to.
 */
@ThreadSafe
public class UriMatcherImpl<T> implements UriMatcher<T> {

    private static final char DELIMITER = '/';

    private final UriLevel<T> rootLevel = new UriLevel<>("", null);

    /**
     *
     * @param path
     * @return null if no match found.
     */
    @Override
    public UriMatchResult<T> lookup(String path) {
        Args.notBlank(path, "path");
        UriMatchResult<T> result = null;
        String p = formatPathPattern(path);

        if (p.length() == 1 && p.charAt(0) == DELIMITER)
            return rootLevel.getObj() == null ? null : new UriMatchResult<>(rootLevel.getObj());

        Queue<UriLevelMatchResult<T>> resultQueue = new LinkedList<>();
        Queue<String> pathQueue = new LinkedList<>();
        resultQueue.offer(new UriLevelMatchResult<>(rootLevel));
        pathQueue.offer(p);
        String pathLeft;
        while (!resultQueue.isEmpty()) {
            UriLevelMatchResult<T> r = resultQueue.poll();
            pathLeft = pathQueue.poll();
            String subPath = nextSubPath(pathLeft);
            ArrayList<UriLevelMatchResult<T>> list = r.uriLevel.matchSubPath(subPath.substring(1));
            for (UriLevelMatchResult<T> levelMatchResult: list) {
                levelMatchResult.addAllParsedResults(r.params);
                levelMatchResult.accumulatePatternCount(r);
            }
            pathLeft = pathLeft.substring(subPath.length());
            if (pathLeft.length() == 0) {
                for (UriLevelMatchResult<T> levelMatchResult : list)
                if (levelMatchResult.isValidMapping()) {
                    if (result == null)
                        result = new UriMatchResult<>(levelMatchResult.uriLevel.getObj(), levelMatchResult.params, levelMatchResult);
                    else {
                        if (PatternCounted.betterMatch(levelMatchResult, result)) {
                            result.setObject(levelMatchResult.uriLevel.getObj());
                            result.setParams(levelMatchResult.params);
                            result.setPatternCounting(levelMatchResult);
                        }
                    }
                }
            }
            else {
                for (UriLevelMatchResult<T> levelMatchResult: list) {
                    resultQueue.offer(levelMatchResult);
                    pathQueue.offer(pathLeft);
                }
            }
        }

        return result;
    }

    @Override
    public synchronized T register(final String pattern, T obj) {
        Args.notBlank(pattern, "pattern");

        UriLevel<T> uriLevel = findUriLevelByPath(pattern, rootLevel, true);
        return uriLevel.setObj(obj);
    }

    /**
     * 每个node要加的child全部统计出来，一次性完成。减少CopyOnWriteHashMap复制的次数
     * @param map
     */
    @Override
    public synchronized void registerAll(Map<String, T> map) {
        Args.notNull(map, "map");
        TrieNode<T> root = convertToTrie(map);

        // register all from Trie
        Queue<Object[]> queue = new LinkedList<>();
        queue.offer(new Object[] {root, rootLevel});
        while (!queue.isEmpty()) {
            Object[] ele = queue.poll();
            TrieNode<T> node = (TrieNode<T>) ele[0];
            UriLevel<T> level = (UriLevel<T>) ele[1];
            if (node.getValue() != null)
                level.setObj(node.getValue());
            Map<String, TrieNode<T>> children = node.getChildren();
            Collection<String> childPatterns = children.keySet();
            level.createChildrenByPatterns(childPatterns);

            for (String childPattern: childPatterns) {
                queue.offer(new Object[] {children.get(childPattern), level.findByPattern(childPattern)});
            }
        }

    }

    @Override
    public synchronized void initialize(Map<String, T> map) {
        clear();
        registerAll(map);
    }

    @Override
    public synchronized T unregister(String pattern) {
        Args.notBlank(pattern, "pattern");

        UriLevel<T> uriLevel = findUriLevelByPath(pattern, rootLevel, false);
        if (uriLevel == null)
            return null;

        T old = uriLevel.setObj(null);
        // remove uriLevel if it is an empty leaf.
        uriLevel.removeSelfIfEmptyLeaf();
        return old;
    }

    @Override
    public synchronized void unregisterAll(Collection<String> patterns) {
        for (String pattern: patterns)
            unregister(pattern);
    }

    @Override
    public synchronized void clear() {
        this.rootLevel.clear();
    }

    /**
     *
     * @param path start with '/'
     * @return the next uri block (start with '/')
     */
    private static String nextSubPath(String path) {
        if (path.length() == 0) return path;
        if (path.charAt(0) != DELIMITER)
            throw new IllegalArgumentException("Invalid Path: {" + path + "}");

        StringBuilder next = new StringBuilder();
        int len = path.length(), i = 0;
        next.append(DELIMITER);
        char c;
        while (i < len - 1) {
            c = path.charAt(++ i);
            if (c == DELIMITER) break;
            next.append(c);
            if (c == '{') {
                while (i < len && c != '}') {
                    next.append((c = path.charAt(++ i)));
                }
                if (i == len)
                    throw new IllegalArgumentException("Invalid path, '}' not found.");
            }
        }
        return next.toString();
    }

    private static String formatPathPattern(String pattern) {
        String p = pattern.trim();
        if (p.charAt(0) != DELIMITER)
            p = DELIMITER + p;
        return p;
    }

    /**
     *
     * @param path path to find
     * @param rootLevel uriLevel root
     * @param create create if pattern not exist
     * @param <T>
     * @return uriLevel found
     */
    private static <T> UriLevel<T> findUriLevelByPath(String path, UriLevel<T> rootLevel, boolean create) {
        String p = formatPathPattern(path);
        UriLevel<T> uriLevel = rootLevel;
        if (!(p.length() == 1 && p.charAt(0) == DELIMITER)) { // not root
            String subPath;
            while (!(subPath = nextSubPath(p)).equals("")) {
                p = p.substring(subPath.length());

                if (subPath.length() == 1) // e.g. pattern = '//'
                    throw new IllegalArgumentException("Invalid pattern: " + path);
                subPath = subPath.substring(1);
                uriLevel = uriLevel.findByPattern(subPath, create);
            }
        }
        return uriLevel;
    }

    private static <T> TrieNode<T> convertToTrie(Map<String, T> map) {
        TrieNode<T> root = new TrieNode<>();
        // format mapping data into a Trie.
        for (Map.Entry<String, T> entry: map.entrySet()) {
            final String pattern = entry.getKey();
            final T obj = entry.getValue();
            String p = formatPathPattern(entry.getKey());

            TrieNode<T> node = root;
            if (!(p.length() == 1 && p.charAt(0) == DELIMITER)) { // not root
                String subPath;
                while (!(subPath = nextSubPath(p)).equals("")) {
                    p = p.substring(subPath.length());

                    if (subPath.length() == 1)
                        throw new IllegalArgumentException("Invalid pattern: " + pattern);
                    subPath = subPath.substring(1);
                    node = node.getChild(subPath);
                }
            }
            node.setValue(obj);
        }
        return root;
    }

    /**
     * Does not guarantee thread safe on all methods.
     * While {@link UriMatcherImpl} can gurantee thread safe
     * @param <T>
     */
    @NotThreadSafe
    private static class UriLevel<T> extends PatternCounted {
        private volatile UriLevel<T> parent;
        private final String pattern;
        private final Pattern regex;
        private final String paramName;
        private volatile T obj;
        private final CopyOnWriteHashMap<String, UriLevel<T>> directMapping;
        private final CopyOnWriteHashMap<String, UriLevel<T>> patternMapping;

        UriLevel(String pattern, UriLevel<T> parent) {
            this(pattern, null, parent);
        }

        UriLevel(String pattern, T obj, UriLevel<T> parent) {
            this.pattern = pattern;
            if (!isDirectMapping(pattern)) {
                CompileResult result = compilePattern(pattern);
                this.regex = result.regex;
                this.paramName = result.paramName;
                this.starCount = result.starCount;
                this.qmarkCount = result.qmarkCount;
                this.varCount = result.varCount;
            } else {
                this.paramName = null;
                this.regex = null;
            }
            this.obj = obj;
            this.parent = parent;
            directMapping = new CopyOnWriteHashMap<>();
            patternMapping = new CopyOnWriteHashMap<>();
        }

        T setObj(T obj) {
            T old = this.obj;
            this.obj = obj;
            return old;
        }

        T getObj() {
            return this.obj;
        }

        boolean isLeaf() {
            return directMapping.isEmpty() && patternMapping.isEmpty();
        }

        UriLevel<T> removeChild(String pattern) {
            if (isDirectMapping(pattern))
                return directMapping.remove(pattern);
            else
                return patternMapping.remove(pattern);
        }

        void removeSelfIfEmptyLeaf() {
            UriLevel<T> level = this;
            UriLevel<T> parent;
            while (level.isLeaf() && ((parent = level.parent) != null)) {
                level.parent = null;
                parent.removeChild(level.pattern);
                level = parent;
            }
        }

        void clear() {
            if (parent != null)
                throw new IllegalStateException("Only root level can invoke clear().");
            directMapping.clear();
            patternMapping.clear();
            this.obj = null;
        }

        /**
         * Find a child using the given {@param pattern}.
         * @param pattern uriLevel path
         * @return the child UriLevel found. Return null if pattern not found.
         */
        UriLevel<T> findByPattern(String pattern) {
            return findByPattern(pattern, false);
        }

        /**
         * Find a child using the given {@param pattern}, create one if not exist.
         * @param pattern uriLevel path
         * @param create whether create when pattern not exists
         * @return The child UriLevel found. Return null if pattern not found and {@param create} set to false.
         */
        UriLevel<T> findByPattern(String pattern, boolean create) {
            if (isDirectMapping(pattern)) {
                if (directMapping.containsKey(pattern))
                    return directMapping.get(pattern);
                else {
                    if (!create)
                        return null;
                    UriLevel<T> newLevel = new UriLevel<>(pattern, this);
                    directMapping.put(pattern, newLevel);
                    return newLevel;
                }
            }
            if (patternMapping.containsKey(pattern))
                return patternMapping.get(pattern);
            else {
                if (!create)
                    return null;
                UriLevel<T> newLevel = new UriLevel<>(pattern, this);
                patternMapping.put(pattern, newLevel);
                return newLevel;
            }
        }

        /**
         * Create new uriLevels for patterns currently not exist.
         * @param patterns
         */
        void createChildrenByPatterns(Collection<String> patterns) {
            HashMap<String, UriLevel<T>> simple = new HashMap<>();
            HashMap<String, UriLevel<T>> complex = new HashMap<>();
            for (String pattern: patterns) {
                if (isDirectMapping(pattern)) {
                    if (!directMapping.containsKey(pattern))
                        simple.put(pattern, new UriLevel<>(pattern, this));
                }
                else {
                    if (!patternMapping.containsKey(pattern))
                        complex.put(pattern, new UriLevel<>(pattern, this));
                }
            }

            // create new ones.
            directMapping.putAll(simple);
            patternMapping.putAll(complex);
        }

        /**
         *
         * @param subPath
         * @return
         */
        ArrayList<UriLevelMatchResult<T>> matchSubPath(String subPath) {
            ArrayList<UriLevelMatchResult<T>> results = new ArrayList<>();
            // check direct mapping
            if (directMapping.containsKey(subPath))
                results.add(new UriLevelMatchResult<>(directMapping.get(subPath)));
            // check pattern mapping
            for (Map.Entry<String, UriLevel<T>> entry: patternMapping.entrySet()) {
                UriLevel<T> subLevel = entry.getValue();
                Pattern regex = subLevel.regex;
                Matcher matcher = regex.matcher(subPath);
                if (matcher.matches()) {
                    UriLevelMatchResult<T> r = new UriLevelMatchResult<>(subLevel);
                    if (subLevel.paramName != null) {
                        String paramValue = matcher.group(subLevel.paramName);
                        r.addParsedResult(subLevel.paramName, paramValue);
                    }
                    results.add(r);
                }
            }
            return results;
        }

        /**
         *
         * @param subPath
         * @return true if `subPath` contains no pattern.
         */
        private boolean isDirectMapping(String subPath) {
            return !(subPath.indexOf('{') >= 0 || subPath.indexOf('}') >= 0
                    || subPath.indexOf('*') >= 0 || subPath.indexOf('?') >= 0);
        }

        private static CompileResult compilePattern(String pattern) {
            char[] arr = pattern.toCharArray();
            int i = 0;
            StringBuilder regexStr = new StringBuilder();
            StringBuilder paramName = new StringBuilder();
            CompileResult compileResult = new CompileResult();
            while (i < arr.length) {
                if (arr[i] == '?') {
                    compileResult.qmarkCount ++;
                    regexStr.append("[^/]?");
                }
                if (arr[i] == '*') {
                    compileResult.starCount ++;
                    regexStr.append("[^/]*");
                }
                else if (arr[i] == '{') {
                    compileResult.varCount ++;
                    i ++;
                    while (i < arr.length && (arr[i] != ':' && arr[i] != '}'))
                        paramName.append(arr[i ++]);
                    if (i == arr.length)
                        throw new IllegalArgumentException("Invalid pattern.");
                    if (arr[i] == '}')
                        regexStr.append("(?<" + paramName + ">[^/]+)");
                    else {
                        i ++;
                        StringBuilder paramRegex = new StringBuilder();
                        while (i < arr.length && arr[i] != '}')
                            paramRegex.append(arr[i ++]);
                        if (i == arr.length)
                            throw new IllegalArgumentException("Invalid pattern.");
                        regexStr.append("(?<" + paramName + ">" + paramRegex + ")");
                    }
                }
                else {
                    regexStr.append(arr[i]);
                }
                i ++;
            }

            compileResult.regex = Pattern.compile(regexStr.toString());
            compileResult.paramName = paramName.length() == 0 ? null : paramName.toString();
            return compileResult;
        }
    }

    private static class UriLevelMatchResult<T> extends PatternCounted {
        UriLevel<T> uriLevel;
        Map<String, Object> params = new HashMap<>();

        UriLevelMatchResult(UriLevel<T> uriLevel) {
            this(uriLevel, new HashMap<>());
        }

        UriLevelMatchResult(UriLevel<T> uriLevel, Map<String, Object> params) {
            super(uriLevel);
            this.uriLevel = uriLevel;
            this.params.putAll(params);
        }

        void addParsedResult(String name, Object value) {
            this.params.put(name, value);
        }

        void addAllParsedResults(Map<String, Object> map) {
            this.params.putAll(map);
        }

        boolean isValidMapping() {
            return uriLevel.getObj() != null;
        }

    }

    private static class CompileResult extends PatternCounted {
        Pattern regex;
        String paramName;
    }

}
