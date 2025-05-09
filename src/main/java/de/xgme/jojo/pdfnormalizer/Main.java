package de.xgme.jojo.pdfnormalizer;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.IOException;

/**
 * Created by JojOatXGME on 21.12.2016.
 */
public class Main {

    public static void main(String[] args)
    {
        if (args.length != 2) {
            System.err.println("Invalid amount of arguments.");
            System.err.println("Usage: java -jar pdfnormalizer.jar <input> <output>");
            System.exit(1);
            return;
        }

        final File inputFile = new File(args[0]);
        final File outputFile = new File(args[1]);

        // Load input file
        final PDDocument doc;
        try {
            doc = Loader.loadPDF(inputFile);
        } catch (IOException e) {
            System.err.println("Could not read input file: " + e.getMessage());
            System.exit(1);
            return;
        }

        // Modify pages
        int pageNumber = 0;
        for (final PDPage page : doc.getPages()) {
            final float tx = -page.getTrimBox().getLowerLeftX();
            final float ty = -page.getTrimBox().getLowerLeftY();
            final Matrix mat = Matrix.getTranslateInstance(tx, ty);

            pageNumber += 1;
            System.err.println("Move origin of page " + pageNumber + " by " + -tx + ", " + -ty);

            // Update views
            {
                final PDRectangle mediaBox = page.getMediaBox();
                final PDRectangle artBox = page.getArtBox();
                final PDRectangle bleedBox = page.getBleedBox();
                final PDRectangle cropBox = page.getCropBox();
                final PDRectangle trimBox = page.getTrimBox();

                page.setMediaBox(new PDRectangle(
                        mediaBox.getLowerLeftX() + tx, mediaBox.getLowerLeftY() + ty,
                        mediaBox.getWidth(), mediaBox.getHeight()));
                page.setArtBox(new PDRectangle(
                        artBox.getLowerLeftX() + tx, artBox.getLowerLeftY() + ty,
                        artBox.getWidth(), artBox.getHeight()));
                page.setBleedBox(new PDRectangle(
                        bleedBox.getLowerLeftX() + tx, bleedBox.getLowerLeftY() + ty,
                        bleedBox.getWidth(), bleedBox.getHeight()));
                page.setCropBox(new PDRectangle(
                        cropBox.getLowerLeftX() + tx, cropBox.getLowerLeftY() + ty,
                        cropBox.getWidth(), cropBox.getHeight()));
                page.setTrimBox(new PDRectangle(
                        trimBox.getLowerLeftX() + tx, trimBox.getLowerLeftY() + ty,
                        trimBox.getWidth(), trimBox.getHeight()));
            }

            // Move content of page
            try (PDPageContentStream stream = new PDPageContentStream(
                    doc, page, PDPageContentStream.AppendMode.PREPEND, true)) {
                stream.transform(mat);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
                return;
            }
        }

        // Save modified document
        try {
            doc.save(outputFile);
            doc.close();
        } catch (IOException e) {
            System.err.println("Could not write to output file: " + e.getMessage());
            System.exit(1);
            return;
        }
    }
}
