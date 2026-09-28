package dev.brainstem.gate;

/** Duck interface on the OneShot returned by BehaviorBuilder.create. */
public interface BsGate {
    void bs$setPrefix(BsNode root);
}
