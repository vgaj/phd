package com.github.vgaj.phd;

import com.github.vgaj.phd.display.ModelGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PhoneHomeDetectorController
{
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ModelGenerator modelGenerator;

    @GetMapping("/")
    //public String index(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
    public String index(Model model)
    {
        // TODO: Avoid running as root - (1) Periodically generate XML report to a file and format it to HTML
        // TODO: Avoid running as root - (2) Have a separate web and query the service via a domain socket
        model.addAttribute("content", modelGenerator.getDisplayContent());
        return "index";
    }

    @GetMapping("/data")
    public String data(@RequestParam(name="address", required=false, defaultValue="") String address, Model model)
    {
        List<String> data = null;
        try
        {
            data = modelGenerator.getData(InetAddress.getByName(address));
        } catch (Throwable t)
        {
            logger.error("Failed to lookup data for address " + address, t);
            data = new ArrayList<>();
        }
        model.addAttribute("address", address);
        model.addAttribute("content", data);
        return "data";
    }

}