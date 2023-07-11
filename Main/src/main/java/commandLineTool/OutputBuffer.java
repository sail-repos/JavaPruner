package commandLineTool;

public class OutputBuffer {
    private final String stdout;
    private final String stderr;

    /**
     * Create an OutputBuffer, a class for storing and managing stdout and stderr
     * results separately
     *
     * @param stdout stdout result
     * @param stderr stderr result
     */
    public OutputBuffer(String stdout, String stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
    }

    /**
     * Returns the stdout result
     *
     * @return stdout result
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * Returns the stderr result
     *
     * @return stderr result
     */
    public String getStderr() {
        return stderr;
    }
}
