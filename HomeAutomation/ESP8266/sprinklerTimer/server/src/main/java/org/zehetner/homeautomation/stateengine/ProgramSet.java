package org.zehetner.homeautomation.stateengine;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.zehetner.homeautomation.common.CombinedProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/19/12
 * Time: 6:30 PM
 */
public class ProgramSet {
    private static final Logger LOG = Logger.getLogger(ProgramSet.class.getName());

    private List<Program> programs = new ArrayList<Program>(5);

    public void addProgram(final Program program) {
        this.programs.add(program);
    }

    public List<Program> getPrograms() {
        return Collections.unmodifiableList(this.programs);
    }

    public SprinklerProgram getProgram(final String name) {
        for (Program program : this.programs) {
           if (program instanceof SprinklerProgram) {
               SprinklerProgram sp = (SprinklerProgram)program;
               if (sp.getName().equals(name)) {
                   return sp;
               }
           }
        }
        return null;
    }


    public void junitClearPrograms() {
        this.programs.clear();
    }

    public void loadPrograms() {
        final String xmlStr;
        try {
            final String fileName = CombinedProperties.getProgramSetXmlFileName();
            LOG.info("Loading programs from the xml file " + System.getProperty("user.dir") + "  " + fileName);
            xmlStr = FileUtils.readFileToString(new File(fileName));
            loadFromXml(xmlStr);

        } catch (Throwable t) {
            LOG.warn("Couldn't load program set file", t);
        }
    }

    public void savePrograms() {
        try {
            final String fileName = CombinedProperties.getProgramSetXmlFileName();
            LOG.info("Saving programs to the xml file " + System.getProperty("user.dir") + fileName);
            FileUtils.writeStringToFile(new File(fileName), toXml());
        } catch (Throwable t) {
            LOG.warn("Couldn't save program set file", t);
        }
    }

    public String toXml() {
        final XStream xStream = new XStream();
        xStream.autodetectAnnotations(true);
        return xStream.toXML(this.programs);
    }

    public void loadFromXml(final String xmlStr) {
        final XStream xStream = new XStream();
        xStream.processAnnotations(Program.class);
        xStream.processAnnotations(SprinklerProgram.class);
        this.programs = (List<Program>)xStream.fromXML(xmlStr);
    }

}
