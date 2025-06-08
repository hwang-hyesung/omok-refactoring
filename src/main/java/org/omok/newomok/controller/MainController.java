package org.omok.newomok.controller;

import lombok.extern.log4j.Log4j2;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@Log4j2
@WebServlet(displayName = "mainController", urlPatterns = "/omok/main")
public class MainController extends HttpServlet {

}
