package me.jishunamatata.fakeblocks;

import net.minecraft.server.v1_16_R3.BlockPosition;

public class BlockAnimationContext {

	private BlockPosition blockPos;
	private int stage = -1;

	public BlockAnimationContext(BlockPosition pos) {
		this.blockPos = pos;
	}

	public BlockPosition getBlockPos() {
		return blockPos;
	}

	public int getStrage() {
		return stage;
	}

	public void setStrage(int stage) {
		this.stage = stage;
	}

}
