package net.dumbcode.projectnublar.server.runtimepack.generator.util;

public interface CallableFunction<A, B> {
  @SuppressWarnings("RedundantThrows")
    // I don't know why it adds a "throws", but that exists for compatibility.
  B get(A a) throws Exception;
}
