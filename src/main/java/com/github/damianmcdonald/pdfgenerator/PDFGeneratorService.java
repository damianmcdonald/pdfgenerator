package com.github.damianmcdonald.pdfgenerator;

import com.lowagie.text.Document;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class PDFGeneratorService {

    private static final String EXTENSION_PDF = ".pdf";
    private static final String EXTENSION_HTML = ".html";
    private static final ExecutorService THREAD_POOL =
            new ThreadPoolExecutor(0, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    private static final long EXT_PROCESS_TIMEOUT = 30L;

    @Value("${tmp.file.directory}")
    private String TMP_FILE_DIRECTORY;

    @Value("${libre.office.binary}")
    private String LIBRE_OFFICE_BINARY;

    public byte[] generatePDFLibreOffice(final String html) throws Exception {
        final String fileName = generateUniqueFileName();
        final String htmlFile = fileName + EXTENSION_HTML;
        final String pdfFile = fileName + EXTENSION_PDF;
        // create tmp html file
        Files.write(Paths.get(htmlFile), html.getBytes());
        final String command = String.format("%s --headless --norestore --writer --convert-to pdf %s --outdir %s", LIBRE_OFFICE_BINARY, htmlFile, TMP_FILE_DIRECTORY);
        final Process p = Runtime.getRuntime().exec(command);
        try {
            final Future<Void> f = THREAD_POOL.submit(new Callable<Void>() {
                public Void call() throws InterruptedException {
                    p.waitFor();
                    return null;
                }
            });
            f.get(EXT_PROCESS_TIMEOUT, TimeUnit.SECONDS);
            try (BufferedReader ir = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                String line = ir.readLine();
                if (line != null && line.length() != 0) {
                    final StringBuilder error = new StringBuilder(line.length() + 1000);
                    while ((line = ir.readLine()) != null) {
                        error.append(line);
                    }
                    throw new IllegalStateException(error.toString());
                }
            }
        } finally {
            p.destroy();
        }
        return Files.readAllBytes(Paths.get(pdfFile));
    }

    public byte[] generatePDFIText(final String html) throws Exception {
        final String pdfFile = generateUniqueFileName() + EXTENSION_PDF;
        // step 1
        final Document document = new Document();
        // step 2
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        // step 3
        document.open();
        // step 4
        final HTMLWorker htmlWorker = new HTMLWorker(document);
        htmlWorker.parse(new StringReader(html));
        // step 5
        document.close();
        return Files.readAllBytes(Paths.get(pdfFile));
    }

    private String generateUniqueFileName() {
        return TMP_FILE_DIRECTORY + File.separator + UUID.randomUUID().toString();
    }

}
