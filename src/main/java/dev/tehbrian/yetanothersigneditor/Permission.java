package dev.tehbrian.yetanothersigneditor;

/**
 * Holds permission constants.
 */
public final class Permission {

	public static final String ROOT = "yase";

	public static final String SET = ROOT + ".set";
	public static final String OPEN = ROOT + ".open";
	public static final String COPY = ROOT + ".copy";
	public static final String UNWAX = ROOT + ".unwax";
	public static final String RELOAD = ROOT + ".reload";

	public static final String FORMAT = ROOT + ".format";
	public static final String MINIMESSAGE = FORMAT + ".minimessage";
	public static final String LEGACY = FORMAT + ".legacy";

	private Permission() {
	}

}
