package org.lahab.clucene.server;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.microsoft.windowsazure.services.core.storage.StorageException;

public class DebugServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected IndexerNode _node;
	
	public DebugServlet(IndexerNode node) {
		_node = node;
	}
	
	public void setNode(IndexerNode node) {
		_node = node;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        if (request.getRequestURI().equals("/_debug/download")) {
        	try {
				_node.download();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	response.getWriter().println("Download finished");
        }
        
	}
}
