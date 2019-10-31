package io.github.springroll;

public class Version {

    private Version() { }

    public static final long HASH = getVersion().hashCode();

    public static String getVersion() {
        // Avoid FindBugs match version string as hard coded IP,
        // use .RELEASE or -SNAPSHOT suffix
        // https://pmd.github.io/pmd-6.15.0/pmd_rules_java_bestpractices.html#avoidusinghardcodedip
        return "0.1.0-SNAPSHOT";
    }

}
