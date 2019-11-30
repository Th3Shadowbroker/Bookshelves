package org.inventivetalent.bookshelves;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Optional;

public class HopperListener implements Listener {

    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event) {

        // Holder
        InventoryHolder holder = event.getDestination().getHolder();

        // Ensure it's a hopper
        if (holder instanceof Hopper) {
            Hopper hopper = (Hopper) holder;
            Block facedBlock = getFacedBlock(hopper);

            // Check lock state
            if (hopper.isLocked()) { return; }

            // Check material
            if (facedBlock.getType() != Material.BOOKSHELF) { return; }

            // Check the faced block is a bookshelf
            Optional<Inventory> facedBlockInventory = Optional.ofNullable(Bookshelves.instance.getShelf(facedBlock));
            if (!facedBlockInventory.isPresent()) { return; }

            // Ensure item is valid
            if (!Bookshelves.instance.isValidBook(event.getItem()));

            // Perform safe item transfer from hopper to shelf
            facedBlockInventory.ifPresent(
                    shelf -> {
                        Optional<Hopper> outputHopper = getOutputHopper(facedBlock);
                        System.out.println(facedBlock.getType().name());

                        event.getSource().remove(event.getItem());

                        // Used to prevent items from vanishing/duplicating
                        boolean success;

                        // Check if the shelf acts as conveyor
                        if (!outputHopper.isPresent()) {
                            success = shelf.addItem(event.getItem()).isEmpty();
                        } else {
                            // Push to connected output-hopper or the shelf if the output-hopper is full/locked
                            boolean hopperCanReceive = !outputHopper.get().isLocked() && outputHopper.get().getInventory().firstEmpty() > -1;
                            success = hopperCanReceive ? outputHopper.get().getInventory().addItem(event.getItem()).isEmpty() : shelf.addItem(event.getItem()).isEmpty();
                        }

                        event.setCancelled(success);
                    }
            );
        }
    }

    public Optional<Hopper> getOutputHopper(Block block) {
        Block relative = block.getRelative(BlockFace.DOWN);
        return relative.getType() == Material.HOPPER ? Optional.of((Hopper) relative.getState()) : Optional.empty();
    }

    private Block getFacedBlock(Hopper hopper) {
        BlockFace blockFace = ((org.bukkit.block.data.type.Hopper) hopper.getBlockData()).getFacing();
        return hopper.getBlock().getRelative(blockFace);
    }

}
