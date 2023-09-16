package net.dumbcode.projectnublar.server.fossil.api;

import net.dumbcode.projectnublar.server.fossil.api.context.FossilRegistrationContext;
import net.dumbcode.projectnublar.server.fossil.api.context.StoneTypeRegistrationContext;

/**
 * the base class that all extensions inherit from. This is where all registers are received
 */
public interface IFossilExtension {
    /**
     * called during the register fossils phase when fossils are ready to be registered
     *
     * @param context the context that fossils will be registered in. <b>Fossils MUST be registered using this</b>
     */

    void registerFossils(FossilRegistrationContext context);

    /**
     * called during the register stone types phase when stone types are ready to be registered
     *
     * @param context the context that stone types will be registered in. <b>Stone Types MUST be registered using this</b>
     */
    void registerStoneTypes(StoneTypeRegistrationContext context);

    String getName();
}