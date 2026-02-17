package socketed.client.handlers;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import socketed.api.socket.gem.effect.slot.ISlotType;
import socketed.api.common.capabilities.socketable.CapabilitySocketableHandler;
import socketed.api.common.capabilities.socketable.ICapabilitySocketable;
import socketed.api.socket.gem.GemCombinationInstance;
import socketed.api.socket.gem.GemCombinationType;
import socketed.api.socket.gem.GemInstance;
import socketed.api.socket.gem.GemType;
import socketed.api.socket.gem.effect.GenericGemEffect;
import socketed.api.util.SocketedUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class TooltipHandler {

    private static int toolTipIndex = -1;
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if(stack.isEmpty()) return;
        
        if(!SocketedUtil.hasCompletedLoading(true)) return;
        
        ICapabilitySocketable sockets = stack.getCapability(CapabilitySocketableHandler.CAP_SOCKETABLE, null);
        GemType gemType = null;
        if(sockets == null) gemType = SocketedUtil.getGemTypeFromItemStack(stack);
        if(sockets == null && gemType == null) return;

        List<String> tooltips = event.getToolTip();
        toolTipIndex = -1;
        ResourceLocation itemLoc = Item.REGISTRY.getNameForObject(event.getItemStack().getItem());
        if(itemLoc != null) {
            String itemName = itemLoc.toString();
            for(int i = tooltips.size() - 1; i >= 0; i--) {
                if(tooltips.get(i).contains(itemName)) {
                    toolTipIndex = i;
                    break;
                }
            }
        }
        if(toolTipIndex > 0 && stack.isItemDamaged()) {
            //Shift one farther up to have durability below socket tooltips
            toolTipIndex--;
        }

        //Socketed item tooltip
        if(sockets != null) {
            //Sockets (x/y) Tooltip
            int socketCount = sockets.getSocketCount();
            if(socketCount <= 0) return;
            int gemCount = sockets.getGemCount();
            insertTooltip(tooltips, TextFormatting.BOLD + I18n.format("socketed.tooltip.socket", gemCount, socketCount) + TextFormatting.RESET);

            if(gemCount > 0) {
                if(!GuiScreen.isShiftKeyDown()) {
                    insertTooltip(tooltips, TextFormatting.BOLD + "" + TextFormatting.GOLD + I18n.format("socketed.tooltip.holdshift") + TextFormatting.RESET);
                }
                else {
                    //Gem Combinations
                    for(GemCombinationInstance combination : sockets.getGemCombinations()) {
                        GemCombinationType combinationType = combination.getGemCombinationType();
                        
                        //Name
                        insertTooltip(tooltips, " " + TextFormatting.ITALIC + combinationType.getColor() + I18n.format(combinationType.getDisplayName()) + TextFormatting.RESET);
                        
                        //Description Per Effect
                        for(GenericGemEffect effect : combination.getGemEffectsForStack(stack)) {
                            String effectTooltip = effect.getTooltip(true);
                            if(effectTooltip != null && !effectTooltip.isEmpty()) {
                                insertTooltip(tooltips, "  " + combinationType.getColor() + effectTooltip + TextFormatting.RESET);
                            }
                        }
                    }
                    //Gems
                    List<GemInstance> gemInstances = sockets.getAllGems(false);
                    if(!gemInstances.isEmpty()) {
                        Map<GemType, List<String>> mappedTips = new LinkedHashMap<>();
                        for(GemInstance gemInstance : gemInstances) {
                            gemType = gemInstance.getGemType();
                            List<String> tipsForType = mappedTips.get(gemType);
                            
                            //Name
                            if(tipsForType == null) {
                                tipsForType = new ArrayList<>();
                                tipsForType.add(" " + gemType.getColor() + I18n.format(gemType.getDisplayName()) + TextFormatting.RESET);
                            }
                            
                            //Description Per Effect
                            for(GenericGemEffect effect : gemInstance.getGemEffectsForStack(stack)) {
                                String effectTooltip = effect.getTooltip(true);
                                if(effectTooltip != null && !effectTooltip.isEmpty()) {
                                    tipsForType.add("  " + gemType.getColor() + effectTooltip + TextFormatting.RESET);
                                }
                            }
                            mappedTips.put(gemType, tipsForType);
                        }
                        //Reiterate tips to collect alike effects to reduce bloat
                        //TODO: more efficient way of doing this?
                        for(List<String> tipsForType : mappedTips.values()) {
                            for(String tip : tipsForType) {
                                insertTooltip(tooltips, tip);
                            }
                        }
                    }
                }
            }
        }
        //Gem tooltip
        else {
            //In socket Tooltip
            insertTooltip(tooltips, TextFormatting.BOLD + I18n.format("socketed.tooltip.gem") + TextFormatting.RESET);
            
            //Name
            insertTooltip(tooltips, " " + gemType.getColor() + I18n.format(gemType.getDisplayName()) + " " + I18n.format("socketed.tooltip.tier_" + gemType.getTier()) + TextFormatting.RESET);
            
            if(!GuiScreen.isShiftKeyDown()) {
                insertTooltip(tooltips, TextFormatting.BOLD + "" + TextFormatting.GOLD + I18n.format("socketed.tooltip.holdshift") + TextFormatting.RESET);
            }
            else {
                //Description Per Effect
                for(GenericGemEffect effect : gemType.getEffects()) {
                    String effectTooltip = effect.getTooltip(false);
                    if(effectTooltip != null && !effectTooltip.isEmpty()) {
                        insertTooltip(tooltips, "  " + gemType.getColor() + ISlotType.getSlotTooltip(effect.getSlotType()) + " " + effectTooltip + TextFormatting.RESET);
                    }
                }
            }
        }
    }

    private static void insertTooltip(List<String> tooltips, String tooltip) {
        if(toolTipIndex < 0 || toolTipIndex >= tooltips.size()) tooltips.add(tooltip);
        else {
            tooltips.add(toolTipIndex, tooltip);
            toolTipIndex++;
        }
    }
}