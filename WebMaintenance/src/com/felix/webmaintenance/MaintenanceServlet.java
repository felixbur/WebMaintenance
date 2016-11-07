package com.felix.webmaintenance;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.felix.util.Constants;
import com.felix.util.StringUtil;

public class MaintenanceServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		MaintenanceManager maintenanceManger = (MaintenanceManager) getServletContext()
				.getAttribute("maintenanceManager");
		req.setCharacterEncoding(com.felix.util.Constants.CHAR_ENC);
		resp.setCharacterEncoding(Constants.CHAR_ENC);
		String command = req.getParameter("command");
		String file = req.getParameter("file");
		String answerString = "";

		if (StringUtil.isEmpty(command)) {
			PrintWriter out = resp.getWriter();
			out.println("Needs request parameter: command");
			return;
		}
		String targetPage = req.getParameter("targetPage");
		if (file != null) {
			if (command.compareTo("show") == 0) {
				answerString = "show";
			} else if (command.compareTo("save") == 0) {
				answerString = "show";
				String contents = req.getParameter("content");
				maintenanceManger.writeFileToDisk(contents, file);
			}
			maintenanceManger.setActFile(file);
		}  else if (command.compareTo(MaintenanceManager.REFRESH_FILES) == 0) {
			maintenanceManger.rereadFilesFromDisk();
		}  else if (command.compareTo(MaintenanceManager.REINITIALIZE) == 0) {
			maintenanceManger.init();
		} else if (command.startsWith(MaintenanceManager.EXECUTE_TRIGGER)) {
			maintenanceManger.executeCommand(command);
		} else if (command.startsWith(MaintenanceManager.SHOWINFO_TRIGGER)) {
			String info = maintenanceManger.showInfo(command);
			answerString = info;
		}

		RequestDispatcher rd = getServletContext().getRequestDispatcher(
				targetPage);
		req.setAttribute("result", answerString);
		rd.forward(req, resp);
	}
}
