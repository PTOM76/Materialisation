package me.shedaniel.materialisation.blocks;

import com.mojang.serialization.MapCodec;
import me.shedaniel.materialisation.gui.MaterialPreparerScreenHandler;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class MaterialPreparerBlock extends HorizontalFacingBlock implements NamedScreenHandlerFactory {
    private static final Text TITLE = Text.translatable("block.materialisation.material_preparer");
    private static final VoxelShape SHAPE;
    
    static {
        VoxelShape base = Block.createCuboidShape(0, 14, 0, 16, 16, 16);
        SHAPE = VoxelShapes.union(base, Block.createCuboidShape(0, 0, 0, 2, 14, 2), Block.createCuboidShape(0, 0, 14, 2, 14, 16), Block.createCuboidShape(14, 0, 14, 16, 14, 16), Block.createCuboidShape(14, 0, 0, 16, 14, 2));
    }
    
    public MaterialPreparerBlock() {
        this(AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).burnable().strength(2.5F, 3).sounds(BlockSoundGroup.WOOD));
    }

    public MaterialPreparerBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    public static final MapCodec<MaterialPreparerBlock> CODEC = createCodec(MaterialPreparerBlock::new);

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
    
    @Override
    public BlockState getPlacementState(ItemPlacementContext placementContext) {
        return getDefaultState().with(FACING, placementContext.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            player.incrementStat(Stats.INTERACT_WITH_LOOM);
            return ActionResult.CONSUME;
        }
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> new MaterialPreparerScreenHandler(i, playerInventory, ScreenHandlerContext.create(world, pos)), TITLE);
    }
    
    @Override
    public BlockRenderType getRenderType(BlockState blockState_1) {
        return BlockRenderType.MODEL;
    }
    
    @Override
    public boolean hasSidedTransparency(BlockState blockState_1) {
        return true;
    }
    
    @Override
    public boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new MaterialPreparerScreenHandler(syncId, inv);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getDefaultState().getBlock().getTranslationKey());
    }
}
