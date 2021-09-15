package xyz.tehbrian.yetanothersigneditor;

/**
 * Constant values.
 */
public final class Constants {

    private Constants() {
    }

    /**
     * Permissions.
     */
    public static final class Permissions {

        public static final String ROOT = "yase";

        public static final String SET = ROOT + ".set";
        public static final String EDIT = ROOT + ".edit";
        public static final String RELOAD = ROOT + ".reload";

        public static final String COLOR = ROOT + ".color";
        public static final String MINI_MESSAGE = COLOR + ".minimessage";
        public static final String LEGACY = COLOR + ".legacy";

        private Permissions() {
        }

    }

}
