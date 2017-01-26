package com.couchbase;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.HttpClientBuilder;
import com.couchbase.client.java.document.json.*;
import java.net.URL;
import java.util.*;
import java.io.*;

public class MovieFXController implements Initializable {

    @FXML
    private TextField fxTitle;

    @FXML
    private TextField fxGenre;

    @FXML
    private CheckBox fxDigital;

    @FXML
    private CheckBox fxBluray;

    @FXML
    private CheckBox fxDvd;

    @FXML
    private ListView fxListView;

    @FXML
    private Button fxSave;

    @FXML
    private Button fxRefresh;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        refresh();
        fxListView.setCellFactory(new Callback<ListView<Movie>, ListCell<Movie>>(){
            @Override
            public ListCell<Movie> call(ListView<Movie> p) {
                ListCell<Movie> cell = new ListCell<Movie>(){
                    @Override
                    protected void updateItem(Movie t, boolean bln) {
                        super.updateItem(t, bln);
                        String item = "";
                        ArrayList<String> formats = new ArrayList<String>();
                        if (t != null) {
                            item = t.getTitle() + ", " + t.getGenre();
                            if(t.getDigitalCopy()) {
                                formats.add("Digital");
                            }
                            if(t.getBluray()) {
                                formats.add("Blu-Ray");
                            }
                            if(t.getDvd()) {
                                formats.add("DVD");
                            }
                            item += " [" + String.join("/", formats) + "]";
                            setText(item);
                        }
                    }
                };
                return cell;
            }
        });
        fxSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if(!fxTitle.getText().equals("") && !fxGenre.getText().equals("")) {
                    // Step #3 - Sending Data to the Backend Java API
                    // Hint
                    // Gather the form data and submit it via the `makePostRequest` function
                    /* CUSTOM CODE HERE */
                    fxTitle.setText("");
                    fxGenre.setText("");
                    fxDigital.setSelected(false);
                    fxBluray.setSelected(false);
                    fxDvd.setSelected(false);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Missing Information");
                    alert.setHeaderText(null);
                    alert.setContentText("Both a title and genre are required for this example.");
                    alert.showAndWait();
                }
            }
        });
        fxRefresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                refresh();
            }
        });
    }

    private void refresh() {
        fxListView.getItems().clear();
        // Step #2 - Request Data from the Backend with a GET Request
        // Hint
        // Use `makeGetRequest` and store the result in the `fxListView`
        /* CUSTOM CODE HERE */
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

    private JsonObject makeGetRequest(String url) {
        JsonObject jsonResult = JsonObject.create();
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet post = new HttpGet(url);
            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            String result = "";
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            jsonResult.put("status", response.getStatusLine().getStatusCode());
            jsonResult.put("content", JsonArray.fromJson(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonResult;
    }

}
