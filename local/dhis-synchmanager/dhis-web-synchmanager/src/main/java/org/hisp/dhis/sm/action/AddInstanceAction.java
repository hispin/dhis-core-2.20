package org.hisp.dhis.sm.action;

import org.hisp.dhis.dxf2.sm.api.SecretKeyGenerator;
import org.hisp.dhis.dxf2.sm.api.SynchInstance;
import org.hisp.dhis.dxf2.sm.api.SynchInstanceService;

import com.opensymphony.xwork2.Action;

public class AddInstanceAction implements Action
{
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SynchInstanceService synchInstanceService;

    public void setSynchInstanceService( SynchInstanceService synchInstanceService )
    {
        this.synchInstanceService = synchInstanceService;
    }
    
    /*
    private MigrationPasswordManager passwordManager;

    public void setPasswordManager( MigrationPasswordManager passwordManager )
    {
        this.passwordManager = passwordManager;
    }
    */
    
    // -------------------------------------------------------------------------
    // Setters & Getters
    // -------------------------------------------------------------------------
    
    private String synchType;
    
    public String getSynchType()
    {
        return synchType;
    }

    public void setSynchType( String synchType )
    {
        this.synchType = synchType;
    }
    
    private String name;
    
    public void setName( String name )
    {
        this.name = name;
    }

    private String url;

    public void setUrl( String url )
    {
        this.url = url;
    }

    private String userId;

    public void setUserId( String userId )
    {
        this.userId = userId;
    }

    private String password;

    public void setPassword( String password )
    {
        this.password = password;
    }
    
    private Integer instanceId;
    
    public void setInstanceId( Integer instanceId )
    {
        this.instanceId = instanceId;
    }
    
    private String instanceType;
    
    public void setInstanceType( String instanceType )
    {
        this.instanceType = instanceType;
    }
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    public String execute()
    {
        SynchInstance instance = new SynchInstance();
        
        
        
        if( instanceId != null )
        {
            instance = synchInstanceService.getInstance( instanceId );
        }   
        
        instance.setName( name );
        instance.setUrl( url );
        instance.setUserId( userId );
        //instance.setPassword( password );
        instance.setSynchType( synchType );
        instance.setType( instanceType );
        //String enCodePassword = passwordManager.encode( password );
        
        //instance.setPassword( enCodePassword );
        
        String enCodePassword = null;
        try
        {
            //enCodePassword = encrypt( password );
            SecretKeyGenerator secretKeyGenerator = new SecretKeyGenerator();
            enCodePassword = secretKeyGenerator.encrypt( password );
            
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        //System.out.println( "Incode Password is" + " : " + enCodePassword );
        
        instance.setPassword( enCodePassword );
        
        if( instanceId != null )
        {
            synchInstanceService.updateInstance( instance );
        }
        else
        {
            synchInstanceService.addInstance( instance );
        }
        
        return SUCCESS;
    }   
}
