package org.hisp.dhis.web.api.resources;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Required;

import com.sun.jersey.api.view.ImplicitProduces;

@Path( "/" )
@ImplicitProduces( MediaType.TEXT_HTML )
public class ApiResource
{
    private VelocityManager velocityManager;

    @GET
    @Produces( MediaType.TEXT_HTML )
    public String getDescription()
        throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, IOException, Exception
    {
        return velocityManager.render( null, "index" );
    }
    
    @Required
    public void setVelocityManager( VelocityManager velocityManager )
    {
        this.velocityManager = velocityManager;
    }
}
