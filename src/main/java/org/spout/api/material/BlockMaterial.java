/*
 * This file is part of SpoutAPI.
 *
 * Copyright (c) 2011-2012, SpoutDev <http://www.spout.org/>
 * SpoutAPI is licensed under the SpoutDev License Version 1.
 *
 * SpoutAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * SpoutAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spout.api.material;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;

import org.spout.api.entity.Entity;
import org.spout.api.entity.component.controller.BlockController;
import org.spout.api.entity.component.controller.type.ControllerType;
import org.spout.api.event.player.PlayerInteractEvent.Action;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.material.basic.BasicAir;
import org.spout.api.material.basic.BasicSkyBox;
import org.spout.api.material.block.BlockFace;
import org.spout.api.material.block.BlockFaces;
import org.spout.api.material.range.EffectRange;
import org.spout.api.math.MathHelper;
import org.spout.api.util.flag.ByteFlagContainer;

public class BlockMaterial extends Material implements Placeable {

	public static final BlockMaterial AIR = new BasicAir();
	public static final BlockMaterial SOLID = new BlockMaterial("solid").setHardness(1.f);
	public static final BlockMaterial UNBREAKABLE = new BlockMaterial("Unbreakable").setHardness(100.f);
	public static final BlockMaterial SKYBOX = new BasicSkyBox();
	public static final BlockMaterial ERROR = new BlockMaterial("Missing Plugin").setHardness((100.f));
	private static CollisionShape shape = new BoxShape(1f, 1f, 1f);

	public BlockMaterial(String name) {
		super(name);
	}
	
	public BlockMaterial(short dataMask, String name) {
		super(dataMask, name);
	}

	public BlockMaterial(String name, int data, Material parent) {
		super(name, data, parent);
	}
	
	protected BlockMaterial(String name, short id) {
		super(name, id);
	}
	
	protected BlockMaterial(short dataMask, String name, short id) {
		super(dataMask, name, id);
	}

	/**
	 * Gets the block at the given id, or null if none found
	 * 
	 * @param id to get
	 * @return block, or null if none found
	 */
	public static BlockMaterial get(short id) {
		Material mat = Material.get(id);
		if (!(mat instanceof BlockMaterial)) {
			return null;
		}

		return (BlockMaterial) mat;

	}

	/**
	 * Gets the associated block material with it's name. Case-insensitive.
	 * 
	 * @param name to lookup
	 * @return material, or null if none found
	 */
	public static BlockMaterial get(String name) {
		Material mat = Material.get(name);
		if (!(mat instanceof BlockMaterial)) {
			return null;
		}

		return (BlockMaterial) mat;
	}

	private ByteFlagContainer occlusion = new ByteFlagContainer(BlockFaces.NESWBT);
	private float hardness = 0F;
	private float friction = 0F;
	private byte opacity = 0xF;
	private ControllerType controller = null;

	/**
	 * Sets the block controller associated with this material<br>
	 * Future calls to getController will return an instance of this block controller.
	 * 
	 * @param controller type to set to
	 * @return This block material
	 */
	public BlockMaterial setController(ControllerType controller) {
		this.controller = controller;
		return this;
	}

	/**
	 * Gets whether this Block Material has a Block Controller associated with it.
	 * @return True if it has a block controller
	 */
	public boolean hasController() {
		return this.controller != null;
	}

	/**
	 * Gets the block controller associated with this material from a block<br>
	 * If the block controller set is null or does not match, a new instance is created and set on the block and returned
	 * 
	 * @param block to get the Block Controller of
	 * @return The Block Controller
	 */
	public BlockController getController(Block block) {
		return getController(block, true);
	}

	/**
	 * Gets the block controller associated with this material from a block<br>
	 * If the block controller set is null or does not match, null is returned<br>
	 * If forced is set True, the controller is forcibly replaced with the one set in this material and is returned
	 * 
	 * @param block to get the Block Controller of
	 * @param forced whether to force-convert the controller if not found or invalid
	 * @return The Block Controller
	 */
	public BlockController getController(Block block, boolean forced) {
		if (this.controller == null) {
			throw new IllegalStateException("Can not obtain the block controller because no controller type is set for this material.");
		}
		BlockController controller = block.getController();
		if (controller != null) {
			Class<?> clazz = this.controller.getControllerClass();
			if (clazz.isAssignableFrom(controller.getClass())) {
				return controller;
			}
		}

		if (!forced) {
			return null;
		}

		controller = (BlockController) this.controller.createController();
		block.setController(controller);
		return controller;
	}

	@Override
	public BlockMaterial getSubMaterial(short data) {
		return (BlockMaterial) super.getSubMaterial(data);
	}

	@Override
	public CollisionShape getCollision() {
		return shape;
	}

	@Override
	public void setCollision(CollisionShape shape) {
		this.shape = shape;
	}

	/**
	 * Gets the friction of this block
	 * 
	 * @return friction value
	 */
	public float getFriction() {
		return this.friction;
	}

	/**
	 * Sets the friction of this block
	 * 
	 * @param slip friction value
	 * @return this material
	 */
	public BlockMaterial setFriction(float slip) {
		this.friction = slip;
		return this;
	}

	/**
	 * Gets the hardness of this block
	 * 
	 * @return hardness value
	 */
	public float getHardness() {
		return this.hardness;
	}

	/**
	 * Sets the hardness of this block
	 * 
	 * @param hardness hardness value
	 * @return this material
	 */
	public BlockMaterial setHardness(float hardness) {
		this.hardness = hardness;
		return this;
	}

	/**
	 * Gets the amount of light this block emits
	 * 
	 * @return light level
	 */
	public byte getLightLevel(short data) {
		return 0;
	}

	/**
	 * Gets the amount of light blocked by this block.
	 * 
	 * 0xF (15) represents a fully opaque block.
	 * 
	 * @return opacity
	 */
	public byte getOpacity() {
		return this.opacity;
	}

	/**
	 * Returns true if the block is opaque, false if not.
	 * @return True if opacity is 15, false if less than.
	 */
	public boolean isOpaque() {
		return this.opacity == 0xF;
	}

	/**
	 * Sets the amount of light blocked by this block.
	 * 
	 * 0xF (15) represents a fully opaque block.
	 * 
	 * @param level of opacity, a value from 0 to 15
	 * @return this material
	 */
	public BlockMaterial setOpacity(int level) {
		this.opacity = (byte) MathHelper.clamp(level, 0, 15);
		return this;
	}

	/**
	 * Turns this Block Material in a fully opaque block, not letting light through from any side<br>
	 * Sets opacity to 15 and sets occlusion to all faces
	 * 
	 * @return this Block Material
	 */
	public BlockMaterial setOpaque() {
		return this.setOpacity(15).setOcclusion(BlockFaces.NESWBT);
	}

	/**
	 * Turns this Block Material in a fully transparent block, letting light through from all sides<br>
	 * Sets the opacity to 0 and sets occlusion to none
	 * 
	 * @return this Block Material
	 */
	public BlockMaterial setTransparent() {
		return this.setOpacity(0).setOcclusion(BlockFaces.NONE);
	}

	/**
	 * True if this block acts as an obstacle when placing a block on it false
	 * if not.
	 * 
	 * If the block is not an obstacle, placement will replace this block.
	 * 
	 * @return if this block acts as a placement obstacle
	 */
	public boolean isPlacementObstacle() {
		return true;
	}
	
	/**
	 * True if this block requires physic updates when a neighbor block changes,
	 * false if not.
	 * 
	 * @return if this block requires physics updates
	 */
	public boolean hasPhysics() {
		return false;
	}

	/**
	 * Called when a block near to this material is changed.<br>
	 * 
	 * @param oldMaterial the previous material, or null if the update was not due to a material change
	 * @param block that got updated
	 * @return true if the block was updated
	 */
	public void onUpdate(BlockMaterial oldMaterial, Block block) {
	}
	
	/**
	 * Returns the maximum range of effect for physics updates to this material.  
	 * This is triggered when the material is set to this material, or when the 
	 * data is changed.
	 * <br>
	 * When triggered, all blocks in this range are queued for updating.
	 * 
	 * @return the maximum range of updates
	 */
	public EffectRange getPhysicsRange(short data) {
		return EffectRange.THIS_AND_NEIGHBORS;
	}
	
	/**
	 * Returns the maximum range of effect for physics updates to this material, when 
	 * it is replaced by another material.  This is triggered when the material or 
	 * sub-material (data & dataMask) is changed.
	 * <br>
	 * When triggered, all blocks in this range are queued for updating.
	 * 
	 * @param data the data the block had when destroyed
	 * @return the maximum range of updates
	 */
	public EffectRange getDestroyRange(short data) {
		return getPhysicsRange(data);
	}
	
	/**
	 * Gets the maximum distance of any physics effect.  This is used to determine if the update can be handled by 
	 * the region thread and not to determine if any blocks should be updated.
	 * 
	 * @param data the data of the material
	 * @return the maximum range of updates
	 */
	public EffectRange getMaximumPhysicsRange(short data) {
		return getPhysicsRange(data);
	}

	/**
	 * Called when this block has been destroyed.
	 * 
	 * @param block that got destroyed
	 */
	public void onDestroy(Block block) {
		block.setMaterial(AIR);
		if (this.hasController()) {
			block.setController(null);
		}
	}
	
	/**
	 * True if this block is a solid block
	 * false if not.
	 * 
	 * @return if this block has collision
	 */
	public boolean isSolid() {
		return true; //TODO this
	}

	/**
	 * Gets the occluded faces of this Block Material<br>
	 * Occluded faces do not let light though and require rendering behind it at those faces
	 * @return the occluded faces
	 */
	public ByteFlagContainer getOcclusion() {
		return this.occlusion;
	}

	/**
	 * Sets the occludes faces of this Block Material
	 * @param faces to make this Block Material occlude
	 * @return this Block Material
	 */
	public BlockMaterial setOcclusion(BlockFaces faces) {
		this.occlusion.set(faces);
		return this;
	}

	@Override
	public boolean canPlace(Block block, short data, BlockFace against, boolean isClickedBlock) {
		return true;
	}

	@Override
	public boolean onPlacement(Block block, short data, BlockFace against, boolean isClickedBlock) {
		block.setMaterial(this, data);
		return true;
	}

	/**
	 * Called when an entity interacts with this block material in the world
	 *
	 * @param entity that is interacting with this material
	 * @param type of interaction
	 * @param clickedFace of the material clicked
	 */
	public void onInteractBy(Entity entity, Block block, Action type, BlockFace clickedFace) {
	}

	/**
	 * Returns true if the block is transparent, false if not.
	 * @return True if opacity is 0, false if more than.
	 */
	public boolean isTransparent() {
		return this.opacity == 0;
	}
	
	/**
	 * Called by the dynamic block update system.  If a material is changed into a
	 * material that it is not compatible with, then this will automatically trigger
	 * a block reset.
	 * 
	 * @param m the other material
	 * @return true if the two materials are compatible
	 */
	public boolean isCompatibleWith(BlockMaterial m) {
		return (m.getId() == getId() && ((m.getData() ^ getData()) & getDataMask()) == 0);
	}
}
