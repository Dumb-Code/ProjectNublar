package net.dumbcode.projectnublar.server.fossil.api;

/**
 * Fossil extensions can annotate their extension class with this to automatically register it to the API. This method of registration is useful because it allows for soft dependencies
 * <br>
 * Using annotation <b>CANNOT</b> be done if the extension's constructor(s) have more than zero arguments
 * <br>
 * <br>
 * Annotated classes <b>MUST</b> implement {@link IFossilExtension}
 * Multiple extensions can be loaded per mod
 * Only classes can be annotated with this
 */
@java.lang.annotation.Target(value = java.lang.annotation.ElementType.TYPE)
public @interface Extension {}
