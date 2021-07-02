package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

public class ItemActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ActionFactory<>(Apoli.identifier("consume"), new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1),
            (data, stack) -> {
                stack.decrement(data.getInt("amount"));
            }));

        register(new ActionFactory<>(Apoli.identifier("log"), new SerializableData()
            .add("message", SerializableDataTypes.STRING)
            .add("show_variables", SerializableDataTypes.BOOLEAN, false)
            .add("warning", SerializableDataTypes.BOOLEAN, false),
            (data, stack) -> {
                String message = data.getString("message");
                if (data.getBoolean("show_variables"))
                    message += stack;
                if (data.getBoolean("warning"))
                    Apoli.LOGGER.warn(message);
                else
                    Apoli.LOGGER.info(message);
            }));
    }

    private static void register(ActionFactory<ItemStack> actionFactory) {
        Registry.register(ApoliRegistries.ITEM_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
