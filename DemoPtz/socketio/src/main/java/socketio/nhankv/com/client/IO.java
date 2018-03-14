package socketio.nhankv.com.client;


import android.util.Log;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.Call;
import okhttp3.WebSocket;
import socketio.nhankv.com.parser.Parser;


public class IO {
    private static boolean isPathHaveSlash = false;

    private static final Logger logger = Logger.getLogger(IO.class.getName());

    private static final ConcurrentHashMap<String, Manager> managers = new ConcurrentHashMap<String, Manager>();

    /**
     * Protocol version.
     */
    public static int protocol = Parser.protocol;

    public static void setDefaultOkHttpWebSocketFactory(WebSocket.Factory factory) {
        Manager.defaultWebSocketFactory = factory;
    }

    public static void setDefaultOkHttpCallFactory(Call.Factory factory) {
        Manager.defaultCallFactory = factory;
    }

    private IO() {
    }

    public static Socket socket(String uri) throws URISyntaxException {
        return socket(uri, null);
    }

    public static Socket socket(String uri, Options opts) throws URISyntaxException {
        return socket(new URI(uri), opts);
    }

    public static Socket socket(URI uri) {
        return socket(uri, null);
    }

    /**
     * Initializes a {@link Socket} from an existing {@link Manager} for multiplexing.
     *
     * @param uri  uri to connect.
     * @param opts options for socket.
     * @return {@link Socket} instance.
     */
    public static Socket socket(URI uri, Options opts) {
        if (opts == null) {
            opts = new Options();
        }
        isPathHaveSlash = checkPathHaveSlash(uri);
        URL parsed = null;
        //isPathHaveSlash of nhankv
        try {
            if (isPathHaveSlash) {
                parsed = new URL(uri.toString());
            } else {
                parsed = Url.parse(uri);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        URI source;
        try {
            if (isPathHaveSlash) {
                source = URI.create(uri.toString() + "/");
            } else {
                source = parsed.toURI();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String id = "", path = "";

        if (isPathHaveSlash) {
            id = uri.toString();
            path = "/";
        } else {
            id = Url.extractId(parsed);
            path = parsed.getPath();
        }

        boolean sameNamespace = managers.containsKey(id)
                && managers.get(id).nsps.containsKey(path);
        boolean newConnection = opts.forceNew || !opts.multiplex || sameNamespace;
        Manager io;

        if (newConnection) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("ignoring socket cache for %s", source));
            }
            io = new Manager(source, opts);
        } else {
            if (!managers.containsKey(id)) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(String.format("new io instance for %s", source));
                }
                managers.putIfAbsent(id, new Manager(source, opts));
            }
            io = managers.get(id);
        }

        String pathSocket = "";
        if (isPathHaveSlash) {
            pathSocket = "/";
        } else {
            pathSocket = parsed.getPath();
        }

        if (pathSocket != null && (opts.query == null || opts.query.isEmpty())) {
            opts.query = pathSocket;
        }
        Log.d("Socket", " Path = " + parsed.getPath());

        return io.socket(parsed.getPath(), opts);
    }

    public static boolean checkPathHaveSlash(URI uri) {
        String[] arrUrl = uri.toString().replace("//", "/").split("/");
        if (arrUrl.length > 2) {
            return true;
        }
        return false;
    }

    public static class Options extends Manager.Options {

        public boolean forceNew;

        /**
         * Whether to enable multiplexing. Default is true.
         */
        public boolean multiplex = true;
    }
}
