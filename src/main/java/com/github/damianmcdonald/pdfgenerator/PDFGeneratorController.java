package com.github.damianmcdonald.pdfgenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/generatepdf")
public class PDFGeneratorController {

    private static final String PDF_FILE_NAME = "generatedPDF.pdf";

    @Autowired
    private PDFGeneratorService service;

    @RequestMapping("/libreoffice")
    public ResponseEntity<byte[]> generatePDFLibreOffice(@RequestParam(value="pdfhtml") final String html) throws Exception {
        return new ResponseEntity<byte[]>(service.generatePDFLibreOffice(html), getHttpHeadersForPDF(), HttpStatus.OK);
    }

    @RequestMapping(value = "/itext", method = RequestMethod.POST)
    public ResponseEntity<byte[]> generatePDFITextNew(@RequestParam("pdfhtml") final String html) throws Exception {
        return new ResponseEntity<byte[]>(service.generatePDFIText(html), getHttpHeadersForPDF(), HttpStatus.OK);
    }

    private HttpHeaders getHttpHeadersForPDF() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.setContentDispositionFormData(PDF_FILE_NAME, PDF_FILE_NAME);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return headers;
    }

}
