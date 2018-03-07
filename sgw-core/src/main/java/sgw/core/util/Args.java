package sgw.core.util;

public class Args {

    public static <T> T notNull(T value, String name) {
        if (value == null)
            throw new IllegalArgumentException("Argument {" + name + "} should not be null.");
        return value;
    }

    public static <T extends CharSequence> T notEmpty(T value, String name) {
        notNull(value, name);
        if (value.length() == 0)
            throw new IllegalArgumentException("Argument {" + name + "} should not be empty.");
        return value;
    }

    public static <T extends CharSequence> T notBlank(T value, String name) {
        notEmpty(value, name);
        boolean blank = true;
        for (int i = 0; i < value.length(); i ++)
            if (!Character.isWhitespace(value.charAt(i)))
                blank = false;
        if (blank)
            throw new IllegalArgumentException("Argument {" + name + "} should not be blank.");
        return value;
    }
}
