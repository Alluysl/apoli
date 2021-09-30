package io.github.apace100.apoli.power.factory.action;


import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.FilterableWeightedList;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.Random;

public class BiEntityActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ActionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("actions", ApoliDataTypes.BIENTITY_ACTIONS),
            (data, entities) -> ((List<ActionFactory<Pair<Entity, Entity>>.Instance>)data.get("actions")).forEach((e) -> e.accept(entities))));
        register(new ActionFactory<>(Apoli.identifier("chance"), new SerializableData()
            .add("action", ApoliDataTypes.BIENTITY_ACTION)
            .add("chance", SerializableDataTypes.FLOAT),
            (data, entities) -> {
                if(new Random().nextFloat() < data.getFloat("chance")) {
                    ((ActionFactory<Pair<Entity, Entity>>.Instance)data.get("action")).accept(entities);
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("choice"), new SerializableData()
            .add("actions", SerializableDataType.weightedList(ApoliDataTypes.BIENTITY_ACTION)),
            (data, entities) -> {
                FilterableWeightedList<ActionFactory<Pair<Entity, Entity>>.Instance> actionList = (FilterableWeightedList<ActionFactory<Pair<Entity, Entity>>.Instance>)data.get("actions");
                ActionFactory<Pair<Entity, Entity>>.Instance action = actionList.pickRandom(new Random());
                action.accept(entities);
            }));
        register(new ActionFactory<>(Apoli.identifier("invert"), new SerializableData()
            .add("action", ApoliDataTypes.BIENTITY_ACTION),
            (data, entities) -> {
                ((ActionFactory<Pair<Entity, Entity>>.Instance)data.get("action")).accept(new Pair<>(entities.getRight(), entities.getLeft()));
            }));
        register(new ActionFactory<>(Apoli.identifier("actor_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION),
            (data, entities) -> {
                ((ActionFactory<Entity>.Instance)data.get("action")).accept(entities.getLeft());
            }));
        register(new ActionFactory<>(Apoli.identifier("target_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION),
            (data, entities) -> {
                ((ActionFactory<Entity>.Instance)data.get("action")).accept(entities.getRight());
            }));
        register(new ActionFactory<>(Apoli.identifier("nothing"), new SerializableData(),
            (data, entities) -> {}));

        register(new ActionFactory<>(Apoli.identifier("mount"), new SerializableData(),
            (data, entities) -> {
                entities.getLeft().startRiding(entities.getRight(), true);
            }));
        register(new ActionFactory<>(Apoli.identifier("set_in_love"), new SerializableData(),
            (data, entities) -> {
                if(entities.getRight() instanceof AnimalEntity && entities.getLeft() instanceof PlayerEntity) {
                    ((AnimalEntity)entities.getRight()).lovePlayer((PlayerEntity)entities.getLeft());
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("tame"), new SerializableData(),
            (data, entities) -> {
                if(entities.getRight() instanceof TameableEntity && entities.getLeft() instanceof PlayerEntity) {
                    if(!((TameableEntity)entities.getRight()).isTamed()) {
                        ((TameableEntity)entities.getRight()).setOwner((PlayerEntity)entities.getLeft());
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("add_velocity"), new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 0F)
            .add("y", SerializableDataTypes.FLOAT, 0F)
            .add("z", SerializableDataTypes.FLOAT, 0F)
            .add("set", SerializableDataTypes.BOOLEAN, false)
            .add("actor", SerializableDataTypes.BOOLEAN, false) // whether to move the actor instead of the target
            .add("client_only", SerializableDataTypes.BOOLEAN, false) // preferred true but false by default for backward compatibility
            .add("server_only", SerializableDataTypes.BOOLEAN, false), // preferred false though we might as well allow it to be specified
            (data, entities) -> {
                Entity actor = entities.getLeft(), target = entities.getRight(), moving = data.getBoolean("actor") ? actor : target;
                if (moving instanceof PlayerEntity
                    && (moving.world.isClient ?
                    data.getBoolean("server_only") : data.getBoolean("client_only")))
                    return;
                Vec3f vec = new Vec3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                TriConsumer<Float, Float, Float> method = moving::addVelocity;
                if(data.getBoolean("set"))
                    method = moving::setVelocity;
                Space.transformVectorToBase(target.getPos().subtract(actor.getPos()), vec, actor.getYaw(), true);
                method.accept(vec.getX(), vec.getY(), vec.getZ());
                moving.velocityModified = true;
            }));
        register(new ActionFactory<>(Apoli.identifier("log"), new SerializableData()
            .add("message", SerializableDataTypes.STRING)
            .add("show_variables", SerializableDataTypes.BOOLEAN, false)
            .add("warning", SerializableDataTypes.BOOLEAN, false)
            .add("client_only", SerializableDataTypes.BOOLEAN, false)
            .add("server_only", SerializableDataTypes.BOOLEAN, false),
            (data, entities) -> {
                if (entities.getLeft().world.isClient ? data.getBoolean("server_only") : data.getBoolean("client_only"))
                    return; // if client and server-only, or server and client-only, abort
                String message = data.getString("message");
                if (data.getBoolean("show_variables"))
                    message += entities.getLeft() + " " + entities.getRight();
                if (data.getBoolean("warning"))
                    Apoli.LOGGER.warn(message);
                else
                    Apoli.LOGGER.info(message);
            }));
    }

    private static void register(ActionFactory<Pair<Entity, Entity>> actionFactory) {
        Registry.register(ApoliRegistries.BIENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
