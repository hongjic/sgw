package sgw.core.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public abstract class PackageScanner {

    protected List<ScanTask> scanTasks = new ArrayList<>();
    protected Set<Class<?>> allClasses = new HashSet<>();

    public PackageScanner() {}

    public PackageScanner ofPackage(String packageName) {
        return ofPackage(packageName, true);
    }

    public PackageScanner ofPackage(String packageName, boolean recursive) {
        scanTasks.add(new ScanTask(packageName, recursive));
        return this;
    }

    /**
     *
     * @param clazz a class found under the specified packages
     * @return true if to put this class into {@code allClasses}
     */
    abstract protected boolean isWanted(Class<?> clazz);

    /**
     * Complete all scanTasks and put the scanned classes into {@code allClasses}
     */
    protected void scan() throws Exception {
        Iterator<ScanTask> iter = scanTasks.iterator();
        while (iter.hasNext()) {
            ScanTask task = iter.next();
            String dirName = task.packcage.replace('.', '/');
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(dirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                switch (url.getProtocol()) {
                    case "file":
                        String absolutePath = URLDecoder.decode(url.getFile(), "UTF-8");
                        scanFiles(task.packcage, absolutePath, task.recursive);
                        break;
                    default:
                        throw new Exception("aaaaa");
                }
            }
        }
    }

    /**
     *
     * @param packageName
     * @param absolutePath
     * @param recursive
     */
    private void scanFiles(String packageName, String absolutePath, boolean recursive) throws Exception {
        File dir = new File(absolutePath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Can't find package named as {" + packageName + "}.");
        }
        scanFiles0(packageName, absolutePath, recursive);
    }

    private void scanFiles0(String packageName, String absolutePath, boolean recursive) throws Exception {
        File file = new File(absolutePath);
        String fileName = file.getName();
        if (file.isFile()) {
            String clazzName = fileName.substring(0, fileName.length() - 6);
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + clazzName);
            if (isWanted(clazz))
                allClasses.add(clazz);
            return;
        }
        File[] subFiles = file.listFiles((f) -> (recursive && f.isDirectory()) || f.getName().endsWith(".class"));

        for (File subFile: subFiles) {
            String pn = packageName;
            if (subFile.isDirectory())
                pn = pn + '.' + subFile.getName();
            scanFiles0(pn, subFile.getAbsolutePath(), recursive);
        }
    }


    private static class ScanTask {
        String packcage;
        boolean recursive;

        ScanTask(String p, boolean r) {
            this.packcage = p;
            this.recursive = r;
        }
    }

}
