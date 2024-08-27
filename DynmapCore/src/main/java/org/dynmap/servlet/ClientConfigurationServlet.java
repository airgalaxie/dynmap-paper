package org.dynmap.servlet;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dynmap.DynmapCore;
import org.dynmap.InternalClientUpdateComponent;
import org.json.simple.JSONObject;
import static org.dynmap.JSONUtils.s;

public class ClientConfigurationServlet extends HttpServlet {
    private static final long serialVersionUID = 9106801553080522469L;
    private Charset cs_utf8 = Charset.forName("UTF-8");

    public ClientConfigurationServlet(DynmapCore plugin) {
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        byte[] outputBytes;

        JSONObject json = new JSONObject();

        s(json, "loggedin", false);
        JSONObject obj = InternalClientUpdateComponent.getClientConfig();
        if(obj != null) {
            json.putAll(obj);

        }
        outputBytes = json.toJSONString().getBytes(cs_utf8);

        String dateStr = new Date().toString();
        res.addHeader("Date", dateStr);
        res.setContentType("text/plain; charset=utf-8");
        res.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
        res.addHeader("Last-modified", dateStr);
        res.setContentLength(outputBytes.length);
        res.getOutputStream().write(outputBytes);
    }
}
