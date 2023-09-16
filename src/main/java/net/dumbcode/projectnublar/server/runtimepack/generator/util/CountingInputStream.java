package net.dumbcode.projectnublar.server.runtimepack.generator.util;

import java.io.IOException;
import java.io.InputStream;

public class CountingInputStream extends InputStream {
  private final InputStream input;
  private int read;

  public CountingInputStream(InputStream input) {
    this.input = input;
  }

  @Override
  public int read() throws IOException {
    int read = this.input.read();
    if (read != -1) {
      this.read++;
    }
    return read;
  }

  @Override
  public int read(byte [] b, int off, int len) throws IOException {
    int read = this.input.read(b, off, len);
    if (read != -1) {
      this.read += read;
    }
    return read;
  }

  public int bytes() {
    return this.read;
  }
}
