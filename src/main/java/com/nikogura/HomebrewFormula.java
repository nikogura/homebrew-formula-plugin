package com.nikogura;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import java.io.*;

/**
 * Goal which builds a Homebrew Formula
 *
 * @goal formula
 * 
 * @phase install
 */
public class HomebrewFormula extends AbstractMojo {
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * The version of the project
     * @parameter expression="${project.version}"
     * @required
     */
    private String version;

    /**
        The tarball to checksum
     * @parameter
     * @required
     */
    private String inputfile;

    /**
        The name of the generated formula
     * @parameter
     * @required
     */
    private String outputfile;

    /**
     * The template to use to generate the formula
     * @parameter
     * @required
     */
    private String template;

    /**
     *
     * @throws MojoExecutionException
     */

    public void execute() throws MojoExecutionException {
        String checksum = generateChecksum();

        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            STGroupFile file = new STGroupFile(template);
            ST template = file.getInstanceOf("formula");

            template.add("version", version);
            template.add("checksum", checksum);

            String formula = template.render();

            File f = outputDirectory;

            if ( !f.exists() ) {
                f.mkdirs();
            }

            File outputFile = new File(f, outputfile);

            fw = new FileWriter(outputFile.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write(formula);
            bw.close();

        } catch (IOException e) {
            throw new MojoExecutionException( "Error creating file " + outputfile, e );
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }

            } catch (IOException e) {
               e.printStackTrace();
            }
        }
    }

    private String generateChecksum() {
        String checksum = "checksum";
        try {
            FileInputStream fis = new FileInputStream(new File(inputfile));
            try {
                checksum = DigestUtils.sha256Hex(fis);
                fis.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return checksum;
    }
}
