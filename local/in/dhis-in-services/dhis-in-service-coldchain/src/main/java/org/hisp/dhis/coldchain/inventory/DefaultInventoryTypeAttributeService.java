package org.hisp.dhis.coldchain.inventory;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultInventoryTypeAttributeService implements InventoryTypeAttributeService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private InventoryTypeAttributeStore inventoryTypeAttributeStore;

    public void setInventoryTypeAttributeStore( InventoryTypeAttributeStore inventoryTypeAttributeStore )
    {
        this.inventoryTypeAttributeStore = inventoryTypeAttributeStore;
    }

    // -------------------------------------------------------------------------
    // InventoryTypeAttribute
    // -------------------------------------------------------------------------
    
    @Override
    public int addInventoryTypeAttribute( InventoryTypeAttribute inventoryTypeAttribute )
    {
        return inventoryTypeAttributeStore.addInventoryTypeAttribute( inventoryTypeAttribute );
    }
    
    @Override
    public void deleteInventoryTypeAttribute( InventoryTypeAttribute inventoryTypeAttribute )
    {
        inventoryTypeAttributeStore.deleteInventoryTypeAttribute( inventoryTypeAttribute );
    }
    
    @Override
    public Collection<InventoryTypeAttribute> getAllInventoryTypeAttributes()
    {
        return inventoryTypeAttributeStore.getAllInventoryTypeAttributes();
    }
    
    @Override
    public void updateInventoryTypeAttribute( InventoryTypeAttribute inventoryTypeAttribute )
    {
        inventoryTypeAttributeStore.updateInventoryTypeAttribute( inventoryTypeAttribute );
    }
    
    public InventoryTypeAttribute getInventoryTypeAttribute( int id )
    {
        return inventoryTypeAttributeStore.getInventoryTypeAttribute( id );
    }
    
    public  InventoryTypeAttribute getInventoryTypeAttributeByName( String name )
    {
        return inventoryTypeAttributeStore.getInventoryTypeAttributeByName( name );
    }
    
}
