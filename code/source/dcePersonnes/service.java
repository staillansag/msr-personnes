package dcePersonnes;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
// --- <<IS-END-IMPORTS>> ---

public final class service

{
	// ---( internal utility methods )---

	final static service _instance = new service();

	static service _newInstance() { return new service(); }

	static service _cast(Object o) { return (service)o; }

	// ---( server methods )---




	public static final void ecritFichierCSV (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(ecritFichierCSV)>> ---
		// @sigtype java 3.5
		// [i] field:0:required chemin
		// [i] field:0:required nom
		// [i] field:0:required contenu
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	filePath = IDataUtil.getString( pipelineCursor, "chemin" );
		String	fileName = IDataUtil.getString( pipelineCursor, "nom" );
		String	content = IDataUtil.getString( pipelineCursor, "contenu" );
		pipelineCursor.destroy();
		// pipeline
		
		File directory = new File(filePath);
		
		if (!directory.exists()) {
		    if(directory.mkdirs()) {
		    } else {
		    	throw new ServiceException("Le r\u00E9pertoire ne peut pas \u00EAtre cr\u00E9\u00E9");
		    }
		}
		
		
		FileWriter fileWriter = null;
		
		try {
		    fileWriter = new FileWriter(filePath + "/" + fileName);
		    fileWriter.write(content);
		} catch (IOException e) {
		    throw new ServiceException(e);
		} finally {
		    try {
		        if (fileWriter != null) {
		            fileWriter.close();
		        }
		    } catch (IOException e) {
		    	throw new ServiceException(e);
		    }
		}
		// --- <<IS-END>> ---

                
	}



	public static final void supprimeRepertoire (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(supprimeRepertoire)>> ---
		// @sigtype java 3.5
		// [i] field:0:required chemin
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
			String	chemin = IDataUtil.getString( pipelineCursor, "chemin" );
		pipelineCursor.destroy();
		// pipeline
		
		String directoryPath = chemin;
		File dir = new File(directoryPath);
		
		if (dir.exists()) {
		    deleteRecursively(dir);
		} else {
		    throw new ServiceException("Le r\u00E9pertoire sp\u00E9cifi\u00E9 n'existe pas.");
		}
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void zippeRepertoire (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(zippeRepertoire)>> ---
		// @sigtype java 3.5
		// [i] field:0:required cheminRepertoire
		// [i] field:0:required cheminZip
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
			String	cheminRepertoire = IDataUtil.getString( pipelineCursor, "cheminRepertoire" );
			String	cheminZip = IDataUtil.getString( pipelineCursor, "cheminZip" );
		pipelineCursor.destroy();
		// pipeline
		
		String sourceDirPath = cheminRepertoire;
		String zipFilePath = cheminZip;
		
		try (FileOutputStream fos = new FileOutputStream(zipFilePath);
		     ZipOutputStream zos = new ZipOutputStream(fos)) {
		
		    File fileToZip = new File(sourceDirPath);
		    String fileName = fileToZip.getName();
		
		    if (fileToZip.isHidden()) {
		        throw new ServiceException("Le dossier source est cach\u00E9. Veuillez fournir un dossier source valide.");
		    }
		    
		    if (!fileToZip.isDirectory()) {
		    	throw new ServiceException("Le chemin source doit \u00EAtre un dossier. Veuillez fournir un dossier source valide.");
		    }
		    
		    if (fileName.endsWith("/")) {
		        zos.putNextEntry(new ZipEntry(fileName));
		        zos.closeEntry();
		    } else {
		        zos.putNextEntry(new ZipEntry(fileName + "/"));
		        zos.closeEntry();
		    }
		    
		    File[] children = fileToZip.listFiles();
		    if (children != null) {
		        for (File childFile : children) {
		            if (childFile.isDirectory()) {
		                zos.putNextEntry(new ZipEntry(fileName + "/" + childFile.getName() + "/"));
		                zos.closeEntry();
		                File[] grandChildren = childFile.listFiles();
		                if (grandChildren != null) {
		                    for (File grandChildFile : grandChildren) {
		                        try (FileInputStream fis = new FileInputStream(grandChildFile)) {
		                            ZipEntry zipEntry = new ZipEntry(fileName + "/" + childFile.getName() + "/" + grandChildFile.getName());
		                            zos.putNextEntry(zipEntry);
		                            byte[] bytes = new byte[1024];
		                            int length;
		                            while ((length = fis.read(bytes)) >= 0) {
		                                zos.write(bytes, 0, length);
		                            }
		                        }
		                    }
		                }
		            } else {
		                try (FileInputStream fis = new FileInputStream(childFile)) {
		                    ZipEntry zipEntry = new ZipEntry(fileName + "/" + childFile.getName());
		                    zos.putNextEntry(zipEntry);
		                    byte[] bytes = new byte[1024];
		                    int length;
		                    while ((length = fis.read(bytes)) >= 0) {
		                        zos.write(bytes, 0, length);
		                    }
		                }
		            }
		        }
		    }
		} catch (IOException e) {
		    throw new ServiceException(e);
		}
		
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	private static void deleteRecursively(File file) throws ServiceException {
	    if (!file.exists()) return;
	
	    if (file.isDirectory()) {
	        for (File child : file.listFiles()) {
	            deleteRecursively(child);
	        }
	    }
	
	    System.gc();
	
	    if (!file.delete()) {
	        try {
	            throw new ServiceException("Impossible de supprimer " + file.getCanonicalPath());
	        } catch (IOException e) {
	        	throw new ServiceException("Erreur lors de l'obtention du chemin canonique pour " + file);
	        }
	    }
	}
	// --- <<IS-END-SHARED>> ---
}

