package me.shedaniel.materialisation.blocks;

import com.mojang.serialization.MapCodec;
import me.shedaniel.materialisation.gui.MaterialisingTableScreenHandler;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.BlockSoundGroup;
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

public class MaterialisingTableBlock extends HorizontalFacingBlock {
    private static final Text TITLE = Text.translatable("block.materialisation.materialising_table");
    private static final VoxelShape SHAPE;

    static {
        VoxelShape base = Block.createCuboidShape(0, 0, 0, 16, 7, 16);
        SHAPE = VoxelShapes.union(base, Block.createCuboidShape(1, 7, 1, 15, 9, 15), Block.createCuboidShape(5, 9, 5, 11, 16, 11));
    }
    
    public MaterialisingTableBlock() {
        this(AbstractBlock.Settings.create().mapColor(MapColor.WHITE).strength(5.0F, 1200.0F).sounds(BlockSoundGroup.METAL));
    }

    public MaterialisingTableBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    public static final MapCodec<MaterialisingTableBlock> CODEC = createCodec(MaterialisingTableBlock::new);

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
            player.openHandledScreen(this.createScreenHandlerFactory(state, world, pos));
            return ActionResult.CONSUME;
        }
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> {
            return new MaterialisingTableScreenHandler(i, playerInventory, ScreenHandlerContext.create(world, pos));
        }, TITLE);
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
}
