/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.mobileid.id.FPS.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.HttpHeaders;
import vn.mobileid.id.FPS.component.field.ConnectorField;
import vn.mobileid.id.FPS.object.InternalResponse;
import vn.mobileid.id.FPS.object.User;
import vn.mobileid.id.FPS.systemManagement.LogHandler;
import vn.mobileid.id.utils.Utils;

/**
 *
 * @author Admin
 */
public class UtilsController extends HttpServlet{
    
    public static void service_(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        String method = req.getMethod();
        switch (method) {
            case "GET": {
                new UtilsController().doGet(req, res);
                break;
            }
           
            default: {
                res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                res.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");                
            }
        }
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
         if (req.getRequestURI().matches("^/fps/v1/documents/fields$")) {
            String transactionId = Utils.getTransactionId(req, null);
            String payload = Utils.getPayload(req);
            LogHandler.request(
                    UtilsController.class,
                    Utils.getDataRequestToLog(req, transactionId, "Get Field Type", payload));            
                try {
                    InternalResponse response = ConnectorField.getFieldType(req, transactionId);
                    Utils.sendMessage(
                            res,
                            response.getStatus(),
                            "application/json",
                            response.getMessage(),
                            transactionId);
                } catch (Exception ex) {
                    catchException(
                            ex,
                            req,
                            res,
                            payload, 
                            0,
                            transactionId);
                }            
        } else {
            Utils.sendMessage(
                    res,
                    A_FPSConstant.HTTP_CODE_METHOD_NOT_ALLOWED,
                    "application/json",
                    null,
                    "");
        }
    }
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (req.getRequestURI().matches("^/fps/v1/documents/[0-9]+/fields.*$")) {
            String transactionId = Utils.getTransactionId(req, null);
            String payload = Utils.getPayload(req);
            LogHandler.request(
                    UtilsController.class,
                    Utils.getDataRequestToLog(req, transactionId, "Add Field", payload));
            if (!Utils.isNullOrEmpty(req.getContentType()) && req.getContentType().contains("application/json")) {
                try {
                    InternalResponse response = ConnectorField.addField(req, payload, transactionId);
                    
                    Utils.createAPILog(req,
                            payload, 
                            (int) Utils.getIdFromURL(req.getRequestURI()),
                            response,
                            response.getException(),
                            transactionId);
                    
                    Utils.sendMessage(
                            res,
                            response.getStatus(),
                            "application/json",
                            response.getMessage(),
                            transactionId);
                } catch (Exception ex) {
                    catchException(
                            ex,
                            req,
                            res,
                            payload, 
                            (int)Utils.getIdFromURL(req.getRequestURI()),
                            transactionId);
                }
            } else {
                Utils.sendMessage(
                        res,
                        HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                        "application/json",
                        null,
                        "none");
            }
        } else {
            Utils.sendMessage(
                    res,
                    A_FPSConstant.HTTP_CODE_METHOD_NOT_ALLOWED,
                    "application/json",
                    null,
                    "none");
        }
    }
    
    //==========================================================================
    //<editor-fold defaultstate="collapsed" desc="Catch Exception">
    private static void catchException(
            Exception ex,
            HttpServletRequest req,
            HttpServletResponse res,
            String payload,
            int documentId,
            String transactionId) {
        try {
            User user = Utils.getUserFromBearerToken(req.getHeader("Authorization"));

            InternalResponse response = new InternalResponse();
            response.setUser(user);
            response.setMessage("INTERNAL EXCEPTION");
            response.setException(ex);

            LogHandler.error(
                    UtilsController.class,
                    transactionId,
                    ex);

            Utils.sendMessage(
                    res,
                    A_FPSConstant.HTTP_CODE_INTERNAL_SERVER_ERROR,
                    "application/json",
                    A_FPSConstant.INTERNAL_EXP_MESS,
                    transactionId);

            Utils.createAPILog(req, payload, documentId, response, response.getException(), transactionId);
        } catch (IOException ex1) {
            Logger.getLogger(DocumentController.class.getName()).log(Level.SEVERE, null, ex1);
        }
    }
    //</editor-fold>
}
