package com.plank.tfc_detailab;

import net.dries007.tfc.config.TFCConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.minecraftforge.common.ForgeConfigSpec;

import static com.redlimerl.detailab.DetailArmorBar.getConfig;

public class HealthBarCalculator {
    // 延迟初始化配置，避免直接导入
    private static ForgeConfigSpec.BooleanValue isSmallHearts = null;

    private static int calculateHealthBarRows(Player player) {
        float maxHealth = player.getMaxHealth();
        float healthModifier = 1.0f;

        if(isNOLoadIHeartTFC()){
            if(TFCConfig.CLIENT.enableHealthBar.get()) return 9;
            var playerHealth = Math.max(player.getAttributeValue(Attributes.MAX_HEALTH), Math.ceil(player.getHealth()));
            var absorptionHealth = Math.ceil(player.getAbsorptionAmount());
            var healthRow = getConfig().getOptions().toggleCompatibleHeartMod ? 1 : (int) Math.ceil((playerHealth + absorptionHealth) / 20.0f);
            return (healthRow - 1) * Math.max(10 - (healthRow - 2), 3) + 10;
        }

        // 延迟初始化配置
        if (isSmallHearts == null) {
            initializeConfig();
        }
        // 获取TFC的健康修饰符（如果存在）
        FoodData foodData = player.getFoodData();
        if (foodData instanceof TFCFoodData) {
            healthModifier = ((TFCFoodData) foodData).getHealthModifier();
        }
        float maxHealthModified = maxHealth * healthModifier;
        float absorption = player.getAbsorptionAmount();
        // 计算总生命值（包括吸收护盾）
        float totalHealth = maxHealthModified + absorption;
        if(isSmallHearts.get()) return (int) Math.ceil(totalHealth / 10.0f) * 5 + 1;
        else return (int) Math.ceil(totalHealth / 20.0f) * 3 + 7;
    }

    private static int calculateMountHealthBarRows(LivingEntity mount) {
        float maxHealth = mount.getMaxHealth();
        float absorption = mount.getAbsorptionAmount();
        absorption = Float.isNaN(absorption) ? 0 : absorption;
        // 延迟初始化配置
        if (isSmallHearts == null) {
            initializeConfig();
        }
        // 计算总生命值（包括吸收护盾）
        float totalHealth = maxHealth + absorption;
        if (isNOLoadIHeartTFC()) {
            if(TFCConfig.CLIENT.enableHealthBar.get()) return 10;
            else return 0;
        }
        if (isSmallHearts.get()) return (int) Math.ceil(totalHealth / 10.0f) * 5 + 1;
        else return (int) Math.ceil(totalHealth / 20.0f) * 3 + 7;
    }

    /**
     * 计算原版生命值渲染的Y偏移量
     * @param player 玩家实例
     * @return 调整后的Y偏移量
     */
    public static int calculateYOffset(Player player) {
        int offset = 0;
        // 延迟初始化配置
        if (isSmallHearts == null) {
            initializeConfig();
        }
        if(isSmallHearts != null && isSmallHearts.get()){
            offset += calculateHealthBarRows(player);
            if (player.getVehicle() instanceof final LivingEntity entity)
            {
                offset += calculateMountHealthBarRows(entity);
            }
        }
        else {
            offset += calculateHealthBarRows(player);
            if (player.getVehicle() instanceof final LivingEntity entity)
            {
                offset += calculateMountHealthBarRows(entity);
            }
        }
        return offset;
    }

    // 延迟初始化配置，使用反射避免直接依赖
    private static void initializeConfig() {
        if (isNOLoadIHeartTFC()) {
            return;
        }

        try {
            // 使用反射获取配置
            Class<?> configClass = Class.forName("com.alekiponi.ihearttfc.IHTFCConfig");
            Object clientConfig = configClass.getField("CLIENT").get(null);
            isSmallHearts = (ForgeConfigSpec.BooleanValue) clientConfig.getClass().getField("useSmallHearts").get(clientConfig);
        } catch (Exception ignored) {
        }
    }

    // 检测ihearttfc模组是否加载
    public static boolean isNOLoadIHeartTFC() {
        try {
            Class.forName("com.alekiponi.ihearttfc.IHTFCConfig");
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }
}