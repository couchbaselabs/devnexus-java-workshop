package couchbase;

import com.couchbase.client.java.*;
import com.couchbase.client.java.env.*;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.query.*;
import com.couchbase.client.java.query.consistency.ScanConsistency;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.bind.annotation.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
@RequestMapping("/")
public class Application implements Filter {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}

    @Value("${hostname}")
    private String hostname;

    @Value("${bucket}")
    private String bucket;

    @Value("${password}")
    private String password;

    public @Bean
    Cluster cluster() {
        // Step #1 - Connect to the Couchbase Cluster
        /* CUSTOM CODE HERE */
    }

    public @Bean
    Bucket bucket() {
        // Step #2 - Open a Couchbase Bucket
        /* CUSTOM CODE HERE */
    }

    @RequestMapping(value="/movies", method= RequestMethod.GET)
    public Object getMovies() {
        // Step #4 - Query for All Documents in the Couchbase Bucket
        // Hint
        // Think N1QL and RxJava.  You want to return a List of Map.
        /* CUSTOM CODE HERE */
    }

    @RequestMapping(value="/search", method= RequestMethod.GET)
    public Object searchMovies(@RequestParam("title") String title) {
        if(title == null || title.equals("")) {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `title` is required").toString(), HttpStatus.BAD_REQUEST);
        }
        // Step #5 - Query for Documents with Parameterization and N1QL
        // Hint
        // Think N1QL and RxJava.  Parameterization fights SQL injection.  You want to return a List of Map.
        /* CUSTOM CODE HERE */
    }

    @RequestMapping(value="/movies", method=RequestMethod.POST)
    public Object createMovie(@RequestBody String json) {
        JsonObject jsonData = JsonObject.fromJson(json);
        if(jsonData.getString("title") == null || jsonData.getString("title").equals("")) {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `title` is required").toString(), HttpStatus.BAD_REQUEST);
        } else if(jsonData.getString("genre") == null || jsonData.getString("genre").equals("")) {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `genre` is required").toString(), HttpStatus.BAD_REQUEST);
        }
        // Step #3 - Insert a New Couchbase Document
        /* CUSTOM CODE HERE */
        return new ResponseEntity<String>(json, HttpStatus.OK);
    }

}
