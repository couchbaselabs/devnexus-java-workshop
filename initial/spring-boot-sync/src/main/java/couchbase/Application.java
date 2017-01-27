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
import java.io.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.HttpClientBuilder;

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

    @Value("${gateway}")
    private String gateway;

    public @Bean
    Cluster cluster() {
        return CouchbaseCluster.create(hostname);
    }

    public @Bean
    Bucket bucket() {
        return cluster().openBucket(bucket, password);
    }

    @RequestMapping(value="/movies", method= RequestMethod.GET)
    public Object getMovies() {
        String query = "SELECT title, genre, formats FROM `" + bucket().name() + "` WHERE title IS NOT MISSING";
        return bucket().async().query(N1qlQuery.simple(query, N1qlParams.build().consistency(ScanConsistency.REQUEST_PLUS)))
            .flatMap(AsyncN1qlQueryResult::rows)
            .map(result -> result.value().toMap())
            .toList()
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();
    }

    @RequestMapping(value="/search", method= RequestMethod.GET)
    public Object searchMovies(@RequestParam("title") String title) {
        if(title == null || title.equals("")) {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `title` is required").toString(), HttpStatus.BAD_REQUEST);
        }
        String statement = "SELECT title, genre, formats FROM `" + bucket().name() + "` WHERE title IS NOT MISSING AND LOWER(title) LIKE '%' || $search || '%'";
        JsonObject parameters = JsonObject.create().put("search", title.toLowerCase());
        ParameterizedN1qlQuery query = ParameterizedN1qlQuery.parameterized(statement, parameters);
        return bucket().async().query(query)
            .flatMap(AsyncN1qlQueryResult::rows)
            .map(result -> result.value().toMap())
            .toList()
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();
    }

    @RequestMapping(value="/movies", method=RequestMethod.POST)
    public Object createMovie(@RequestBody String json) {
        JsonObject jsonData = JsonObject.fromJson(json);
        // Step #1 - Using the Sync Gateway RESTful API
        // Hint
        // Think back to requests in the `javafx-http` project
        /* CUSTOM CODE HERE */
        return new ResponseEntity<String>(json, HttpStatus.OK);
    }

    private JsonObject makePostRequest(String url, String body) {
        JsonObject jsonResult = JsonObject.create();
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(body, ContentType.create("application/json")));
            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            String result = "";
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            jsonResult.put("status", response.getStatusLine().getStatusCode());
            jsonResult.put("content", JsonObject.fromJson(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonResult;
    }

}
