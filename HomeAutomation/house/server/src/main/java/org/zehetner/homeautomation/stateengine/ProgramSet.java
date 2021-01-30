package org.zehetner.homeautomation.stateengine;

import com.thoughtworks.xstream.XStream;

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
    private List<Program> programs = new ArrayList<Program>(5);

    public void addProgram(final Program program) {
        this.programs.add(program);
    }

    public List<Program> getPrograms() {
        return Collections.unmodifiableList(this.programs);
    }

    public String toXml() {
        final XStream xStream = new XStream();
        xStream.processAnnotations(SprinklerProgram.class);
        xStream.processAnnotations(SprinklerAction.class);
        xStream.processAnnotations(SprinklerRepeatPolicy.DayOfWeek.class);
        xStream.alias("programs", this.getClass());
        return xStream.toXML(this.programs);
    }

    public void loadFromXml(final String xmlStr) {
        final XStream xStream = new XStream();
        xStream.processAnnotations(SprinklerProgram.class);
        xStream.processAnnotations(SprinklerAction.class);
        xStream.processAnnotations(SprinklerRepeatPolicy.DayOfWeek.class);
        this.programs = (List<Program>)xStream.fromXML(xmlStr);
    }

}
