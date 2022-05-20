package java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ProcessBuilder用于创建操作系统进程。
 * 每个ProcessBuilder实例管理一个进程的属性。
 * start()方法使用这些属性创建一个新的Process实例。 可以从同一实例重复调用start()方法以创建具有相同或相关属性的新子进程。
 */
public final class ProcessBuilder {
    // 命令
    private List<String> command;
    // 工作目录，默认值是当前进程的当前工作目录，通常是系统属性user.dir命名的目录
    private File directory;
    // 环境，依赖于系统的映射（从变量到值的）。初始值是当前进程环境的副本（可以System.getenv()获取操作系统的环境变量 ）
    private Map<String,String> environment;
    private boolean redirectErrorStream;
    private Redirect[] redirects;
    // 使用指定的操作系统程序和参数构造进程构建器。
    // 此构造不会使副本command列表。 对列表的后续更新将反映在流程构建器的状态中。
    // 不检查command是否对应于有效的操作系统命令。
    // 形参：command – 包含程序及其参数的列表
    public ProcessBuilder(List<String> command) {
        if (command == null)
            throw new NullPointerException();
        this.command = command;
    }

    public ProcessBuilder(String... command) {
        this.command = new ArrayList<>(command.length);
        for (String arg : command)
            this.command.add(arg);
    }

    public ProcessBuilder command(List<String> command) {
        if (command == null)
            throw new NullPointerException();
        this.command = command;
        return this;
    }

    public ProcessBuilder command(String... command) {
        this.command = new ArrayList<>(command.length);
        for (String arg : command)
            this.command.add(arg);
        return this;
    }

    public List<String> command() {
        return command;
    }
    //  返回进程构建器的环境
    public Map<String,String> environment() {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkPermission(new RuntimePermission("getenv.*"));

        if (environment == null)
            environment = ProcessEnvironment.environment();

        assert environment != null;

        return environment;
    }

    // Only for use by Runtime.exec(...envp...)
    ProcessBuilder environment(String[] envp) {
        assert environment == null;
        if (envp != null) {
            environment = ProcessEnvironment.emptyEnvironment(envp.length);
            assert environment != null;

            for (String envstring : envp) {
                if (envstring.indexOf((int) '\u0000') != -1)
                    envstring = envstring.replaceFirst("\u0000.*", "");

                int eqlsign =
                    envstring.indexOf('=', ProcessEnvironment.MIN_NAME_LENGTH);
                // Silently ignore envstrings lacking the required `='.
                if (eqlsign != -1)
                    environment.put(envstring.substring(0,eqlsign), envstring.substring(eqlsign+1));
            }
        }
        return this;
    }
    // 返回此流程构建器的工作目录。
    // 随后由该对象的start()方法start()子进程将使用它作为它们的工作目录。
    // 返回值可能为null ——这意味着使用当前 Java 进程的工作目录，通常是系统属性user.dir命名的目录，作为子进程的工作目录。
    public File directory() {
        return directory;
    }

    public ProcessBuilder directory(File directory) {
        this.directory = directory;
        return this;
    }

    // ---------------- I/O Redirection ----------------

    static class NullInputStream extends InputStream {
        static final NullInputStream INSTANCE = new NullInputStream();
        private NullInputStream() {}
        public int read()      { return -1; }
        public int available() { return 0; }
    }

    static class NullOutputStream extends OutputStream {
        static final NullOutputStream INSTANCE = new NullOutputStream();
        private NullOutputStream() {}
        public void write(int b) throws IOException {
            throw new IOException("Stream closed");
        }
    }
    // 重定向方式
    public static abstract class Redirect {
        public enum Type {
            PIPE,

            INHERIT,

            READ,

            WRITE,

            APPEND
        };

        public abstract Type type();

        public static final Redirect PIPE = new Redirect() {
                public Type type() { return Type.PIPE; }
                public String toString() { return type().toString(); }};

        public static final Redirect INHERIT = new Redirect() {
                public Type type() { return Type.INHERIT; }
                public String toString() { return type().toString(); }};

        public File file() { return null; }

        boolean append() {
            throw new UnsupportedOperationException();
        }

        public static Redirect from(final File file) {
            if (file == null)
                throw new NullPointerException();
            return new Redirect() {
                    public Type type() { return Type.READ; }
                    public File file() { return file; }
                    public String toString() {
                        return "redirect to read from file \"" + file + "\"";
                    }
                };
        }

        public static Redirect to(final File file) {
            if (file == null)
                throw new NullPointerException();
            return new Redirect() {
                    public Type type() { return Type.WRITE; }
                    public File file() { return file; }
                    public String toString() {
                        return "redirect to write to file \"" + file + "\"";
                    }
                    boolean append() { return false; }
                };
        }

        public static Redirect appendTo(final File file) {
            if (file == null)
                throw new NullPointerException();
            return new Redirect() {
                    public Type type() { return Type.APPEND; }
                    public File file() { return file; }
                    public String toString() {
                        return "redirect to append to file \"" + file + "\"";
                    }
                    boolean append() { return true; }
                };
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (! (obj instanceof Redirect))
                return false;
            Redirect r = (Redirect) obj;
            if (r.type() != this.type())
                return false;
            assert this.file() != null;
            return this.file().equals(r.file());
        }

        public int hashCode() {
            File file = file();
            if (file == null)
                return super.hashCode();
            else
                return file.hashCode();
        }

        private Redirect() {}
    }

    private Redirect[] redirects() {
        if (redirects == null)
            redirects = new Redirect[] {
                Redirect.PIPE, Redirect.PIPE, Redirect.PIPE
            };
        return redirects;
    }

    public ProcessBuilder redirectInput(Redirect source) {
        if (source.type() == Redirect.Type.WRITE ||
            source.type() == Redirect.Type.APPEND)
            throw new IllegalArgumentException("Redirect invalid for reading: " + source);
        redirects()[0] = source;
        return this;
    }

    public ProcessBuilder redirectOutput(Redirect destination) {
        if (destination.type() == Redirect.Type.READ)
            throw new IllegalArgumentException("Redirect invalid for writing: " + destination);
        redirects()[1] = destination;
        return this;
    }

    public ProcessBuilder redirectError(Redirect destination) {
        if (destination.type() == Redirect.Type.READ)
            throw new IllegalArgumentException("Redirect invalid for writing: " + destination);
        redirects()[2] = destination;
        return this;
    }

    public ProcessBuilder redirectInput(File file) {
        return redirectInput(Redirect.from(file));
    }

    public ProcessBuilder redirectOutput(File file) {
        return redirectOutput(Redirect.to(file));
    }

    public ProcessBuilder redirectError(File file) {
        return redirectError(Redirect.to(file));
    }

    public Redirect redirectInput() {
        return (redirects == null) ? Redirect.PIPE : redirects[0];
    }

    public Redirect redirectOutput() {
        return (redirects == null) ? Redirect.PIPE : redirects[1];
    }

    public Redirect redirectError() {
        return (redirects == null) ? Redirect.PIPE : redirects[2];
    }

    public ProcessBuilder inheritIO() {
        Arrays.fill(redirects(), Redirect.INHERIT);
        return this;
    }

    public boolean redirectErrorStream() {
        return redirectErrorStream;
    }

    public ProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }
    // 使用此进程构建器的属性启动一个新进程。
    // 新进程将调用command()给出的命令和参数，在directory()给出的工作目录中，以及environment()给出的进程环境。
    public Process start() throws IOException {
        // Must convert to array first -- a malicious user-supplied
        // list might try to circumvent the security check.
        String[] cmdarray = command.toArray(new String[command.size()]);
        cmdarray = cmdarray.clone();

        for (String arg : cmdarray)
            if (arg == null)
                throw new NullPointerException();
        // Throws IndexOutOfBoundsException if command is empty
        String prog = cmdarray[0];

        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkExec(prog);

        String dir = directory == null ? null : directory.toString();

        for (int i = 1; i < cmdarray.length; i++) {
            if (cmdarray[i].indexOf('\u0000') >= 0) {
                throw new IOException("invalid null character in command");
            }
        }

        try {
            // 创建进程
            return ProcessImpl.start(cmdarray, environment, dir, redirects, redirectErrorStream);
        } catch (IOException | IllegalArgumentException e) {
            String exceptionInfo = ": " + e.getMessage();
            Throwable cause = e;
            if ((e instanceof IOException) && security != null) {
                // Can not disclose the fail reason for read-protected files.
                try {
                    security.checkRead(prog);
                } catch (SecurityException se) {
                    exceptionInfo = "";
                    cause = se;
                }
            }
            // It's much easier for us to create a high-quality error
            // message than the low-level C code which found the problem.
            throw new IOException(
                "Cannot run program \"" + prog + "\""
                + (dir == null ? "" : " (in directory \"" + dir + "\")")
                + exceptionInfo,
                cause);
        }
    }
}
