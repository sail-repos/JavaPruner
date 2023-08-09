package Reducer;

public class ClassInfo {

    public String id;

    public String project;

    public String classname;

    public String classpath;

    public String vmoptions;

    public int testtype;

    public String filename;

    public ClassInfo(){

    }

    public ClassInfo(String id, String project, String classnme, String classpath, String vmoptions, int testtype, String filename){
        this.id = id;
        this.project = project;
        this.classname = classnme;
        this.classpath = classpath;
        this.vmoptions = vmoptions;
        this.testtype = testtype;
        this.filename = filename;
    }

}
