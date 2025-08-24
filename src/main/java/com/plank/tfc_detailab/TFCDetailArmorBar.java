package com.plank.tfc_detailab;

import com.redlimerl.detailab.api.DetailArmorBarAPI;
import com.redlimerl.detailab.api.render.ArmorBarRenderManager;
import com.redlimerl.detailab.api.render.TextureOffset;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Mod(TFCDetailArmorBar.MODID)
@SuppressWarnings("removal")
public class TFCDetailArmorBar {
    public static final String MODID = "tfc_detailab";
    public TFCDetailArmorBar(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);
    }

    // 在FMLLoadCompleteEvent事件中执行物品查找，确保所有Mod已完成注册
    @SubscribeEvent
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            Map<ArmorItem[], CompatInfo> modCompats = new CompatBuilder()
                    .complexAllEquipment("bismuth_bronze")
                    .complexAllEquipment("black_bronze")
                    .complexAllEquipment("black_steel")
                    .complexAllEquipment("blue_steel")
                    .complexAllEquipment("bronze")
                    .complexAllEquipment("copper")
                    .complexAllEquipment("red_steel")
                    .complexAllEquipment("steel")
                    .complexAllEquipment("wrought_iron")
                    .complexAllEquipment("purple_steel")
                    .build();

            modCompats.forEach((armor, info) ->
                    DetailArmorBarAPI.customArmorBarBuilder().armor(armor).render((stack) -> new ArmorBarRenderManager(
                            info.getLocation(),
                            info.getSize(),
                            info.getSize(),
                            new TextureOffset(info.getFullOffset().getKey(), info.getFullOffset().getValue()),
                            new TextureOffset(info.getHalfOffset().getKey(), info.getHalfOffset().getValue()),
                            new TextureOffset(info.getFullOutlineOffset().getKey(), info.getFullOutlineOffset().getValue()),
                            new TextureOffset(info.getHalfOutlineOffset().getKey(), info.getHalfOutlineOffset().getValue())
                    )).register());
        });
    }
    public static ArmorItem getArmor(String type, String metal) {
        // 将字符串路径转换为ResourceLocation
        ResourceLocation itemLocation;
        itemLocation = ResourceLocation.tryParse("tfc:metal/" + type + "/" + metal);
        if(Objects.equals(metal, "purple_steel")) itemLocation = ResourceLocation.tryParse("rosia:purple_steel_" + type);
        if (itemLocation == null) {
            throw new IllegalArgumentException("无效的物品路径: " + metal);
        }

        // 从物品注册表中获取Item对象
        Item item = ForgeRegistries.ITEMS.getValue(itemLocation);

        if (item == null) {
            throw new IllegalArgumentException("未找到对应物品: " + metal);
        }

        return (ArmorItem) item;
    }

    public static ArmorItem[] getArmorForMetal(String metal) {
        ArmorItem[] armorItems = new ArmorItem[4];
        if (getArmor("helmet", metal) != Items.AIR){
            armorItems[0] = getArmor("helmet", metal);
            armorItems[1] = getArmor("chestplate", metal);
            armorItems[2] = getArmor("greaves", metal);
            armorItems[3] = getArmor("boots", metal);
        }
        return armorItems;
    }

    private static ResourceLocation textureId(String metal) {
        String path;
        path = "tfc/" + metal;
        if(metal.equals("purple_steel")) path = "rosia/purple_steel";
        return new ResourceLocation(MODID, "textures/gui/sprites/hud/" + path + ".png");
    }

    public static class CompatBuilder {

        private final Map<ArmorItem[], CompatInfo> registered;


        public CompatBuilder() {
            this.registered = new HashMap<>();
        }

        public CompatBuilder complexAllEquipment(String metal) {
            complexCompat(builder -> builder.setLocation(textureId(metal)), metal, getArmorForMetal(metal));
            return this;
        }
        public void complexCompat(Function<CompatInfo.Builder, CompatInfo.Builder> buildFunction, String name, ArmorItem[] armorItems) {
            registered.put(armorItems, buildFunction.apply(new CompatInfo.Builder(textureId(name))).build());
        }


        public Map<ArmorItem[], CompatInfo> build() {
            return registered;
        }
    }

    public static class CompatInfo {
        public static final int DEFAULT_SIZE = 18;

        private final ResourceLocation location;
        private final int size;
        private final Map.Entry<Integer,Integer> fullOffset;
        private final Map.Entry<Integer,Integer> halfOffset;
        private final Map.Entry<Integer,Integer> fullOutlineOffset;
        private final Map.Entry<Integer,Integer> halfOutlineOffset;

        private CompatInfo(Builder builder) {
            this.location = builder.location;
            this.size = builder.size;
            this.fullOffset = builder.fullOffset;
            this.halfOffset = builder.halfOffset;
            this.fullOutlineOffset = builder.fullOutlineOffset;
            this.halfOutlineOffset = builder.halfOutlineOffset;
        }

        public ResourceLocation getLocation() {
            return location;
        }

        public int getSize() {
            return size;
        }

        public Map.Entry<Integer, Integer> getFullOffset() {
            return fullOffset;
        }

        public Map.Entry<Integer, Integer> getHalfOffset() {
            return halfOffset;
        }

        public Map.Entry<Integer, Integer> getFullOutlineOffset() {
            return fullOutlineOffset;
        }

        public Map.Entry<Integer, Integer> getHalfOutlineOffset() {
            return halfOutlineOffset;
        }

        public static class Builder {
            ResourceLocation location;
            int size;
            Map.Entry<Integer,Integer> fullOffset;
            Map.Entry<Integer,Integer> halfOffset;
            Map.Entry<Integer,Integer> fullOutlineOffset;
            Map.Entry<Integer,Integer> halfOutlineOffset;

            public Builder(ResourceLocation location) {
                this.location = location;
                this.size = DEFAULT_SIZE;
                this.fullOffset = Map.entry(9,0);
                this.halfOffset = Map.entry(0,0);
                this.fullOutlineOffset = Map.entry(9,9);
                this.halfOutlineOffset = Map.entry(0,9);
            }

            public Builder setLocation(ResourceLocation location) {
                this.location = location;
                return this;
            }

            public Builder setSize(int size) {
                this.size = size;
                return this;
            }

            public Builder setFullOffset(int x, int y) {
                this.fullOffset = Map.entry(x,y);
                return this;
            }

            public Builder setHalfOffset(int x, int y) {
                this.halfOffset = Map.entry(x,y); // 修复：赋值给halfOffset
                return this;
            }

            public Builder setFullOutlineOffset(int x, int y) {
                this.fullOutlineOffset = Map.entry(x,y); // 修复：赋值给fullOutlineOffset
                return this;
            }

            public Builder setHalfOutlineOffset(int x, int y) {
                this.halfOutlineOffset = Map.entry(x,y); // 修复：赋值给halfOutlineOffset
                return this;
            }

            public CompatInfo build() {
                return new CompatInfo(this);
            }
        }
    }
}
