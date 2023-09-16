package net.dumbcode.projectnublar.server.runtimepack.generator.json.animation;


public class JFrame implements Cloneable {
  public final int index;
  public Integer time;

  public JFrame(int index) {
    this.index = index;
  }

  public JFrame(int index, int time) {
    this(index);
    this.time = time;
  }

  /**
   * Added in BRRP 0.7.0 according to ARRP 0.6.2. Author: Devan Kerman.
   */
  public JFrame time(int time) {
    this.time = time;
    return this;
  }

  @Override
  public JFrame clone() {
    try {
      return (JFrame) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
