package org.hisp.dhis.dashboard.maintenance.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.hisp.dhis.dashboard.util.DBConnection;
import org.hisp.dhis.dashboard.util.DashBoardService;

import com.opensymphony.xwork2.Action;

public class StreamMySqlBackupAction implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DashBoardService dashBoardService;

    public void setDashBoardService( DashBoardService dashBoardService )
    {
        this.dashBoardService = dashBoardService;
    }

    private DBConnection dbConnection;
    
    public void setDbConnection( DBConnection dbConnection )
    {
        this.dbConnection = dbConnection;
    }

    // -------------------------------------------------------------------------
    // Input and Output Parameters
    // -------------------------------------------------------------------------

    private InputStream inputStream;

    public InputStream getInputStream()
    {
        return inputStream;
    }
    
    private String fileName;

    public String getFileName()
    {
        return fileName;
    }
    
    private String backupFilePath;
    
    public void setBackupFilePath( String backupFilePath )
    {
        this.backupFilePath = backupFilePath;
    }
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        
        fileName = "dhis2.zip";
        
        byte data[] = new byte[1024];
        
        String zipFilePath = backupFilePath.substring( 0, backupFilePath.lastIndexOf( "/" ) );
        
        zipFilePath += "/dhis2.zip";
        
        try
        {
            FileOutputStream zipFOS = new FileOutputStream( zipFilePath );
    
            ZipOutputStream out = new ZipOutputStream( new BufferedOutputStream(zipFOS) );
    
            FileInputStream fis = new FileInputStream( backupFilePath );
            
            BufferedInputStream bis = new BufferedInputStream( fis, 1024);
            
            ZipEntry entry = new ZipEntry( "dhis2.sql" );
            
            out.putNextEntry( entry );
            
            int count;
            while( (count = bis.read( data, 0, 1024 )) != -1 )
            {
                out.write( data, 0, count );
            }
            
            out.close();
            bis.close();
            
            System.out.println("zipFilePath : "+ zipFilePath );
        
            /*
            PipedOutputStream out = new PipedOutputStream();
            
            PipedInputStream in = new PipedInputStream( out );
            
            ZipOutputStream zipOut = new ZipOutputStream( out );
            
            zipOut.putNextEntry( new ZipEntry( fileName ) );
             */
            
            inputStream = new BufferedInputStream( new FileInputStream(zipFilePath) , 1024 );
        }
        catch( Exception e)
        {
            System.out.println( e.getMessage() );
        }
        
        return SUCCESS;
    }
}
