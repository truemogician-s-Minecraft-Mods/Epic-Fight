package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Models;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.brain.BrainRemodeler;
import yesman.epicfight.world.entity.ai.brain.task.AnimatedCombatBehavior;
import yesman.epicfight.world.entity.ai.brain.task.MoveToTargetSinkStopInaction;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class PiglinBrutePatch extends HumanoidMobPatch<PiglinBrute> {
	public PiglinBrutePatch() {
		super(Faction.PIGLINS);
	}
	
	@Override
	protected void initAttributes() {
		super.initAttributes();
		this.original.getAttribute(EpicFightAttributes.STUN_ARMOR.get()).setBaseValue(8.0F);
		this.original.getAttribute(EpicFightAttributes.IMPACT.get()).setBaseValue(3.0F);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void initAnimator(ClientAnimator clientAnimator) {
		clientAnimator.addLivingAnimation(LivingMotion.IDLE, Animations.PIGLIN_IDLE);
		clientAnimator.addLivingAnimation(LivingMotion.WALK, Animations.PIGLIN_WALK);
		clientAnimator.addLivingAnimation(LivingMotion.FALL, Animations.BIPED_FALL);
		clientAnimator.addLivingAnimation(LivingMotion.MOUNT, Animations.BIPED_MOUNT);
		clientAnimator.addLivingAnimation(LivingMotion.DEATH, Animations.PIGLIN_DEATH);
		clientAnimator.setCurrentMotionsAsDefault();
	}
	
	@Override
	public void updateMotion(boolean considerInaction) {
		super.commonMobUpdateMotion(considerInaction);
	}
	
	@Override
	public <M extends Model> M getEntityModel(Models<M> modelDB) {
		return modelDB.piglin;
	}
	
	@Override
	public void setAIAsInfantry(boolean holdingRanedWeapon) {
		CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();
		
		if (builder != null) {
			BrainRemodeler.replaceBehavior(this.original.getBrain(), Activity.FIGHT, 12, MeleeAttack.class, new AnimatedCombatBehavior<>(this, builder.build(this)));
		}
		
		BrainRemodeler.replaceBehavior(this.original.getBrain(), Activity.CORE, 1, MoveToTargetSink.class, new MoveToTargetSinkStopInaction());
	}
	
	@Override
	public void setAIAsMounted(Entity ridingEntity) {
		
	}
	
	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		return super.getModelMatrix(partialTicks).scale(1.1F, 1.1F, 1.1F);
	}
}