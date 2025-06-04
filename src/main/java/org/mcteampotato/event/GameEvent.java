package org.mcteampotato.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mcteampotato.SOLValpotato;
import org.mcteampotato.attchment.FoodData;
import org.mcteampotato.attchment.FoodDataAttachment;
import org.mcteampotato.attchment.FoodEffectInstance;
import org.mcteampotato.attchment.FoodInstance;
import org.mcteampotato.network.SyncFoodDataPacket;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SOLValpotato.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class GameEvent {
    private static boolean HealSet = false;

    @SubscribeEvent
    public static void tooltipsEvent(ItemTooltipEvent event) {
        List<Component> toolTips = event.getToolTip();
        ItemStack itemStack = event.getItemStack();
        FoodData.FoodInfo foodInfo = FoodData.getInfo(itemStack);

        if (foodInfo != null) {
            if (itemStack.is(Items.OMINOUS_BOTTLE)) return;
            if (itemStack.is(Items.ROTTEN_FLESH)) {
                toolTips.add(1, Component.translatable("tooltips.sol_valpotato.empty").withStyle(ChatFormatting.GOLD));
                return;
            }
            List<Component> list = new ArrayList<>();
            list.add(Component.literal("❤️ %.1f 额外生命".formatted(((float) foodInfo.getHearts()) / 2)).withStyle(ChatFormatting.RED));
            list.add(Component.literal("⚡ %.1f 脱战恢复".formatted(foodInfo.getRestore())).withStyle(ChatFormatting.GREEN));
            list.add(Component.literal("⏳ %.1f 持续时间".formatted(((float) foodInfo.getDurationTicks()) / 1200)).withStyle(ChatFormatting.BLUE));
            toolTips.addAll(1, list);
        }
    }

    @SubscribeEvent
    public static void rightClick(PlayerInteractEvent.RightClickItem event) {
        LivingEntity livingEntity = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (livingEntity.getAirSupply() < livingEntity.getMaxAirSupply()) {
            event.setCanceled(true);
        }
        event.setCanceled(checkFood(livingEntity, stack));
    }

    @SubscribeEvent
    public static void useItem(LivingEntityUseItemEvent.Start event) {
        LivingEntity livingEntity = event.getEntity();
        ItemStack stack = event.getItem();
        event.setCanceled(checkFood(livingEntity,stack));
    }

    public static boolean checkFood(LivingEntity livingEntity, ItemStack stack){
        Item item = stack.getItem();
        FoodData.FoodInfo info = FoodData.getInfo(stack);
        if (item instanceof PotionItem || stack.is(Items.ROTTEN_FLESH) || item instanceof OminousBottleItem) return false;
        if (info != null || livingEntity instanceof ServerPlayer serverPlayer && stack.getFoodProperties(serverPlayer) != null) {
            FoodDataAttachment foodData = livingEntity.getData(SOLValpotato.FOOD_DATA);
            return foodData.isFull() || foodData.isSame(item);
        }
        return false;
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            FoodDataAttachment foodData = serverPlayer.getData(SOLValpotato.FOOD_DATA);
            PacketDistributor.sendToPlayer(serverPlayer, new SyncFoodDataPacket(foodData.serialize(serverPlayer.level().registryAccess())));
        }
    }

    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            serverPlayer.getPersistentData().putLong("SOL:HurtTime", System.currentTimeMillis() + 10 * 20 * 50);
            if (serverPlayer.invulnerableTime == 10) {
                FoodDataAttachment foodData = serverPlayer.getData(SOLValpotato.FOOD_DATA);
                for (int i = 0; i < event.getAmount() * 60; i++) {
                    foodData.tick(serverPlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public static void heal(LivingHealEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            if (!HealSet) {
                event.setAmount((float) (event.getAmount() * 0.5));
            } else {
                FoodDataAttachment foodData = serverPlayer.getData(SOLValpotato.FOOD_DATA);
                for (int i = 0; i < event.getAmount() * 20; i++) {
                    foodData.tick(serverPlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        FoodDataAttachment foodData = player.getData(SOLValpotato.FOOD_DATA);
        List<FoodInstance> foodInstanceList = foodData.getSlots();
        if (player instanceof ServerPlayer serverPlayer) {
            foodData.tick(serverPlayer);
            long hurtTime = serverPlayer.getPersistentData().getLong("SOL:HurtTime");
            float heal = 0;
            if (hurtTime != 0 && System.currentTimeMillis() - hurtTime > 0 && player.tickCount % 20 == 0 && serverPlayer.getHealth() < serverPlayer.getMaxHealth()) {
                for (FoodInstance foodInstance : foodInstanceList) {
                    heal += foodInstance.getInfo().getRestore();
                }
                HealSet = true;
                serverPlayer.heal(heal * (float) Math.min(((System.currentTimeMillis() - hurtTime) / 10 * 20 * 50), 1));
                HealSet = false;
            }
            if (player.tickCount % 600 == 0) {
                AddEffect(serverPlayer, foodInstanceList);
            }
        }
    }


    @SubscribeEvent
    public static void deathEvent(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getData(SOLValpotato.FOOD_DATA).clear(serverPlayer);
        }
    }

    public static void AddEffect(ServerPlayer serverPlayer, List<FoodInstance> foodInstanceList) {
        for (MobEffectInstance effect : new ArrayList<>(serverPlayer.getActiveEffects())) {
            if (effect instanceof FoodEffectInstance) {
                serverPlayer.removeEffect(effect.getEffect());
            }
        }
        for (FoodInstance foodInstance : foodInstanceList) {
            FoodProperties foodProps = foodInstance.getInfo().getItemStack().getFoodProperties(null);
            if (foodProps != null) {
                for (FoodProperties.PossibleEffect possibleEffect : foodProps.effects()) {
                    MobEffectInstance foodEffect = possibleEffect.effect();
                    MobEffectInstance currentEffect = serverPlayer.getEffect(foodEffect.getEffect());
                    if (currentEffect == null || currentEffect.getAmplifier() < foodEffect.getAmplifier()) {
                        serverPlayer.addEffect(new FoodEffectInstance(
                                foodEffect.getEffect(),
                                -1,
                                foodEffect.getAmplifier(),
                                true,
                                false,
                                true
                        ));
                    }
                }
            }
        }
    }
}
