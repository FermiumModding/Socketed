package socketed.common.attributes;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.village.MerchantTradeOffersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class AttributeHandler {
    @SubscribeEvent
    public static void onEntityConstruction(EntityEvent.EntityConstructing event) {
        if(event.getEntity() instanceof EntityPlayer) {
            ((EntityPlayer)event.getEntity()).getAttributeMap().registerAttribute(SocketedAttributes.DURABILITY);
            ((EntityPlayer)event.getEntity()).getAttributeMap().registerAttribute(SocketedAttributes.XP);
            ((EntityPlayer)event.getEntity()).getAttributeMap().registerAttribute(SocketedAttributes.TRADECOST);
        }
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event){
        EntityPlayer player = event.getAttackingPlayer();
        if(player == null) return;
        EntityLivingBase victim = event.getEntityLiving();
        if(victim == null) return;
        double amount = event.getDroppedExperience();
        if(amount <= 0) return;

        IAttributeInstance xpAttribute = player.getEntityAttribute(SocketedAttributes.XP);
        if(xpAttribute == null) return;
        xpAttribute.setBaseValue(amount);
        event.setDroppedExperience((int) xpAttribute.getAttributeValue());
        xpAttribute.setBaseValue(0);
    }

    @SubscribeEvent
    public static void onTradeOffers(MerchantTradeOffersEvent event){
        EntityPlayer player = event.getPlayer();
        if(player == null) return;
        if(player.world.isRemote) return; //why does this even run on client every single tick breh
        MerchantRecipeList trades = event.getList();
        if (trades == null) return;

        IAttributeInstance tradeAttribute = player.getEntityAttribute(SocketedAttributes.TRADECOST);
        if(tradeAttribute == null || tradeAttribute.getModifiers().isEmpty()) return;

        for (int i = 0; i < trades.size(); i++) {
            MerchantRecipe recipe = trades.get(i);
            if(recipe.getItemToBuy().getCount() > 1)
                recipe = reduceCost(recipe, tradeAttribute, true);
            else if(recipe.hasSecondItemToBuy() && recipe.getSecondItemToBuy().getCount() > 1)
                recipe = reduceCost(recipe, tradeAttribute, false);
            trades.set(i, recipe);
        }
    }

    private static MerchantRecipe reduceCost(MerchantRecipe recipe, IAttributeInstance tradeAttribute, boolean targetsFirstItem){
        MerchantRecipe newRecipe = recipe;

        ItemStack itemToBuy = getStack(recipe, targetsFirstItem);

        tradeAttribute.setBaseValue(itemToBuy.getCount());
        int newCount = MathHelper.clamp((int) Math.round(tradeAttribute.getAttributeValue()), 1, itemToBuy.getMaxStackSize());

        if (newCount != itemToBuy.getCount()) {
            newRecipe = new MerchantRecipe(recipe.writeToTags()); //copy to not modify the original trade
            getStack(newRecipe, targetsFirstItem).setCount(newCount); //modify count
        }
        tradeAttribute.setBaseValue(0); //reset attribute, technically not needed
        return newRecipe;
    }

    private static ItemStack getStack(MerchantRecipe recipe, boolean targetsFirstItem){
        return targetsFirstItem ? recipe.getItemToBuy() : recipe.getSecondItemToBuy();
    }
}
