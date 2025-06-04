package org.mcteampotato;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.mcteampotato.attchment.FoodDataAttachment;
import org.mcteampotato.config.ValpotatoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Mod(SOLValpotato.MOD_ID)
public class SOLValpotato {
    public static final String MOD_ID = "sol_valpotato";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPE = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    public static final Supplier<AttachmentType<FoodDataAttachment>> FOOD_DATA = ATTACHMENT_TYPE.register("food_data", () -> AttachmentType.serializable(FoodDataAttachment::new).copyOnDeath().build());

    public SOLValpotato(IEventBus iEventBus, ModContainer modContainer) {
        ATTACHMENT_TYPE.register(iEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, ValpotatoConfig.BUILDER.build());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }
}
